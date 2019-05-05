package deepequals;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.builder;

final class CycleDetector {
    private final BiMap<Integer, Method> sequence = HashBiMap.create();
    private Cycle cycle = null;

    void add(final Method m) {
        final Integer idx = sequence.inverse().get(m);
        if (idx != null) {
            cycle = cycle(idx);
            return;
        }
        sequence.put(sequence.size(), m);
    }

    Optional<Cycle> getCycle() {
        return Optional.ofNullable(cycle);
    }

    void remove() {
        sequence.remove(sequence.size() - 1);
    }

    private Cycle cycle(final Integer idx) {
        final Builder<Method> b = builder();
        sequence.keySet().stream().sorted().forEach(i -> b.add(sequence.get(i)));
        return new Cycle(b.build(), idx);
    }
}
