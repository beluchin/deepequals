package deepequals;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.reflect.Modifier.isFinal;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("UnstableApiUsage")
final class FieldBasedGetter implements Getter {
    private static final Predicate<Field> IS_FINAL = f -> isFinal(f.getModifiers());

    private final Field field;
    private final TypeToken typeToken;

    private FieldBasedGetter(final Field field, final TypeToken typeToken) {
        this.field = field;
        this.typeToken = typeToken;
    }

    @Override
    public Class<?> declaringClass() {
        return field.getDeclaringClass();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FieldBasedGetter that = (FieldBasedGetter) o;
        return field.equals(that.field) &&
                typeToken.equals(that.typeToken);
    }

    @Override
    public Object get(final Object x) {
        try {
            return field.get(x);
        } catch (final IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, typeToken);
    }

    @Override
    public String name() {
        return field.getName();
    }

    @Override
    public TypeToken type() {
        return typeToken.resolveType(field.getGenericType());
    }


    static Set<Getter> fieldBasedGetters(final TypeToken tt) {
        final Set<FieldBasedGetter> result = stream(tt.getRawType().getFields())
                .filter(IS_FINAL)
                .map(f -> new FieldBasedGetter(f, tt))
                .collect(toSet());
        result.forEach(g -> g.field.setAccessible(true));
        return ImmutableSet.copyOf(result);
    }
}
