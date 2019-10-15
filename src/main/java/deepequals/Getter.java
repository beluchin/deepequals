package deepequals;

import com.google.common.reflect.TypeToken;

@SuppressWarnings("UnstableApiUsage")
interface Getter {
    Class<?> declaringClass();
    Object get(Object x);
    String name();
    TypeToken type();
}
