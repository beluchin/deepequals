package deepequals;

import com.google.common.reflect.TypeToken;

@SuppressWarnings("UnstableApiUsage")
interface Getter {
    Object get(Object x);
    TypeToken type();
}
