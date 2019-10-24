package deepequals.test.ignore;

import org.junit.jupiter.api.Test;

import static deepequals.DeepEquals.withOptions;
import static deepequals.MethodPredicates.methods;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByNameTest {
    static class Foo {
        @SuppressWarnings("unused")
        public int bar() {throw new RuntimeException();}
    }

    @Test
    void byName() {
        //noinspection unchecked
        assertTrue(withOptions()
                           .ignore(methods("bar"))
                           .deepEquals(Foo.class, new Foo(), new Foo()));
    }
}
