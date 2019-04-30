package deepequals.test;

import deepequals.MethodPredicates;
import org.junit.jupiter.api.Test;

import static deepequals.MethodPredicates.methods;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodPredicatesTest {
    public static class Foo {}

    @Test
    void classMustHaveMethods() {
        assertThrows(IllegalArgumentException.class,
                     () -> methods(Foo.class, "whatever"));
    }
}
