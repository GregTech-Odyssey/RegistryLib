package com.gto.registrylib.builders;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.client.Client;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.DistExecutor;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.entry.EntityEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.api.distmarker.Dist;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

public class EntityBuilder<T extends Entity, P>
                          extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> {

    public static <T extends Entity, P> EntityBuilder<T, P> create(
                                                                   RegistryCore owner, P parent, String name,
                                                                   EntityType.EntityFactory<T> factory, MobCategory category) {
        return new EntityBuilder<>(owner, parent, name, factory, category).defaultLang();
    }

    private final EntityType.EntityFactory<T> factory;
    private final MobCategory category;
    private Function<EntityType.Builder<T>, EntityType.Builder<T>> builderCallback = FunctionUtil.identityFn();
    private Supplier<AttributeSupplier.Builder> attributesFactory;

    protected EntityBuilder(
                            RegistryCore core, P parent, String name,
                            EntityType.EntityFactory<T> factory, MobCategory category) {
        super(core, parent, name, Registries.ENTITY_TYPE);
        this.factory = factory;
        this.category = category;
    }

    // === Configuration ===

    @StandardAPI
    public EntityBuilder<T, P> properties(@NotNull UnaryOperator<EntityType.Builder<T>> func) {
        builderCallback = builderCallback.andThen(func);
        return this;
    }

    @SyntaxSugar("properties(b -> b.sized(width, height))")
    public EntityBuilder<T, P> sized(float width, float height) {
        return properties(b -> b.sized(width, height));
    }

    @SyntaxSugar("properties(b -> b.clientTrackingRange(range))")
    public EntityBuilder<T, P> clientTrackingRange(int range) {
        return properties(b -> b.clientTrackingRange(range));
    }

    @SyntaxSugar("properties(b -> b.updateInterval(interval))")
    public EntityBuilder<T, P> updateInterval(int interval) {
        return properties(b -> b.updateInterval(interval));
    }

    @SyntaxSugar("properties(b -> b.fireImmune())")
    public EntityBuilder<T, P> fireImmune() {
        return properties(EntityType.Builder::fireImmune);
    }

    @SyntaxSugar("properties(b -> b.noSummon())")
    public EntityBuilder<T, P> noSummon() {
        return properties(EntityType.Builder::noSummon);
    }

    @SyntaxSugar("properties(b -> b.noSave())")
    public EntityBuilder<T, P> noSave() {
        return properties(EntityType.Builder::noSave);
    }

    @StandardAPI
    @SuppressWarnings("unchecked")
    public EntityBuilder<T, P> attributes(@Nonnull Supplier<AttributeSupplier.Builder> attributes) {
        this.attributesFactory = attributes;
        return this;
    }

    @StandardAPI
    @SuppressWarnings("rawtypes")
    public EntityBuilder<T, P> renderer(
                                        @Nonnull Supplier<EntityRendererProvider> renderer) {
        Supplier supplier = valueSupplier;
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> Client.registerEntityRenderer(supplier, renderer.get()));
        return this;
    }

    @StandardAPI
    public EntityBuilder<T, P> spawnEgg(
                                        @Nonnull Consumer<ItemBuilder<SpawnEggItem, EntityBuilder<T, P>>> consumer) {
        var eggBuilder = core.<SpawnEggItem, EntityBuilder<T, P>>item(
                this, name + "_spawn_egg",
                p -> new SpawnEggItem(p.spawnEgg(valueSupplier.get())), false);
        consumer.accept(eggBuilder);
        eggBuilder.build();
        return this;
    }

    @SyntaxSugar("spawnEgg(FunctionUtil.noOpConsumer())")
    public EntityBuilder<T, P> spawnEgg() {
        return spawnEgg(FunctionUtil.noOpConsumer());
    }

    // === Lang ===

    @SyntaxSugar("lang(t -> t.getDescriptionId())")
    public EntityBuilder<T, P> defaultLang() {
        return lang(t -> t.getDescriptionId());
    }

    @SyntaxSugar("lang(t -> t.getDescriptionId(), name)")
    public EntityBuilder<T, P> lang(@NotNull String name) {
        Function<EntityType<T>, String> keyFn = EntityType::getDescriptionId;
        return lang(keyFn, name);
    }

    @SyntaxSugar("lang(type, t -> t.getDescriptionId(), name)")
    public EntityBuilder<T, P> lang(
                                    @Nonnull ProviderType<? extends RegistryLibLangProvider> type, @Nonnull String name) {
        return lang(type, t -> t.getDescriptionId(), name);
    }

    // === Tags ===

    @SafeVarargs
    @StandardAPI
    public final EntityBuilder<T, P> addTag(@NotNull TagKey<EntityType<?>>... tags) {
        return addTag(ProviderType.ENTITY_TAGS, false, tags);
    }

    // === Registration ===

    @SuppressWarnings("unchecked")
    @Override
    protected EntityType<T> createEntry(ResourceKey<EntityType<?>> key) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, category);
        builder = builderCallback.apply(builder);
        return builder.build(key);
    }

    @Override
    protected RegistryEntry<EntityType<?>, EntityType<T>> createEntryWrapper(
                                                                             ResourceKey<EntityType<?>> key) {
        return new EntityEntry<>(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityEntry<T> register() {
        if (attributesFactory != null) {
            core.registerEntityAttributes(valueSupplier, attributesFactory);
        }
        return (EntityEntry<T>) super.register();
    }
}
