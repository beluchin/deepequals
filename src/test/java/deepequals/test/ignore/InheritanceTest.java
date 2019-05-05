package deepequals.test.ignore;

import deepequals.DeepEquals;
import org.junit.jupiter.api.Test;

import static deepequals.DeepEquals.withOptions;
import static deepequals.MethodPredicates.methods;
import static deepequals.test.ignore.InheritanceTest.Derived.bar;
import static deepequals.test.ignore.InheritanceTest.Derived.foo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InheritanceTest {
    @SuppressWarnings("unused")
    public static class Base {
        int foo = 0;
        int bar = 0;
        public int foo() {return foo;}
        public int bar() {return bar;}
    }

    @SuppressWarnings("WeakerAccess")
    public static class Derived extends Base {
        static Derived foo(final int i) {final Derived f = new Derived (); f.foo = i; return f;}
        static Derived bar(final int i) {final Derived f = new Derived (); f.bar = i; return f;}
    }

    @Test
    void ignoreOnTypeBeingCompared() {
        //noinspection unchecked
        final DeepEquals.WithOptions wo = withOptions()
                .ignore(methods(Derived.class, "foo"));
        assertTrue(wo.deepEquals(Derived.class, foo(3), foo(42)));
        assertFalse(wo.deepEquals(Derived.class, bar(3), bar(42)));
    }

    @Test
    void ignoreOnAnyOtherType() {
        //noinspection unchecked
        assertFalse(withOptions()
                            .ignore(methods(Base.class, "foo"))
                            .deepEquals(Derived.class,
                                        foo(3),
                                        foo(42)));
    }
}
