package deepequals;

import static com.google.common.base.Throwables.propagate;
import static deepequals.Getters.getters;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiPredicate;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

@SuppressWarnings("rawtypes")
public final class DeepEquals {
    public interface WithOptions {
        <T> boolean deepEquals(Class<T> c, T x, T y);
        <T> boolean deepEquals(TypeToken<T> tt, T x, T y);

        boolean deepEqualsTypeUnsafe(Object classOrTypeToken, Object x, Object y);

        WithOptions ignore(BiPredicate<TypeToken, Method> first,
                           BiPredicate<TypeToken, Method>... rest);

        WithOptions orderLenient(/* add support to limit order leniency to certain types/method */);

        WithOptions override(Comparator... comparators);

        WithOptions typeLenient();

        WithOptions verbose();
    }

    public static abstract class Comparator {
        private final BiPredicate predicate;

        Comparator(final BiPredicate predicate) {
            this.predicate = predicate;
        }

        public BiPredicate predicate() {
            return predicate;
        }
    }

    public static class Field {
        private final TypeToken<?> typeToken;
        private final String fieldName;

        public Field(final TypeToken<?> typeToken, final String fieldName) {
            this.typeToken = typeToken;
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof Field))
                return false;
            final Field other = (Field) obj;
            if (fieldName == null) {
                if (other.fieldName != null)
                    return false;
            } else if (!fieldName.equals(other.fieldName))
                return false;
            if (typeToken == null) {
                if (other.typeToken != null)
                    return false;
            } else if (!typeToken.equals(other.typeToken))
                return false;
            return true;
        }

        public String fieldName() {
            return fieldName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((fieldName == null) ? 0 : fieldName.hashCode());
            result = prime * result
                    + ((typeToken == null) ? 0 : typeToken.hashCode());
            return result;
        }

        public TypeToken<?> typeToken() {
            return typeToken;
        }
    }

    public static class FieldComparator extends Comparator {
        private final Field field;

        public FieldComparator(final Field field, final BiPredicate f) {
            super(f);
            this.field = field;
        }

        Field field() {
            return field;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static class Stateful implements WithOptions {
        private final Map<TypeToken, BiPredicate> typeTokenComparators = new HashMap<>();
        private final Map<Field, BiPredicate> fieldComparators = new HashMap<>();
        private final Stack<String> objectPath = new Stack<>();
        private final Set<Field> ignoredFields = new HashSet<>();
        private final Set<BiPredicate<TypeToken, Method>> ignoredMethods = new HashSet<>();
        private CycleDetector cycleDetector;
        private boolean verbose = false;
        private boolean orderLenient = false;
        private boolean typeLenient = false;
        private Options options;

        @Override
        public <T> boolean deepEquals(final Class<T> c, final T x, final T y) {
            return deepEquals(TypeToken.of(c), x, y);
        }

        @Override
        public <T> boolean deepEquals(final TypeToken<T> tt, final T x, final T y) {
            options = new Options(typeLenient, ignoredMethods);

            boolean result = false;
            cycleDetector = new CycleDetector();
            try {
                result = deepEqualsImpl(tt, x, y);
            }
            finally {
                if (verbose && !result) {
                    printPath();
                }
            }
            return result;
        }

        @Override
        public boolean deepEqualsTypeUnsafe(
                final Object classOrTypeToken, final Object x, final Object y) {
            //noinspection unchecked
            return deepEquals(
                    classOrTypeToken instanceof Class
                            ? TypeToken.of((Class)classOrTypeToken)
                            : (TypeToken) classOrTypeToken,
                    x,
                    y);
        }

        @Override
        public WithOptions ignore(final BiPredicate<TypeToken, Method> first,
                                  final BiPredicate<TypeToken, Method>... rest) {
            ignoredMethods.add(first);
            ignoredMethods.addAll(asList(rest));
            return this;
        }

        @Override
        public WithOptions orderLenient() {
            orderLenient  = true;
            return this;
        }

        @Override
        public WithOptions override(final Comparator... comparators) {
            for (final Comparator c: comparators) {
                if (c instanceof TypeTokenComparator) {
                    override((TypeTokenComparator) c);
                }
                else {
                    override((FieldComparator) c);
                }
            }
            return this;
        }

        @Override
        public WithOptions typeLenient() {
            typeLenient  = true;
            return this;
        }

        @Override
        public WithOptions verbose() {
            verbose  = true;
            return this;
        }

        private boolean compareArrays(final TypeToken tt, final Object x, final Object y) {
            return compareSequencesOf(
                    tt.getComponentType(),
                    asList((Object[]) x),
                    asList((Object[]) y));
        }

        private boolean compareCollections(final TypeToken tt, final Object x, final Object y) {
            return compareSequencesOf(
                    getTypeArgToken(tt, 0),
                    asList(((Collection<?>) x).toArray(new Object[] {})),
                    asList(((Collection<?>) y).toArray(new Object[] {})));
        }

        @SuppressWarnings("unchecked")
        private boolean compareDeep(final TypeToken tt, final Object x, final Object y) {
            final Set<Getter> getters = getters(tt, options);

            return getters.stream()
                    .allMatch(getter -> {
                        final Object xfield = getter.get(x);
                        final Object yfield = getter.get(y);
                        return deepEqualsImpl(getter.type(), xfield, yfield);
                    });

            /*
            return ms.stream()
                    .allMatch(m -> {
                        cycleDetector.add(m);
                        cycleDetector.getCycle().ifPresent(c -> {
                            throw new CycleException(c.toString());
                        });

                        final String fieldName = m.getName();
                        pushNode(fieldName);
                        final Object xfield = invoke(m, x);
                        final Object yfield = invoke(m, y);
                        final Optional<BiPredicate> override = override(tt,
                                                                        fieldName);
                        final boolean equals = override.isPresent()
                                ? override.get().test(xfield, yfield)
                                : deepEqualsImpl(tt.resolveType(m.getGenericReturnType()),
                                                 xfield,
                                                 yfield);
                        if (equals) {
                            popNode();
                        }
                        cycleDetector.remove();
                        return equals;
                    });
             */
        }

        private boolean compareIterables(final TypeToken tt, final Object x, final Object y) {
            return compareSequencesOf(
                    getTypeArgToken(tt, 0),
                    Lists.newArrayList((Iterable<?>) x),
                    Lists.newArrayList((Iterable<?>) y));
        }

        private boolean compareLists(final TypeToken tt, final Object x, final Object y) {
            return compareSequencesOf(getTypeArgToken(tt, 0), (List<?>) x, (List<?>) y);
        }

        private boolean compareMaps(final TypeToken tt, final Object x, final Object y) {
            final Map<?, ?> mapx = (Map<?, ?>) x;
            final Map<?, ?> mapy = (Map<?, ?>) y;
            final TypeToken<?> valueTT = getTypeArgToken(tt, 1);
            return mapx.size() == mapy.size()
                    && mapx.keySet().stream().allMatch(k ->
                        mapy.containsKey(k)
                        && deepEqualsImpl(valueTT, mapx.get(k), mapy.get(k)));
        }

        private boolean compareOptionals(final TypeToken tt, final Object x, final Object y) {
            final Optional<?> optx = (Optional<?>) x;
            final Optional<?> opty = (Optional<?>) y;

            return (!optx.isPresent() && !opty.isPresent())
                    || (optx.isPresent() && opty.isPresent()
                            && deepEqualsImpl(
                                    getTypeArgToken(tt, 0),
                                    optx.get(), opty.get()));
        }

        private boolean compareSequencesOf(
                final TypeToken componentType, final List x, final List y) {
            return orderLenient
                    ? compareSequencesOfLenient(componentType, x, y)
                    : compareSequencesOfStrict(componentType, x, y);
        }

        private boolean compareSequencesOfLenient(
                final TypeToken componentType, final List x, final List y) {
            final int xSize = x.size();
            final int ySize = y.size();
            if (xSize != ySize) {
                return false;
            }
            final Set<Integer> yIndices = new HashSet<>();
            return x.stream()
                    .allMatch(e -> {
                        return range(0, ySize)
                                .filter(idx -> !yIndices.contains(idx))
                                .filter(yidx -> {
                                    if (deepEqualsImpl(componentType, e, y.get(yidx))) {
                                        yIndices.add(yidx);
                                        return true;
                                    }
                                    return false;
                                })
                                .findAny()
                                .isPresent();
                    });
        }

        private boolean compareSequencesOfStrict(final TypeToken componentType,
                final List x, final List y) {
            final int xSize = x.size();
            final int ySize = y.size();
            if (!range(0, xSize)
                    .allMatch(idx -> {
                        pushIndexedNode(idx);
                        final boolean equals = idx < xSize && idx < ySize
                                && deepEqualsImpl(componentType, x.get(idx), y.get(idx));
                        if (equals) {
                            popIndexedNode();
                        }
                        return equals;
                    })) {
                return false;
            }
            if (xSize != ySize) {
                pushIndexedNode(xSize);
                return false;
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private boolean deepEqualsImpl(final TypeToken tt, final Object x, final Object y) {
            if ((x == null && y != null) || (x != null && y == null)) {
                return false;
            }
            if (x == y || (x == null && y == null)) {
                return true;
            }
            if (typeTokenComparators.containsKey(tt)) {
                return typeTokenComparators.get(tt).test(x, y);
            }
            if (compareWithEquals(tt)) {
                return x.equals(y);
            }
            if (tt.isArray()) {
                return compareArrays(tt, x, y);
            }
            if (comparing(tt, Optional.class)) {
                return compareOptionals(tt, x, y);
            }
            if (comparing(tt, Set.class)) {
                return compareSets(x, y);
            }
            if (comparing(tt, Map.class)) {
                return compareMaps(tt, x, y);
            }
            if (comparing(tt, List.class)) {
                return compareLists(tt, x, y);
            }
            if (comparing(tt, Collection.class)) {
                return compareCollections(tt, x, y);
            }
            if (comparing(tt, Iterable.class)) {
                return compareIterables(tt, x, y);
            }
            return compareDeep(tt, x, y);
        }

        private void override(final FieldComparator c) {
            final Field field = c.field();
            if (this.fieldComparators.containsKey(field)) {
                throw new IllegalArgumentException(format(
                        "there is a comparator already defined for %s.%s",
                        field.typeToken().getType().getTypeName(),
                        field.fieldName()));
            }
            this.fieldComparators.put(field, c.predicate());
        }

        private Optional<BiPredicate> override(final TypeToken tt, final String fieldName) {
            final Field f = field(tt, fieldName);
            return Optional.ofNullable(fieldComparators.getOrDefault(f, null));
        }

        private void override(final TypeTokenComparator c) {
            final TypeToken<?> tt = c.typeToken();
            if (this.typeTokenComparators.containsKey(tt)) {
                throw new IllegalArgumentException(format(
                        "there is a comparator already defined for %s",
                        tt.getType().getTypeName()));
            }
            this.typeTokenComparators.put(tt, c.predicate());
        }

        private void popIndexedNode() {
            final String top = objectPath.pop();
            objectPath.push(top.substring(0, top.lastIndexOf('[')));
        }

        private void popNode() {
            objectPath.pop();
        }

        private void printPath() {
            System.err.println(Joiner.on(".").join(objectPath));
        }

        private void pushIndexedNode(final int idx) {
            final String prefix = objectPath.isEmpty()? "": objectPath.pop();
            objectPath.push(format("%s[%d]", prefix, idx));
        }

        private void pushNode(final String name) {
            objectPath.push(name);
        }

        private static boolean compareSets(final Object x, final Object y) {
            final Set<?> setx = (Set<?>) x;
            final Set<?> sety = (Set<?>) y;
            return setx.size() == sety.size()
                    && setx.stream().allMatch(e -> sety.contains(e));
        }

        private static boolean compareWithEquals(final TypeToken tt) {
            final TypeToken<?> unwrapped = tt.unwrap();
            final Class<?> t = unwrapped.getRawType();
            return unwrapped.isPrimitive()
                    || t.isEnum()
                    || isOneOf(
                            t,
                            String.class,
                            LocalDate.class,
                            LocalTime.class,
                            LocalDateTime.class,
                            Object.class);
        }

        private static boolean comparing(final TypeToken tt, final Class c) {
            return tt.getRawType().equals(c);
        }

        private static boolean isOneOf(final Class c, final Class... classes) {
            return ImmutableSet.copyOf(classes).contains(c);
        }
    }

    public static class TypeTokenComparator extends Comparator {
        private final TypeToken typeToken;

        public TypeTokenComparator(final TypeToken typeToken, final BiPredicate predicate) {
            super(predicate);
            this.typeToken = typeToken;
        }

        public TypeToken typeToken() {
            return typeToken;
        }
    }

    public static <T> TypeTokenComparator comparator(final Class<T> c, final BiPredicate<T, T> f) {
        return new TypeTokenComparator(TypeToken.of(c), f);
    }

    public static <T> Comparator comparator(final Field field, final BiPredicate<T, T> f) {
        return new FieldComparator(field, f);
    }

    public static <T> TypeTokenComparator comparator(final TypeToken<T> tt, final BiPredicate<T, T> f) {
        return new TypeTokenComparator(tt, f);
    }

    public static <T> boolean deepEquals(final Class<T> c, final T x, final T y) {
        return new Stateful().deepEquals(c, x, y);
    }

    public static <T> boolean deepEquals(final TypeToken<T> tt, final T x, final T y) {
        return new Stateful().deepEquals(tt, x, y);
    }

    public static boolean deepEqualsTypeUnsafe(
            final Object classOrTypeToken, final Object x, final Object y) {
        return new Stateful().deepEqualsTypeUnsafe(classOrTypeToken, x, y);
    }

    public static Field field(final Class<?> c, final String name) {
        return new Field(TypeToken.of(c), name);
    }

    public static Field field(final TypeToken<?> tt, final String name) {
        return new Field(tt, name);
    }

    public static WithOptions withOptions() {
        return new Stateful();
    }

    // this method cannot be placed in a general purpose class (even though it is clearly
    // general-purpose) because otherwise the tests would not work due to the issue of not
    // being allowed to call methods on inner classes reflectively (even though the method may
    // be public)
    // http://www.javaspecialists.eu/archive/Issue117.html
    private static TypeToken getTypeArgToken(final TypeToken tt, final int i) {
        final ParameterizedType pt = (ParameterizedType) (tt.getType());
        return TypeToken.of(pt.getActualTypeArguments()[i]);
    }
}
