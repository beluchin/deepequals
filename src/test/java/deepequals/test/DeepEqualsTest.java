package deepequals.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import deepequals.DeepEquals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static deepequals.DeepEquals.*;
import static deepequals.test.DeepEqualsTest.EnumFoo.Hello;
import static deepequals.test.DeepEqualsTest.EnumFoo.World;
import static java.lang.Math.abs;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static utils.RandomUtils.uniqueString;

@SuppressWarnings("unused")
public class DeepEqualsTest {
    @FunctionalInterface
    public interface Predicate3 {
        boolean test(Object arg1, Object arg2, Object arg3);
    }

    public enum EnumFoo {
        Hello, World
    }

    @Test
    public void __1_equals() {
        assertAllTrue(DeepEquals::deepEqualsTypeUnsafe,
                      int.class, 1, 1,
                      Integer.class, 1, 1,
                      String.class, "hello", "hello",
                      LocalDate.class, LocalDate.MIN, LocalDate.MIN,
                      LocalTime.class, LocalTime.MIN, LocalTime.MIN,
                      LocalDateTime.class, LocalDateTime.MIN, LocalDateTime.MIN,
                      EnumFoo.class, Hello, Hello);

        final Object x = new Object();
        assertTrue(deepEquals(Object.class, x, x));

        assertAllFalse(DeepEquals::deepEqualsTypeUnsafe,
                int.class, 2, 1,
                Integer.class, 2, 1,
                String.class, "hello", "world",
                LocalDate.class, LocalDate.MIN, LocalDate.MAX,
                LocalDate.class, LocalDate.MIN, LocalDate.MAX,
                LocalDateTime.class, LocalDateTime.MIN, LocalDateTime.MAX,
                Object.class, new Object(), new Object(),
                EnumFoo.class, Hello, World);
    }

    public static class _2_Foo {
        public int get() { return 42; }
    };
    @Test
    public void __2_class() {
        assertTrue(deepEquals(_2_Foo.class, new _2_Foo(), new _2_Foo()));
    }

    @SuppressWarnings("serial")
    @Test
    public void __3_generics() {
        assertTrue(deepEquals(new TypeToken<Supplier<Integer>>() {}, () -> 42, () -> 42));
    }

    @SuppressWarnings("serial")
    @Test
    public void __4_optionals() {
        assertTrue(deepEqualsVarArgs(
                new TypeToken<Optional<Integer>>() {},
                Optional.empty(), Optional.empty(),
                Optional.of(42), Optional.of(42)));
        assertFalse(deepEqualsVarArgs(
                new TypeToken<Optional<Integer>>() {},
                Optional.empty(), Optional.of(42),
                Optional.of(42), Optional.empty()));
    }

    @SuppressWarnings("serial")
    @Test
    public void __5_sets() {

        // returns true if the sets have the same elements
        // according to the Set::contains method. It does not
        // deep compare the elements in the set

        assertTrue(deepEquals(
                new TypeToken<Set<Integer>>() {},
                ImmutableSet.of(1, 2),
                new HashSet<Integer>() {{
                    add(1);
                    add(2);
                }}));
        assertFalse(deepEquals(
                new TypeToken<Set<Integer>>() {},
                ImmutableSet.of(1, 2),
                new HashSet<Integer>() {{ add(1); }}));
    }

    public static class _6_Foo {
        public int myInt() { return 42; }
    }
    public static class _6_Bar {
        public String myString() { return uniqueString(); }
    }
    @SuppressWarnings("serial")
    @Test
    void __6_maps() {

        // returns true if the maps have exactly the same keys
        // and the corresponding values are deep-equals.

        assertTrue(deepEquals(
                new TypeToken<Map<Integer, _6_Foo>>() {},
                ImmutableMap.of(1, new _6_Foo()),
                new HashMap<Integer, _6_Foo>() {{
                    put(1, new _6_Foo() {});
                }}));

        // -- different size
        assertFalse(deepEquals(
                new TypeToken<Map<Integer, _6_Foo>>() {},
                ImmutableMap.of(1, new _6_Foo()),
                new HashMap<Integer, _6_Foo>()));
        // -- same size, different keys
        assertFalse(deepEquals(
                new TypeToken<Map<Integer, _6_Foo>>() {},
                ImmutableMap.of(1, new _6_Foo()),
                new HashMap<Integer, _6_Foo>() {{ put(2, new _6_Foo()); }}));
        // -- same keys, values are not deep equals
        assertFalse(deepEquals(
                new TypeToken<Map<Integer, _6_Bar>>() {},
                ImmutableMap.of(1, new _6_Bar()),
                new HashMap<Integer, _6_Bar>() {{ put(1, new _6_Bar()); }}));
    }

