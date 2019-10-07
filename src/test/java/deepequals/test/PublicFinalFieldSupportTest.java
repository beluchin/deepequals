package deepequals.test;

import org.junit.jupiter.api.Test;

import static deepequals.DeepEquals.deepEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PublicFinalFieldSupportTest {
    @Test
    void happyPath() {
        class Foo {
            public final int x;
            Foo(final int x) { this.x = x; }
        }
        assertTrue(deepEquals(Foo.class,
                              new Foo(0),
                              new Foo(0)));
        assertFalse(deepEquals(Foo.class,
                              new Foo(0),
                              new Foo(42)));
    }
}
