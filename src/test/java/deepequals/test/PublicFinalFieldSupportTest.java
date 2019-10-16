package deepequals.test;

import org.junit.jupiter.api.Test;

import static deepequals.DeepEquals.deepEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PublicFinalFieldSupportTest {
    @Test
    void happyPath() {
        //noinspection WeakerAccess
        class Base {
            public final int x;
            Base(final int x) { this.x = x; }
        }
        class Derived extends Base {
            Derived(final int x) { super(x);}
        }
        assertTrue(deepEquals(Derived.class,
                              new Derived(0),
                              new Derived(0)));
        assertFalse(deepEquals(Derived.class,
                               new Derived(0),
                               new Derived(42)));
    }

    // TODO either fields or methods but not both by default. User must choose.

    // TODO generics

    class FooCycle {
        public final BarCycle bar;
        FooCycle(BarCycle bar) { this.bar = bar; }
    }
    class BarCycle {
        private FooCycle foo;
        public FooCycle foo() { return foo; }
        public void set(FooCycle foo) { this.foo = foo; }
    }
    @Test
    void detectsCycles() {
        //noinspection unused
        BarCycle bar1 = new BarCycle();
        BarCycle bar2 = new BarCycle();
        FooCycle foo1 = new FooCycle(bar1);
        FooCycle foo2 = new FooCycle(bar2);
        bar1.set(foo1);
        bar2.set(foo2);
        assertThrows(IllegalArgumentException.class,
                     () -> deepEquals(FooCycle.class, foo1, foo2));
    }
}
