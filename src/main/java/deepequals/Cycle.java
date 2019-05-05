package deepequals;

import java.lang.reflect.Method;
import java.util.List;

import static java.lang.String.format;

final class Cycle {
    private final List<Method> methods;
    private final int idx;

    Cycle(final List<Method> methods, final int idx) {
        this.methods = methods;
        this.idx = idx;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(format("cycle starts at index: %d\n", idx));
        int i = 0;
        for (final Method m : methods) {
            builder.append(format("%d: %s::%s\n",
                                  i++, m.getDeclaringClass().getName(),
                                  m.getName()));
        }
        return builder.toString();
    }
}
