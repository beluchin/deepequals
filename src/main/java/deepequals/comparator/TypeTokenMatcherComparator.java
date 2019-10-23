package deepequals.comparator;

import com.google.common.reflect.TypeToken;

import java.util.function.BiPredicate;

@SuppressWarnings("UnstableApiUsage")
public abstract class TypeTokenMatcherComparator extends Comparator {
    protected TypeTokenMatcherComparator(final BiPredicate predicate) {
        super(predicate);
    }

    public abstract boolean matches(TypeToken tt);
}
