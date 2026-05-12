package com.gto.registrylib.state;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class StateRegistryManager {

    private final Map<Key, StateEntry<?>> entries = new LinkedHashMap<>();

    public void register(StateEntry<?> entry) {
        StateEntry<?> existing = entries.putIfAbsent(new Key(entry.scope(), entry.identifier()), entry);
        if (existing != null) {
            throw new IllegalStateException("Duplicate state entry: " + entry.scope().id() + " " + entry.identifier());
        }
    }

    public Optional<StateEntry<?>> get(StateScope scope, Identifier id) {
        return Optional.ofNullable(entries.get(new Key(scope, id)));
    }

    public Collection<StateEntry<?>> all() {
        return entries
                .values()
                .stream()
                .sorted(Comparator
                        .comparing((StateEntry<?> e) -> e.identifier().toString())
                        .thenComparing(e -> e.scope().id()))
                .toList();
    }

    private record Key(StateScope scope, Identifier id) {}
}
