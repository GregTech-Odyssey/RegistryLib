package com.gto.registrylib.state;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class StateRegistryManager {

    private final Map<Key, StateEntry<?>> entries = new LinkedHashMap<>();

    public void register(StateEntry<?> entry) {
        Optional<StateEntry<?>> conflictingScope = entries.values().stream()
                .filter(existing -> existing.identifier().equals(entry.identifier()))
                .findFirst();
        if (conflictingScope.isPresent()) {
            StateEntry<?> existing = conflictingScope.get();
            throw new IllegalStateException(
                    "Duplicate state id: " + entry.identifier() + " is already registered as " + existing.scope().id() + " and cannot also be registered as " + entry.scope().id());
        }
        StateEntry<?> existing = entries.putIfAbsent(new Key(entry.scope(), entry.identifier()), entry);
        if (existing != null) {
            throw new IllegalStateException(
                    "Duplicate state entry: " + entry.scope().id() + " " + entry.identifier());
        }
    }

    public Optional<StateEntry<?>> get(StateScope scope, ResourceLocation id) {
        return Optional.ofNullable(entries.get(new Key(scope, id)));
    }

    public Collection<StateEntry<?>> all() {
        return entries.values().stream()
                .sorted(
                        Comparator.comparing((StateEntry<?> e) -> e.identifier().toString())
                                .thenComparing(e -> e.scope().id()))
                .toList();
    }

    private record Key(StateScope scope, ResourceLocation id) {}
}
