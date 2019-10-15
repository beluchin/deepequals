package deepequals;

import java.util.List;

import static java.lang.String.format;

final class Cycle {
    private final List<Getter> methods;
    private final int idx;

    Cycle(final List<Getter> methods, final int idx) {
        this.methods = methods;
        this.idx = idx;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(format("cycle starts at index: %d\n", idx));
        int i = 0;
        for (final Getter g: methods) {
            builder.append(format("%d: %s::%s\n",
                                  i++, g.declaringClass().getName(),
                                  g.name()));
        }
        return builder.toString();
    }
}
