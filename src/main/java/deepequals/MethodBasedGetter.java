package deepequals;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Throwables.propagate;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static java_helpers.Predicates.not;

@SuppressWarnings("UnstableApiUsage")
final class MethodBasedGetter implements Getter {
    private static final Predicate<Method> WithArguments = m -> m.getParameterCount() != 0;
    private static final Predicate<Method> ReturningVoid = m -> m.getReturnType().equals(Void.TYPE);
    private static final Predicate<Method> IsBridge = Method::isBridge;
    private static final Predicate<Method> IsStatic = MethodBasedGetter::isStatic;
    private static final Set<String> ObjectClassMethodNames = objectClassMethodNames();
    private static final Predicate<Method> IsMethodOfObject = m -> ObjectClassMethodNames.contains(m.getName());

    private final Method method;
    private final TypeToken typeToken;

    private MethodBasedGetter(final Method method, final TypeToken typeToken) {
        this.method = method;
        this.typeToken = typeToken;
    }

    @Override
    public Class<?> declaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MethodBasedGetter that = (MethodBasedGetter) o;
        return method.equals(that.method) &&
                typeToken.equals(that.typeToken);
    }

    @Override
    public Object get(final Object x) {
        try {
            return method.invoke(x);
        } catch (final Throwable e) {
            throw propagate(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, typeToken);
    }

    @Override
    public String name() {
        return method.getName();
    }

    @Override
    public TypeToken type() {
        return typeToken.resolveType(method.getGenericReturnType());
    }

    static Set<Getter> methodBasedGetters(final TypeToken tt, final Options options) {
        final Set<MethodBasedGetter> result = methodsToInvoke(tt, options).stream()
                .map(m -> new MethodBasedGetter(m, tt))
                .collect(toSet());
        result.forEach(m -> m.method.setAccessible(true));
        return ImmutableSet.copyOf(result);
    }

    private static void enforceNoMethodsReturningVoid(
            final Class c, final Set<Method> result) {
        result.stream()
                .filter(ReturningVoid)
                .findAny()
                .ifPresent(m -> {
                    throw new IllegalClassException(format(
                            "%s contains methods returning void.",
                            c.getName()));
                });
    }

    private static void enforceNoMethodsWithArguments(
            final Class c, final Set<Method> result) {
        result.stream()
                .filter(WithArguments)
                .findAny()
                .ifPresent(m -> {
                    throw new IllegalClassException(format(
                            "%s contains methods with arguments.",
                            c.getName()));
                });
    }

    private static Set<Method> excludeMethods(
            final Set<Method> original, final Predicate<Method> p) {
        return ImmutableSet.copyOf(original.stream()
                                           .filter(p.negate())
                                           .collect(toSet()));
    }

    private static Predicate<Method> ignoredOn(final TypeToken tt, final Options options) {
        return m -> options.ignoredMethods.stream()
                .anyMatch(p -> p.test(tt, m));
    }

    private static boolean isStatic(final Method m) {
        return Modifier.isStatic(m.getModifiers());
    }

    private static Set<Method> methodsToInvoke(
            final TypeToken tt,
            final Options options) {
        final Class c = tt.getRawType();
        Set<Method> result = stream(c.getMethods())
                .filter(not(IsBridge))
                .filter(not(IsStatic))
                .filter(not(IsMethodOfObject))
                .filter(not(ignoredOn(tt, options)))
                .collect(toSet());
        syntheticMethodsAreNotSupported(result);
        if (!options.typeLenient) {
            enforceNoMethodsWithArguments(c, result);
            enforceNoMethodsReturningVoid(c, result);
        }
        else {
            result = excludeMethods(result, WithArguments.or(ReturningVoid));
        }
        return result;
    }

    private static Set<String> objectClassMethodNames() {
        return ImmutableSet.copyOf(stream(Object.class.getMethods())
                                           .map(Method::getName)
                                           .collect(toSet()));
    }

    private static void syntheticMethodsAreNotSupported(final Set<Method> ms) {
        ms.stream()
                .filter(Method::isSynthetic)
                .findAny()
                .ifPresent(m -> {
                    throw new UnsupportedOperationException(String.format(
                            "method %s is synthetic", m.getName()));
                });
    }
}
