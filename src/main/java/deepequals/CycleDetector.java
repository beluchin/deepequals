package deepequals;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList.Builder;

import java.util.Optional;

import static com.google.common.collect.ImmutableList.builder;

final class CycleDetector {
    private final BiMap<Integer, Getter> sequence = HashBiMap.create();
    private Cycle cycle = null;

    void add(final Getter getter) {
        final Integer idx = sequence.inverse().get(getter);
        if (idx != null) {
            cycle = cycle(idx);
            return;
        }
        sequence.put(sequence.size(), getter);
    }

    Optional<Cycle> getCycle() {
        return Optional.ofNullable(cycle);
    }

    void remove() {
        sequence.remove(sequence.size() - 1);
    }

    private Cycle cycle(final Integer idx) {
        final Builder<Getter> b = builder();
        sequence.keySet().stream().sorted().forEach(i -> b.add(sequence.get(i)));
        return new Cycle(b.build(), idx);
    }
}
