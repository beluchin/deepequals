package deepequals.test;

import com.google.common.reflect.TypeToken;
import deepequals.DeepEquals;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static deepequals.DeepEquals.comparator;
import static deepequals.DeepEquals.deepEquals;
import static deepequals.DeepEquals.deepEqualsTypeUnsafe;
import static deepequals.DeepEquals.withOptions;
import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EitherTest {
    interface Either<T1, T2> {
        <R> R join(Function<T1, R> map1, Function<T2, R> map2);
        static <T1, T2> Either<T1, T2> first(T1 x) { return new Either1<>(x); }
    }

    final static class Either1<T1, T2> implements Either<T1, T2> {
        private final T1 x;
        Either1(final T1 x) { this.x = x; }
        @Override
        public <R> R join(final Function<T1, R> map1,
                          final Function<T2, R> unused) {
            return map1.apply(x);
        }
    }

    @Test
    void either() {
        final DeepEquals.WithOptions options = withOptions().override(comparator(
                new TypeToken<Either<Integer, String>>() {},
                (lhs, rhs) -> lhs.join(
                        i -> deepEquals(Integer.class,
                                        i,
                                        rhs.join(identity(),
                                                 x -> null)),
                        s -> deepEquals(String.class,
                                        rhs.join(x -> null,
                                                 identity()),
                                        s))));
        assertTrue(options.deepEquals(
                new TypeToken<Either<Integer, String>>() {},
                Either.first(42),
                Either.first(42)));

        assertFalse(options.deepEquals(
                new TypeToken<Either<Integer, String>>() {},
                Either.first(42),
                Either.first(0)));
    }

    @Test
    void typeUnsafe() {
        final DeepEquals.WithOptions options = withOptions().override(comparator(
                Either.class,
                (lhs, rhs) -> {
                    final Function function1 = i -> deepEqualsTypeUnsafe(
                            i.getClass(),
                            i,
                            rhs.join(x -> x, y -> null));
                    final Function function2 = s -> deepEqualsTypeUnsafe(
                            s.getClass(),
                            rhs.join(x -> null, identity()),
                            s);
                    return (boolean) lhs.join(function1, function2);
                }));
        assertTrue(options.deepEquals(
                Either.class,
                Either.first(42),
                Either.first(42)));

        assertFalse(options.deepEquals(
                Either.class,
                Either.first(42),
                Either.first(0)));
    }
}
