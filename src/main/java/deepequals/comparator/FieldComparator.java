package deepequals.comparator;

import deepequals.DeepEquals;

import java.util.function.BiPredicate;

public class FieldComparator extends Comparator {
    private final DeepEquals.Field field;

    public FieldComparator(final DeepEquals.Field field, final BiPredicate f) {
        super(f);
        this.field = field;
    }

    public DeepEquals.Field field() {
        return field;
    }
}
