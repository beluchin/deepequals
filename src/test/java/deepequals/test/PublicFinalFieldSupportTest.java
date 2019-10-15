package deepequals.test;

import org.junit.jupiter.api.Test;

import static deepequals.DeepEquals.deepEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    // TODO cycle
}
