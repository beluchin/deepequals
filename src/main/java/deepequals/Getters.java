package deepequals;

import com.google.common.reflect.TypeToken;

import java.util.Set;

import static deepequals.FieldBasedGetter.fieldBasedGetters;
import static deepequals.MethodBasedGetter.methodBasedGetters;

@SuppressWarnings("UnstableApiUsage")
final class Getters {
    private Getters() {}

    static Set<Getter> getters(final TypeToken tt, final Options options) {
        final Set<Getter> fieldGetters = fieldBasedGetters(tt);
        return !fieldGetters.isEmpty()
                ? fieldGetters
                : methodBasedGetters(tt, options);
    }
}
