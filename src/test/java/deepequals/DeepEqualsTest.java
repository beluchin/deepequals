package deepequals;

import static deepequals.DeepEquals.comparator;
import static deepequals.DeepEquals.deepEquals;
import static deepequals.DeepEquals.field;
import static deepequals.DeepEquals.withOptions;
import static deepequals.DeepEqualsTest.EnumFoo.Hello;
import static deepequals.DeepEqualsTest.EnumFoo.World;
import static java.lang.Math.abs;
import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static utils.RandomUtils.uniqueString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

@SuppressWarnings("unused")
public class DeepEqualsTest {

    @FunctionalInterface
    public interface Predicate3 {
        boolean test(Object arg1, Object arg2, Object arg3);
    }

    public static enum EnumFoo {
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

    @Test
    public void __2_class() {
        class Foo {
            public int get() { return 42; }
        };
        assertTrue(deepEquals(Foo.class, new Foo(), new Foo()));
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

    @SuppressWarnings("serial")
    @Test
    public void __6_maps() {

        // returns true if the maps have exactly the same keys
        // and the corresponding values are deep-equals.

        class Foo {
            int myInt() { return 42; }
        };
        assertTrue(deepEquals(
                new TypeToken<Map<Integer, Foo>>() {},
                ImmutableMap.of(1, new Foo()),
                new HashMap<Integer, Foo>() {{
                    put(1, new Foo() {});
                }}));

        // -- different size
        assertFalse(deepEquals(
                new TypeToken<Map<Integer, Foo>>() {},
                ImmutableMap.of(1, new Foo()),
                new HashMap<Integer, Foo>()));
        // -- same size, different keys
        assertFalse(deepEquals(
                new TypeToken<Map<Integer, Foo>>() {},
                ImmutableMap.of(1, new Foo()),
                new HashMap<Integer, Foo>() {{ put(2, new Foo()); }}));
        // -- same keys, values are not deep equals
        class Bar {
            public String myString() { return uniqueString(); }
        };
        assertFalse(deepEquals(
                new TypeToken<Map<Integer, Bar>>() {},
                ImmutableMap.of(1, new Bar()),
                new HashMap<Integer, Bar>() {{ put(1, new Bar()); }}));
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

    @Test
    public void _12_overrideClassComparator() {
        assertTrue(withOptions()
                .override(comparator(Integer.class, (x, y) -> abs(x) == abs(y)))
                .deepEquals(Integer.class, 42, -42));
        assertFalse(withOptions()
                .override(comparator(Integer.class, (x, y) -> abs(x) == abs(y)))
                .deepEquals(Integer.class, 43, 42));
    }

    @SuppressWarnings("serial")
    @Test
    public void _13_overrideTypeTokenComparator() {
        class Foo {
            public Supplier<Integer> get() { throw new UnsupportedOperationException(); }
        };

        final Foo _42Positive = new Foo() { @Override public Supplier<Integer> get() { return () -> 42; } };
        final Foo _42Negative = new Foo() { @Override public Supplier<Integer> get() { return () -> -42; } };
        final Foo _43 = new Foo() { @Override public Supplier<Integer> get() { return () -> 43; } };

        assertTrue(withOptions()
                .override(comparator(
                        new TypeToken<Supplier<Integer>>() {},
                        (x, y) -> abs(x.get()) == abs(y.get())))
                .deepEquals(Foo.class, _42Positive, _42Negative));
        assertFalse(withOptions()
                .override(comparator(
                        new TypeToken<Supplier<Integer>>() {},
                        (x, y) -> abs(x.get()) == abs(y.get())))
                .deepEquals(Foo.class, _42Positive, _43));
        assertFalse(withOptions()
                .override(comparator(
                        //              different supplier type
                        new TypeToken<Supplier<Double>>() {},

                        (x, y) -> abs(x.get()) == abs(y.get())))
                .deepEquals(Foo.class, _42Positive, _42Negative));
    }

    @Test
    public void _14_overrideFieldComparator() {
        class Foo {
            public String bad() { return uniqueString(); }
            public String good() { return "good"; }
        };
        assertTrue(withOptions()
                .override(DeepEquals. <String> comparator(field(Foo.class, "bad"), (x, y) -> {
                    return !x.isEmpty() && !y.isEmpty(); // field type aware
                }))
                .deepEquals(Foo.class, new Foo(), new Foo()));
        assertFalse(withOptions()
                .override(DeepEquals. <String> comparator(field(Foo.class, "good"), (x, y) -> true))
                .deepEquals(Foo.class, new Foo(), new Foo()));
    }

    @Test
    public void _15_overrideComparatorForFieldOnTypeToken() {
        class Foo {
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
        assertTrue(withOptions()
                .override(DeepEquals. <Supplier<String>> comparator(
                        field(Foo.class, "bad"),
                        (x, y) -> !x.get().isEmpty() && !y.get().isEmpty()))
                .deepEquals(Foo.class, new Foo(), new Foo()));
        assertFalse(withOptions()
                .override(DeepEquals. <Supplier<String>> comparator(
                        field(Foo.class, "good"),
                        (x, y) -> true))
                .deepEquals(Foo.class, new Foo(), new Foo()));
    }

    @Test
    public void _16_null() {
        assertTrue(deepEquals(Object.class, null, null));
        assertFalse(deepEquals(Object.class, null, new Object()));
        assertFalse(deepEquals(Object.class, new Object(), null));
    }

    @SuppressWarnings("serial")
    @Test
    public void _17_verbose() {
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try {
            class Foo {
                public String getString() { return uniqueString(); }
            };
            // --
            withOptions()
                    .verbose()
                    .deepEquals(Foo.class, new Foo(), new Foo());
            assertEquals("getString", get(errContent));
            // --
            final Supplier<Foo> fooSupplier1 = new Supplier<Foo>() {
                @Override
                public Foo get() { return new Foo(); }
            };
            final Supplier<Foo> fooSupplier2 = new Supplier<Foo>() {
                @Override
                public Foo get() { return new Foo(); }
            };
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Supplier<Foo>>() {},
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
                            new TypeToken<Optional<Foo>>() {},
                            empty(),
                            empty());
            assertEquals("", get(errContent));
            // --
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Optional<Foo>>() {},
                            empty(),
                            Optional.of(new Foo()));
            assertEquals("", get(errContent));
            // --
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Optional<Foo>>() {},
                            Optional.of(new Foo()),
                            Optional.of(new Foo()));
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
            final Supplier<List<Foo>> listOfFooSupplier1 = new Supplier<List<Foo>>() {
                @Override public List<Foo> get() { return ImmutableList.of(new Foo()); }
            };
            final Supplier<List<Foo>> listOfFooSupplier2 = new Supplier<List<Foo>>() {
                @Override public List<Foo> get() { return ImmutableList.of(new Foo()); }
            };
            withOptions()
                    .verbose()
                    .deepEquals(
                            new TypeToken<Supplier<List<Foo>>>() {},
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

    @Test
    public void _18_typeLenient() {
        class Foo {
            public int bar(final Object x) { return 0; }
            public void baz() {}
            public int xuq() { return 42; }
        }
        assertTrue(withOptions()
                .typeLenient()
                .deepEquals(Foo.class, new Foo(), new Foo()));
    }

    @Test
    public void class_not() {
        class Foo {
            public String get() { return uniqueString(); }
        };
        assertFalse(deepEquals(Foo.class, new Foo(), new Foo()));
    }

    @Test(expected = StackOverflowError.class)
    public void cyclesAreNotDetected() {
        class Foo {
            public Foo get() { return this; }
        };
        deepEquals(Foo.class, new Foo(), new Foo());
    }

    @SuppressWarnings("serial")
    @Test
    public void generics_not() {
        assertFalse(deepEquals(new TypeToken<Supplier<Integer>>() {}, () -> 43, () -> 42));
    }

    @Ignore
    @Test(expected = FieldNotFoundException.class)
    public void incorrectMethodNameOnFieldDeclaration() {
        fail("not implemented");
    }

    @Test(expected = IllegalClassException.class)
    public void methodsReturningVoidAreIllegal() {
        class Foo {
            public void bar() { throw new UnsupportedOperationException(); }
            public int baz() { throw new UnsupportedOperationException(); }
        }
        deepEquals(Foo.class, new Foo(), new Foo());
    }

    @Test(expected = IllegalClassException.class)
    public void methodsWithArgumentsAreIllegal() {
        class Foo {
            public int bar(final Object x) { throw new UnsupportedOperationException(); }
            public int baz() { throw new UnsupportedOperationException(); }
        }
        deepEquals(Foo.class, new Foo(), new Foo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void twoComparatorsForTheSameType() {
        withOptions()
                .override(comparator(int.class, (x, y) -> true),
                        comparator(int.class, (x, y) -> true));
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

    @Test
    public void bridgeMethods() {
        class Bar {
            public int get() { return 42; }
        }
        class Foo implements Supplier<Bar> {
            @Override
            public Bar get() { return new Bar();}
        }
        assertTrue(deepEquals(Foo.class, new Foo(), new Foo()));
    }

    private static void assertAll(
            final Consumer<Boolean> c, final Predicate3 p, final Object... args) {
        IntStream.range(0, args.length)
                .filter(i -> (i % 3 == 0))
                .forEach(i -> c.accept(p.test(args[i], args[i + 1], args[i + 2])));
    }

    private static void assertAllFalse(final Predicate3 f, final Object... args) {
        assertAll(Assert::assertFalse, f, args);
    }

    private static void assertAllTrue(final Predicate3 f, final Object... args) {
        assertAll(Assert::assertTrue, f, args);
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
