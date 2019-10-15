package deepequals;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

@SuppressWarnings("UnstableApiUsage")
final class Options {
    final boolean typeLenient;
    final Set<BiPredicate<TypeToken, Method>> ignoredMethods;

    Options(final boolean typeLenient,
            final Set<BiPredicate<TypeToken, Method>> ignoredMethods) {
        this.typeLenient = typeLenient;
        this.ignoredMethods = ignoredMethods;
    }
}
