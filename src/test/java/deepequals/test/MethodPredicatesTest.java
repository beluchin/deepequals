package deepequals.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static deepequals.MethodPredicates.methods;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodPredicatesTest {
    private static class Foo {}

    @Test
    void classMustHaveMethods() {
        assertThrows(IllegalArgumentException.class,
                     () -> methods(Foo.class, "whatever"));
    }

    @Disabled
    @Test
    void methodsFromObjectAreIllegal() {}
}
