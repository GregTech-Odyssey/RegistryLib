package com.gto.registrylib.util.entry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import org.jetbrains.annotations.Nullable;

public class EntityEntry<T extends Entity>
                        extends RegistryEntry<EntityType<?>, EntityType<T>> {

    public EntityEntry(ResourceKey<EntityType<?>> key) {
        super(key);
    }

    public boolean is(@Nullable Entity entity) {
        return entity != null && entity.getType() == value;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityEntry<T> cast(
                                                         RegistryEntry<EntityType<?>, EntityType<T>> entry) {
        return RegistryEntry.cast(EntityEntry.class, entry);
    }
}
