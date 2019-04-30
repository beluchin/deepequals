package deepequals;

import java.lang.reflect.Method;

final class Methods {
    private Methods() {
    }

    static <T> Method method(final Class<T> clazz, final String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (final NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
}
