package deepequals.test.ignore;

import deepequals.DeepEquals;
import org.junit.jupiter.api.Test;

import static deepequals.DeepEquals.withOptions;
import static deepequals.MethodPredicates.methods;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IgnoreNoInheritanceTest {
    @SuppressWarnings("unused")
    public static class Foo {
        private int bar = 0;
        private int baz = 0;
        private int xuq = 0;
        public int bar() {return bar;}
        public int baz() {return baz;}
        public int xuq() {return xuq;}
        static Foo bar(final int bar) {final Foo f = new Foo(); f.bar = bar; return f;}
        static Foo baz(final int baz) {final Foo f = new Foo(); f.baz = baz; return f;}
        static Foo xuq(final int xuq) {final Foo f = new Foo(); f.xuq = xuq; return f;}
        static Foo barBaz(final int bar, final int baz) {
            final Foo f = new Foo();
            f.bar = bar;
            f.baz = baz;
            return f;
        }
    }

    @Test
    void ignore() {
        //noinspection unchecked
        final DeepEquals.WithOptions wo = withOptions()
                .ignore(methods(Foo.class, "bar"));

        assertTrue(wo.deepEquals(Foo.class, Foo.bar(3), Foo.bar(42)));
        assertFalse(wo.deepEquals(Foo.class, Foo.baz(3), Foo.baz(42)));
    }

    @Test
    void ignoreMultiple() {
        //noinspection unchecked
        final DeepEquals.WithOptions withOptions = withOptions()
                .ignore(methods(Foo.class, "bar"),
                        methods(Foo.class, "baz"));

        assertTrue(withOptions.deepEquals(Foo.class, Foo.barBaz(3, 3), Foo.barBaz(42, 42)));
        assertFalse(withOptions.deepEquals(Foo.class, Foo.xuq(3), Foo.xuq(42)));
    }

    @Test
    void ignoreMultipleNoDup() {
        //noinspection unchecked
        final DeepEquals.WithOptions withOptions = withOptions()
                .ignore(methods(Foo.class, "bar", "baz"));

        assertTrue(withOptions.deepEquals(Foo.class, Foo.barBaz(3, 3), Foo.barBaz(42, 42)));
        assertFalse(withOptions.deepEquals(Foo.class, Foo.xuq(3), Foo.xuq(42)));
    }
}
