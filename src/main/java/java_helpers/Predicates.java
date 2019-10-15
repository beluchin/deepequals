package java_helpers;

import java.util.function.Predicate;

public final class Predicates {
    private Predicates() {}

    public static <T> Predicate<T> not(final Predicate<T> predicate) {
        return predicate.negate();
    }
}
