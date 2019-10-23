package deepequals.comparator;

import com.google.common.reflect.TypeToken;

import java.util.function.BiPredicate;

@SuppressWarnings("UnstableApiUsage")
public class TypeTokenComparator extends Comparator {
    private final TypeToken typeToken;

    public TypeTokenComparator(final TypeToken typeToken, final BiPredicate predicate) {
        super(predicate);
        this.typeToken = typeToken;
    }

    public TypeToken typeToken() {
        return typeToken;
    }
}
