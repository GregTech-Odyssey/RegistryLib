package com.gto.registrylib.builders;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.client.Client;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.loot.RegistryLibEntityLootTables;
import com.gto.registrylib.datagen.loot.RegistryLibLootTableProvider.LootType;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.DistExecutor;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.entry.EntityEntry;
import com.gto.registrylib.util.entry.RegistryEntry;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddSpawnsBiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class EntityBuilder<T extends Entity, P>
                          extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> {

    public static <T extends Entity, P> EntityBuilder<T, P> create(
                                                                   RegistryCore owner,
                                                                   P parent,
                                                                   String name,
                                                                   EntityType.EntityFactory<T> factory,
                                                                   MobCategory category) {
        return new EntityBuilder<>(owner, parent, name, factory, category).defaultLang();
    }

    private final EntityType.EntityFactory<T> factory;
    private final MobCategory category;
    private Function<EntityType.Builder<T>, EntityType.Builder<T>> builderCallback = FunctionUtil.identityFn();
    private Supplier<AttributeSupplier.Builder> attributesFactory;

    protected EntityBuilder(
                            RegistryCore core,
                            P parent,
                            String name,
                            EntityType.EntityFactory<T> factory,
                            MobCategory category) {
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
    public EntityBuilder<T, P> attributes(@NotNull Supplier<AttributeSupplier.Builder> attributes) {
        this.attributesFactory = attributes;
        return this;
    }

    @StandardAPI
    @SuppressWarnings("rawtypes")
    public EntityBuilder<T, P> renderer(@NotNull Supplier<EntityRendererProvider> renderer) {
        Supplier supplier = getValueSupplier();
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> Client.registerEntityRenderer(supplier, renderer.get()));
        return this;
    }

    @StandardAPI
    public EntityBuilder<T, P> spawnEgg(
                                        @NotNull Consumer<ItemBuilder<SpawnEggItem, EntityBuilder<T, P>>> consumer) {
        var supplier = getValueSupplier();
        var eggBuilder = core.<SpawnEggItem, EntityBuilder<T, P>>item(
                this,
                name + "_spawn_egg",
                p -> new SpawnEggItem(p.spawnEgg(supplier.get())),
                false);
        consumer.accept(eggBuilder);
        eggBuilder.build();
        return this;
    }

    @SyntaxSugar("spawnEgg(FunctionUtil.noOpConsumer())")
    public EntityBuilder<T, P> spawnEgg() {
        return spawnEgg(FunctionUtil.noOpConsumer());
    }

    // === Loot ===

    @StandardAPI
    public EntityBuilder<T, P> loot(
                                    @NotNull BiConsumer<RegistryLibEntityLootTables, EntityType<T>> cons) {
        if (!core.doDatagen()) return this;
        return setData(
                ProviderType.LOOT,
                prov -> prov.addLootAction(LootType.ENTITY, tb -> cons.accept(tb, getValue())));
    }

    // === Spawn Placement ===

    @StandardAPI
    public EntityBuilder<T, P> spawnPlacement(
                                              @NotNull SpawnPlacementType placementType,
                                              @NotNull Heightmap.Types heightmap,
                                              @NotNull SpawnPlacements.SpawnPredicate<T> predicate) {
        core.registerSpawnPlacement(getValueSupplier(), placementType, heightmap, predicate);
        return this;
    }

    // === Biome Spawn ===

    /**
     * 将此实体添加到指定生物群系标签的自然生成列表中。
     *
     * <p>
     * 仅调用 {@link #spawnPlacement} 只会注册生成<b>条件</b>（在哪种地形可以生成）， 但不会让实体实际出现在任何生物群系的刷怪列表中。本方法通过
     * NeoForge 的 {@code AddSpawnsBiomeModifier} 数据包注册来补全这一环节。
     *
     * @param biomeTag 生物群系标签（如 {@code BiomeTags.IS_OVERWORLD}）
     * @param weight   生成权重（值越大，在同分类实体中被选中的概率越大）
     * @param minCount 每次生成的最小数量
     * @param maxCount 每次生成的最大数量
     */
    @StandardAPI
    public EntityBuilder<T, P> spawnBiomes(
                                           @NotNull TagKey<Biome> biomeTag, int weight, int minCount, int maxCount) {
        if (!core.doDatagen()) return this;
        final TagKey<Biome> capturedTag = biomeTag;
        final int capturedWeight = weight;
        final int capturedMin = minCount;
        final int capturedMax = maxCount;
        core.getDataGenInitializer()
                .add(
                        NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                        ctx -> {
                            var biomes = ctx.lookup(Registries.BIOME).getOrThrow(capturedTag);
                            var spawner = new Weighted<>(
                                    new SpawnerData(getValue(), capturedMin, capturedMax), capturedWeight);
                            var modifier = AddSpawnsBiomeModifier.singleSpawn(biomes, spawner);
                            var key = ResourceKey.create(
                                    NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                                    Identifier.fromNamespaceAndPath(core.getModid(), name + "_spawn"));
                            ctx.register(key, modifier);
                        });
        return this;
    }

    // === Lang ===

    @SyntaxSugar("lang(t -> t.getDescriptionId())")
    public EntityBuilder<T, P> defaultLang() {
        return lang(EntityType::getDescriptionId);
    }

    @SyntaxSugar("lang(t -> t.getDescriptionId(), name)")
    public EntityBuilder<T, P> lang(@NotNull String name) {
        Function<EntityType<T>, String> keyFn = EntityType::getDescriptionId;
        return lang(keyFn, name);
    }

    @SyntaxSugar("lang(type, t -> t.getDescriptionId(), name)")
    public EntityBuilder<T, P> lang(
                                    @NotNull ProviderType<? extends RegistryLibLangProvider> type, @NotNull String name) {
        return lang(type, EntityType::getDescriptionId, name);
    }

    // === Tags ===

    @SafeVarargs
    @StandardAPI
    public final EntityBuilder<T, P> addTag(@NotNull TagKey<EntityType<?>>... tags) {
        return addTag(ProviderType.ENTITY_TAGS, false, tags);
    }

    // === Registration ===

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

    @Override
    public EntityEntry<T> register() {
        if (attributesFactory != null) {
            core.registerEntityAttributes(getValueSupplier(), attributesFactory);
        }
        return (EntityEntry<T>) super.register();
    }
}
