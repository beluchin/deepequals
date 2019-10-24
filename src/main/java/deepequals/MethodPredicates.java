package deepequals;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

@SuppressWarnings("UnstableApiUsage")
public final class MethodPredicates {
    private MethodPredicates() {
    }

    public static BiPredicate<TypeToken, Method> methods(String first, String... rest) {
        final Set<String> names = set(first, rest);
        return (tt, m) -> names.contains(m.getName());
    }

    public static <T> BiPredicate<TypeToken, Method> methods(
            final Class<T> ignored,
            final String first,
            final String... rest) {
        final Set<String> names = set(first, rest);
        ensureGetMethods(ignored, names);
        return (tt, m) -> tt.getRawType().equals(ignored)
                && names.contains(m.getName());
    }

    public static <T> BiPredicate<TypeToken, Method> methods(
            final TypeToken<T> ignored,
            final String first,
            final String... rest) {
        final Set<String> names = set(first, rest);
        ensureGetMethods(ignored.getRawType(), names);
        return (tt, m) -> tt.equals(ignored) && names.contains(m.getName());
    }

    private static void ensureGetMethods(final Class c, final Set<String> ns) {
        ns.forEach(n -> {
            try {
                //noinspection unchecked
                c.getMethod(n);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(n);
            }
        });
    }

    private static Set<String> set(final String first, final String[] rest) {
        return ImmutableSet.<String>builder()
                .add(first)
                .add(rest)
                .build();
    }
}