    @SuppressWarnings("serial")
    @Test
    public void __7_lists() {
        assertTrue(deepEquals(
                new TypeToken<List<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(1, 2)));
        assertFalse(deepEquals(
                new TypeToken<List<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(1, 2, 3)));
        // by default, order must be maintained
        assertFalse(deepEquals(
                new TypeToken<List<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(2, 1)));
    }

    @SuppressWarnings("serial")
    @Test
    public void __8_arrays() {
        assertTrue(deepEquals(
                new TypeToken<Integer[]>() {},
                new Integer[] {1, 2},
                new Integer[] {1, 2}));
        assertFalse(deepEquals(
                new TypeToken<Integer[]>() {},
                new Integer[] {1, 2},
                new Integer[] {1, 2, 3}));
        // by default, order must be maintained
        assertFalse(deepEquals(
                new TypeToken<Integer[]>() {},
                new Integer[] {1, 2},
                new Integer[] {2, 1}));
    }

    @SuppressWarnings("serial")
    @Test
    public void __9_iterables() {
        assertTrue(deepEquals(
                new TypeToken<Iterable<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(1, 2)));
        assertFalse(deepEquals(
                new TypeToken<Iterable<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(1, 2, 3)));
        // by default, order must be maintained
        assertFalse(deepEquals(
                new TypeToken<Iterable<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(2, 1)));
    }

    @SuppressWarnings("serial")
    @Test
    public void _10_collections() {
        assertTrue(deepEquals(
                new TypeToken<Collection<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(1, 2)));
        assertFalse(deepEquals(
                new TypeToken<Collection<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(1, 2, 3)));
        // by default, order must be maintained
        assertFalse(deepEquals(
                new TypeToken<Collection<Integer>>() {},
                ImmutableList.of(1, 2),
                ImmutableList.of(2, 1)));
    }

    @SuppressWarnings("serial")
    @Test
    public void _11_orderLenient() {

        // arrays, collections, and iterables also participate in the order-lenient option

        assertTrue(withOptions()
                .orderLenient()
                .deepEquals(
                        new TypeToken<List<Integer>>() {},
                        ImmutableList.of(1, 2),
                        ImmutableList.of(2, 1)));
        assertFalse(withOptions()
                .orderLenient()
                .deepEquals(
                        new TypeToken<List<Integer>>() {},
                        ImmutableList.of(1, 1, 2),
                        ImmutableList.of(1, 2)));
    }

    @Disabled
    @Test
    public void _12_equalsLenient() {
        fail("not implemented");
    }

    @Test
    public void _13_overrideClassComparator() {
        assertTrue(withOptions()
                .override(comparator(Integer.class, (x, y) -> abs(x) == abs(y)))
                .deepEquals(Integer.class, 42, -42));
        assertFalse(withOptions()
                .override(comparator(Integer.class, (x, y) -> abs(x) == abs(y)))
                .deepEquals(Integer.class, 43, 42));
    }

    public static class _14_Foo {
        public Supplier<Integer> get() { throw new UnsupportedOperationException(); }
    }
    @SuppressWarnings("serial")
    @Test
    void _14_overrideTypeTokenComparator() {
        final _14_Foo _42Positive = new _14_Foo() { @Override public Supplier<Integer> get() { return () -> 42; } };
        final _14_Foo _42Negative = new _14_Foo() { @Override public Supplier<Integer> get() { return () -> -42; } };
        final _14_Foo _43 = new _14_Foo() { @Override public Supplier<Integer> get() { return () -> 43; } };

        assertTrue(withOptions()
                .override(comparator(
                        new TypeToken<Supplier<Integer>>() {},
                        (x, y) -> abs(x.get()) == abs(y.get())))
                .deepEquals(_14_Foo.class, _42Positive, _42Negative));
        assertFalse(withOptions()
                .override(comparator(
                        new TypeToken<Supplier<Integer>>() {},
                        (x, y) -> abs(x.get()) == abs(y.get())))
                .deepEquals(_14_Foo.class, _42Positive, _43));
        assertFalse(withOptions()
                .override(comparator(
                        //              different supplier type
                        new TypeToken<Supplier<Double>>() {},

                        (x, y) -> abs(x.get()) == abs(y.get())))
                .deepEquals(_14_Foo.class, _42Positive, _42Negative));
    }

