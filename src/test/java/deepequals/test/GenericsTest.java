package deepequals.test;

import com.google.common.reflect.TypeToken;
import deepequals.DeepEquals;
import deepequals.comparator.TypeTokenMatcherComparator;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static deepequals.DeepEquals.comparator;
import static deepequals.DeepEquals.field;
import static deepequals.DeepEquals.withOptions;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.RandomUtils.uniqueString;

@SuppressWarnings("UnstableApiUsage")
class GenericsTest {
    @SuppressWarnings("unused")
    static class Foo<T> {
        public String unique() {return uniqueString();}
    }

    @Test
    void lastMatchingComparatorWins() {
        assertTrue(withOptions()
                           .override(comparatorForParameterizedTypes(
                                   Foo.class,
                                   (x, y) -> false))
                           .override(comparatorForParameterizedTypes(
                                   Foo.class,
                                   (x, y) -> true))
                           .deepEquals(new TypeToken<Foo<String>>() {},
                                       new Foo<>(),
                                       new Foo<>()));
        assertFalse(withOptions()
                           .override(comparatorForParameterizedTypes(
                                   Foo.class,
                                   (x, y) -> true))
                           .override(comparatorForParameterizedTypes(
                                   Foo.class,
                                   (x, y) -> false))
                           .deepEquals(new TypeToken<Foo<String>>() {},
                                       new Foo<>(),
                                       new Foo<>()));
    }

    @Test
    void override() {
        assertTrue(withOptions()
                           .override(comparator(new TypeToken<Foo<String>>() {},
                                                (x, y) -> true))
                           .deepEquals(new TypeToken<Foo<String>>() {},
                                       new Foo<>(),
                                       new Foo<>()));
    }

    @Test
    void overrideAllViaRawType() {
        assertTrue(withOptions()
                           .override(comparatorForParameterizedTypes(
                                   Foo.class,
                                   (x, y) -> true))
                           .deepEquals(new TypeToken<Foo<String>>() {},
                                       new Foo<>(),
                                       new Foo<>()));
    }

    @SuppressWarnings("serial")
    @Test
    void overrideComparator() {
        class Foo {
            @SuppressWarnings("unused")
            public Supplier<Integer> get() { throw new UnsupportedOperationException(); }
        }
        final Foo _42Positive = new Foo() { @Override public Supplier<Integer> get() { return () -> 42; } };
        final Foo _42Negative = new Foo() { @Override public Supplier<Integer> get() { return () -> -42; } };
        final Foo _43 = new Foo() { @Override public Supplier<Integer> get() { return () -> 43; } };

        assertTrue(withOptions()
                           .override(comparator(
                                   new TypeToken<Supplier<Integer>>() {},
                                   (x, y) -> abs(x.get()) == abs(y.get())))
                           .deepEquals(Foo.class, _42Positive, _42Negative));
        assertFalse(withOptions()
                            .override(comparator(
                                    new TypeToken<Supplier<Integer>>() {},
                                    (x, y) -> abs(x.get()) == abs(y.get())))
                            .deepEquals(Foo.class, _42Positive, _43));
        assertFalse(withOptions()
                            .override(comparator(
                                    //              different supplier type
                                    new TypeToken<Supplier<Double>>() {},

                                    (x, y) -> abs(x.get()) == abs(y.get())))
                            .deepEquals(Foo.class, _42Positive, _42Negative));
    }

    @Test
    void overrideComparatorForField() {
        //noinspection unused
        class Foo {
            public Supplier<String> bad() {
                //noinspection Convert2Lambda,Anonymous2MethodRef
                return new Supplier<String>() {
                    @Override
                    public String get() {
                        return uniqueString();
                    }
                };
            }
            public Supplier<String> good() {
                return () -> "good";
            }
        }
        assertTrue(withOptions()
                           .override(DeepEquals. <Supplier<String>> comparator(
                                   field(Foo.class, "bad"),
                                   (x, y) -> !x.get().isEmpty() && !y.get().isEmpty()))
                           .deepEquals(Foo.class, new Foo(), new Foo()));
        assertFalse(withOptions()
                            .override(DeepEquals. <Supplier<String>> comparator(
                                    field(Foo.class, "good"),
                                    (x, y) -> true))
                            .deepEquals(Foo.class, new Foo(), new Foo()));
    }

    @Test
    void overridingRawType() {

        // overriding the raw type has no impact on the related
        // parameterized types!

        assertFalse(withOptions()
                           .override(comparator(Foo.class, (x, y) -> true))
                           .deepEquals(new TypeToken<Foo<String>>() {},
                                       new Foo<>(),
                                       new Foo<>()));
    }

    private static <T> TypeTokenMatcherComparator comparatorForParameterizedTypes(
            @SuppressWarnings("SameParameterValue") final Class<T> class__,
            final BiPredicate<T, T> predicate) {
        return new TypeTokenMatcherComparator(predicate) {
            @Override
            public boolean matches(final TypeToken tt) {
                return tt.getRawType().equals(class__);
            }
        };
    }
}
