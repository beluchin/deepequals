package deepequals;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class MethodPredicates {
    private MethodPredicates() {}

    public static <T> Predicate<Method> method(final Class<T> clazz,
                                               final String methodName) {
        return m -> m.equals(Methods.method(clazz, methodName));
    }
}