    public static class _15_Foo {
        public String bad() { return uniqueString(); }
        public String good() { return "good"; }
    };
    @Test
    void _15_overrideFieldComparator() {
        assertTrue(withOptions()
                .override(DeepEquals. <String> comparator(field(_15_Foo.class, "bad"), (x, y) -> {
                    return !x.isEmpty() && !y.isEmpty(); // field type aware
                }))
                .deepEquals(_15_Foo.class, new _15_Foo(), new _15_Foo()));
        assertFalse(withOptions()
                .override(DeepEquals. <String> comparator(field(_15_Foo.class, "good"), (x, y) -> true))
                .deepEquals(_15_Foo.class, new _15_Foo(), new _15_Foo()));
    }

    public static class _16_Foo {
        public Supplier<String> bad() {
            return new Supplier<String>() {
                @Override public String get() { return uniqueString(); }
            };
        }
        public Supplier<String> good() {
            return new Supplier<String>() {
                @Override public String get() { return "good"; }
            };
        }
    };
    @Test
    void _16_overrideComparatorForFieldOnTypeToken() {
        assertTrue(withOptions()
                .override(DeepEquals. <Supplier<String>> comparator(
                        field(_16_Foo.class, "bad"),
                        (x, y) -> !x.get().isEmpty() && !y.get().isEmpty()))
                .deepEquals(_16_Foo.class, new _16_Foo(), new _16_Foo()));
        assertFalse(withOptions()
                .override(DeepEquals. <Supplier<String>> comparator(
                        field(_16_Foo.class, "good"),
                        (x, y) -> true))
                .deepEquals(_16_Foo.class, new _16_Foo(), new _16_Foo()));
    }

    @Test
    public void _17_null() {
        assertTrue(deepEquals(Object.class, null, null));
        assertFalse(deepEquals(Object.class, null, new Object()));
        assertFalse(deepEquals(Object.class, new Object(), null));
    }

