package deepequals;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import java.util.Set;

import static deepequals.FieldBasedGetter.fieldBasedGetters;
import static deepequals.MethodBasedGetter.methodBasedGetters;

@SuppressWarnings("UnstableApiUsage")
final class Getters {
    private Getters() {}

    static Set<Getter> getters(final TypeToken tt, final Options options) {
        return Sets.union(
                fieldBasedGetters(tt),
                methodBasedGetters(tt, options));
    }
}
