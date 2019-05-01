package deepequals.test.ignore;

import com.google.common.reflect.TypeToken;
import deepequals.DeepEquals;
import org.junit.jupiter.api.Test;

import static deepequals.DeepEquals.withOptions;
import static deepequals.MethodPredicates.methods;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnstableApiUsage")
class IgnoreGenericsTest {
    @SuppressWarnings("unused")
    public static class Foo<T> {
        public T bar() { throw new RuntimeException(); }
    }
    @SuppressWarnings("unused")
    public static class Wrapper {
        private final int i;

        private Wrapper(int i) { this.i = i; }

        public Foo<Integer> fooInteger() {
            return new Foo<Integer>() {
                @Override
                public Integer bar() { return i; }
            };
        }

        public Foo<String> fooString() {
            return new Foo<>();
        }
    }

    @Test
    void ignoreFieldGenerics() {
        //noinspection unchecked
        final DeepEquals.WithOptions withOptions = withOptions()
                .ignore(methods(new TypeToken<Foo<String>>() {}, "bar"));
        assertTrue(withOptions.deepEquals(
                Wrapper.class,
                new Wrapper(42),
                new Wrapper(42)));
        assertFalse(withOptions.deepEquals(
                Wrapper.class,
                new Wrapper(1),
                new Wrapper(42)));
    }
}
