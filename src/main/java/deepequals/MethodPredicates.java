package deepequals;

import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

public final class MethodPredicates {
    private MethodPredicates() {
    }

    public static BiPredicate<Class, Method> methods(String first, String... rest) {
        final ImmutableSet<String> names = ImmutableSet.<String>builder()
                .add(first)
                .add(rest)
                .build();
        return (c, m) -> names.contains(m.getName());
    }

    public static <T> BiPredicate<Class, Method> methods(
            final Class<T> ignored,
            final String first,
            final String... rest) {
        final ImmutableSet<String> names = ImmutableSet.<String>builder()
                .add(first)
                .add(rest)
                .build();
        ensureGetMethods(ignored, names);
        return (compared, m) -> compared.equals(ignored)
                && names.contains(m.getName());
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
}
