package deepequals.comparator;

import java.util.function.BiPredicate;

public abstract class Comparator {
    private final BiPredicate predicate;

    Comparator(final BiPredicate predicate) {
        this.predicate = predicate;
    }

    public BiPredicate predicate() {
        return predicate;
    }
}