    public static class _18_Foo {
        public String getString() { return uniqueString(); }
    }
    @SuppressWarnings("serial")
    @Test
    void _18_verbose() {
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try {
            // --
            withOptions()
                    .verbose()
                    .deepEquals(_18_Foo.class, new _18_Foo(), new _18_Foo());
            assertEquals("getString", get(errContent));
            // --
            final Supplier<_18_Foo> fooSupplier1 = new Supplier<_18_Foo>() {
                @Override
                public _18_Foo get() { return new _18_Foo(); }
            };
            final Supplier<_18_Foo> fooSupplier2 = new Supplier<_18_Foo>() {
                @Override
                public _18_Foo get() { return new _18_Foo(); }
            };
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Supplier<_18_Foo>>() {},
                            fooSupplier1,
                            fooSupplier2);
            assertEquals("get.getString", get(errContent));
            // --
            withOptions()
                    .verbose()
                    .deepEquals(Integer.class, 1, 2);
            assertEquals("", get(errContent));
            // --
            // -- optionals
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Optional<_18_Foo>>() {},
                            empty(),
                            empty());
            assertEquals("", get(errContent));
            // --
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Optional<_18_Foo>>() {},
                            empty(),
                            Optional.of(new _18_Foo()));
            assertEquals("", get(errContent));
            // --
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Optional<_18_Foo>>() {},
                            Optional.of(new _18_Foo()),
                            Optional.of(new _18_Foo()));
            assertEquals("getString", get(errContent));
            // --
            // sets
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Supplier<Set<Integer>>>() {},
                            () -> ImmutableSet.of(1, 2),
                            () -> new HashSet<Integer>() {{ add(1); }});
            assertEquals("get", get(errContent));
            // --
            // lists/arrays
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<List<Integer>>() {},
                            ImmutableList.of(1, 2),
                            ImmutableList.of(1));
            assertEquals("[1]", get(errContent));
            // --
            final Supplier<List<_18_Foo>> listOfFooSupplier1 = new Supplier<List<_18_Foo>>() {
                @Override public List<_18_Foo> get() { return ImmutableList.of(new _18_Foo()); }
            };
            final Supplier<List<_18_Foo>> listOfFooSupplier2 = new Supplier<List<_18_Foo>>() {
                @Override public List<_18_Foo> get() { return ImmutableList.of(new _18_Foo()); }
            };
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Supplier<List<_18_Foo>>>() {},
                            listOfFooSupplier1,
                            listOfFooSupplier2);
            assertEquals("get[0].getString", get(errContent));
            // --
            // list/arrays order lenient
            withOptions()
                    .verbose()
                    .orderLenient()
                    .deepEquals(
                            new TypeToken<Supplier<List<Integer>>>() {},
                            new Supplier<List<Integer>>() {
                                @Override
                                public List<Integer> get() {
                                    return ImmutableList.of(1, 2);
                                }
                            },
                            new Supplier<List<Integer>>() {
                                @Override
                                public List<Integer> get() {
                                    return ImmutableList.of();
                                }
                            });
            // no index information is provided
            assertEquals("get", get(errContent));
        }
        finally {
            System.setErr(null);
        }
    }

    public static class _19_Foo {
        public int bar(final Object x) { return 0; }
        public void baz() {}
        public int xuq() { return 42; }
    }
    @Test
    void _19_typeLenient() {
        assertTrue(withOptions()
                .typeLenient()
                .deepEquals(_19_Foo.class, new _19_Foo(), new _19_Foo()));
    }

    public static class _class_not_Foo {
        public String get() { return uniqueString(); }
    }
    @Test
    void class_not() {
        assertFalse(deepEquals(_class_not_Foo.class, new _class_not_Foo(), new _class_not_Foo()));
    }

    public static class _cycles_Foo {
        public _cycles_Foo get() { return this; }
    };
    @Test
    void detectsCycles() {
        assertThrows(IllegalArgumentException.class,
                     () -> deepEquals(_cycles_Foo.class, new _cycles_Foo(), new _cycles_Foo()));
    }

    @SuppressWarnings("serial")
    @Test
    public void generics_not() {
        assertFalse(deepEquals(new TypeToken<Supplier<Integer>>() {}, () -> 43, () -> 42));
    }

    @Disabled
    @Test
    public void incorrectMethodNameOnFieldDeclaration() {
        fail("not implemented");
    }

    @Test
    public void methodsReturningVoidAreIllegal() {
        class Foo {
            public void bar() { throw new UnsupportedOperationException(); }
            public int baz() { throw new UnsupportedOperationException(); }
        }
        assertThrows(IllegalArgumentException.class,
                     () -> deepEquals(Foo.class, new Foo(), new Foo()));
    }

    @Test
    public void methodsWithArgumentsAreIllegal() {
        class Foo {
            public int bar(final Object x) { throw new UnsupportedOperationException(); }
            public int baz() { throw new UnsupportedOperationException(); }
        }
        assertThrows(IllegalArgumentException.class,
                     () -> deepEquals(Foo.class, new Foo(), new Foo()));
    }

    @Test
    public void twoComparatorsForTheSameType() {
        assertThrows(IllegalArgumentException.class,
                     () -> withOptions()
                             .override(comparator(int.class, (x, y) -> true),
                                       comparator(int.class, (x, y) -> true)));
    }

    @Test
    public void verboseWhenExceptionsAreThrown() {
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        class Foo {
            public String getString() { throw new RuntimeException(); }
        };
        try {
            withOptions()
                    .verbose()
                    .deepEquals(Foo.class, new Foo(), new Foo());
        }
        catch (final RuntimeException e) {
        }
        finally {
            System.setErr(null);
        }

        assertEquals("getString", get(errContent));
    }

    public static class _bridge_Bar {
        public int get() { return 42; }
    }
    public static class _bridge_Foo implements Supplier<_bridge_Bar> {
        @Override
        public _bridge_Bar get() { return new _bridge_Bar();}
    }
    @Test
    void bridgeMethods() {
        assertTrue(deepEquals(_bridge_Foo.class, new _bridge_Foo(), new _bridge_Foo()));
    }

    private static void assertAll(
            final Consumer<Boolean> c, final Predicate3 p, final Object... args) {
        IntStream.range(0, args.length)
                .filter(i -> (i % 3 == 0))
                .forEach(i -> c.accept(p.test(args[i], args[i + 1], args[i + 2])));
    }

    private static void assertAllFalse(final Predicate3 f, final Object... args) {
        assertAll(Assertions::assertFalse, f, args);
    }

    private static void assertAllTrue(final Predicate3 f, final Object... args) {
        assertAll(Assertions::assertTrue, f, args);
    }

    @SafeVarargs
    private static <T> boolean deepEqualsVarArgs(final TypeToken<T> tt, final T... xs) {
        return IntStream.range(0, xs.length)
                .filter(i -> (i % 2) == 0)
                .noneMatch(i -> !deepEquals(tt, xs[i], xs[i + 1]));
    }

    private static String get(final ByteArrayOutputStream err) {
        final String result = err.toString().trim();
        err.reset();
        return result;
    }

}
