package deepequals.test;

import com.google.common.reflect.TypeToken;
import deepequals.comparator.TypeTokenMatcherComparator;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

import static deepequals.DeepEquals.comparator;
import static deepequals.DeepEquals.withOptions;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.RandomUtils.uniqueString;

@SuppressWarnings("UnstableApiUsage")
class GenericsTest {
    @SuppressWarnings("unused")
    static class Foo<T> {
        public String throws_() {throw new UnsupportedOperationException();}
    }

    @Test
    void overrideAllParameterizedTypesViaRawType() {
        assertTrue(withOptions()
                           .override(comparatorForParameterizedTypes(
                                   Foo.class,
                                   (x, y) -> true))
                           .deepEquals(new TypeToken<Foo<String>>() {},
                                       new Foo<>(),
                                       new Foo<>()));
    }

    @Test
    void overridingParameterizedType() {
        assertTrue(withOptions()
                           .override(comparator(new TypeToken<Foo<String>>() {},
                                                (x, y) -> true))
                           .deepEquals(new TypeToken<Foo<String>>() {},
                                       new Foo<>(),
                                       new Foo<>()));
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

    // TODO bring here the tests all tests on generics.

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
