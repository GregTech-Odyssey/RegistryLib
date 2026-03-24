package com.gto.registrylib.datagen;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.generator.RegistryLibBlockModelGenerator;
import com.gto.registrylib.datagen.generator.RegistryLibItemModelGenerator;
import com.gto.registrylib.datagen.loot.RegistryLibLootTableProvider;
import com.gto.registrylib.datagen.provider.RegistryLibAdvancementProvider;
import com.gto.registrylib.datagen.provider.RegistryLibDatapackProvider;
import com.gto.registrylib.datagen.provider.RegistryLibEnchantmentTagsProvider;
import com.gto.registrylib.datagen.provider.RegistryLibGeneralResourceProvider;
import com.gto.registrylib.datagen.provider.RegistryLibItemTagsProvider;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.datagen.provider.RegistryLibModelProvider;
import com.gto.registrylib.datagen.provider.RegistryLibProvider;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeRunner;
import com.gto.registrylib.datagen.provider.RegistryLibTagsProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface ProviderType<T extends RegistryLibProvider> extends GeneratorType<T> {

    ProviderType NULL = unusedContext -> null;

    // SERVER DATA
    ProviderType<RegistryLibRecipeRunner> RECIPE = registerServerData("recipe", RegistryLibRecipeRunner::new);
    ProviderType<RegistryLibLootTableProvider> LOOT = registerServerData("loot", RegistryLibLootTableProvider::new);
    ProviderType<RegistryLibAdvancementProvider> ADVANCEMENT = registerServerData("advancement", RegistryLibAdvancementProvider::new);
    ProviderType<RegistryLibDatapackProvider> DATAPACK_REGISTRIES = registerServerData("datapack_registries", RegistryLibDatapackProvider::new);
    ProviderType<RegistryLibEnchantmentTagsProvider> ENCHANTMENT_TAGS = registerTag(
            "tags/enchantment",
            Registries.ENCHANTMENT,
            c -> new RegistryLibEnchantmentTagsProvider(
                    c.parent(), c.type(), c.output(), c.provider()));
    ProviderType<RegistryLibTagsProvider.IntrinsicImpl<Block>> BLOCK_TAGS = registerIntrinsicTag(
            "tags/block", "blocks", Registries.BLOCK, block -> block.builtInRegistryHolder().key());
    ProviderType<RegistryLibItemTagsProvider> ITEM_TAGS = registerTag(
            "tags/item",
            Registries.ITEM,
            c -> new RegistryLibItemTagsProvider(
                    c.parent(),
                    c.type(),
                    "items",
                    c.output(),
                    c.provider(),
                    c.get(BLOCK_TAGS).contentsGetter()));
    ProviderType<RegistryLibTagsProvider.IntrinsicImpl<Fluid>> FLUID_TAGS = registerIntrinsicTag(
            "tags/fluid", "fluids", Registries.FLUID, fluid -> fluid.builtInRegistryHolder().key());
    ProviderType<RegistryLibTagsProvider.IntrinsicImpl<EntityType<?>>> ENTITY_TAGS = registerIntrinsicTag(
            "tags/entity",
            "entity_types",
            Registries.ENTITY_TYPE,
            entityType -> entityType.builtInRegistryHolder().key());

    // CLIENT DATA
    ProviderType<RegistryLibModelProvider> MODEL = registerClientProvider(
            "model", () -> c -> new RegistryLibModelProvider(c.parent(), c.output()));
    ProviderType<RegistryLibLangProvider> LANG = registerClientProvider(
            "lang", () -> c -> new RegistryLibLangProvider(c.parent(), c.output()));
    ProviderType<RegistryLibGeneralResourceProvider> GENERAL_RESOURCE = registerClientProvider(
            "general_resource",
            () -> c -> new RegistryLibGeneralResourceProvider(c.parent(), c.output()));

    GeneratorType<RegistryLibBlockModelGenerator> BLOCKSTATE = MODEL.createGenerator("blockstate");
    GeneratorType<RegistryLibItemModelGenerator> ITEM_MODEL = MODEL.createGenerator("item_model");

    record Context<T extends RegistryLibProvider>(
                                                  ProviderType<T> type,
                                                  RegistryCore parent,
                                                  GatherDataEvent event,
                                                  Map<ProviderType<?>, RegistryLibProvider> existing,
                                                  PackOutput output,
                                                  CompletableFuture<HolderLookup.Provider> provider) {

        @SuppressWarnings("unchecked")
        public <R extends RegistryLibProvider> R get(ProviderType<R> other) {
            return (R) existing().get(other);
        }
    }

    T create(Context<T> context);

    default <R> GeneratorType<R> createGenerator(String type) {
        return new GeneratorType<>() {

            public String toString() {
                return type;
            }
        };
    }

    interface SimpleServerDataFactory<T extends RegistryLibProvider> extends ProviderType<T> {

        T create(
                 RegistryCore parent, PackOutput output, CompletableFuture<HolderLookup.Provider> provider);

        @Override
        default T create(Context<T> context) {
            return create(context.parent(), context.output(), context.provider());
        }

        default ProviderType<T> asProvider() {
            return this;
        }
    }

    @Nonnull
    static <T extends RegistryLibProvider> ProviderType<T> registerServerData(
                                                                              String name, SimpleServerDataFactory<T> factory) {
        return registerProvider(name, factory.asProvider());
    }

    @Nonnull
    static <T extends RegistryLibProvider> ProviderType<T> registerProvider(
                                                                            String name, ProviderType<T> type) {
        RegistryLibDataProvider.TYPES.put(name, type);
        return type;
    }

    @Nonnull
    static <T extends RegistryLibProvider> ProviderType<T> registerClientProvider(
                                                                                  String name, Supplier<ProviderType<T>> supplier) {
        if (!DatagenModLoader.isRunningDataGen()) return NULL;
        var type = supplier.get();
        RegistryLibDataProvider.TYPES.put(name, type);
        return type;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    static <T, R extends RegistryLibTagsProvider<T>> ProviderType<R> registerTag(
                                                                                 String name, ResourceKey<? extends Registry<T>> key, ProviderType<R> type) {
        if (RegistryLibDataProvider.TAG_TYPES.containsKey(key)) {
            return (ProviderType<R>) RegistryLibDataProvider.TAG_TYPES.get(key);
        }
        RegistryLibDataProvider.TAG_TYPES.put(key, type);
        RegistryLibDataProvider.TYPES.put(name, type);
        return type;
    }

    @Nonnull
    static <T> ProviderType<RegistryLibTagsProvider.IntrinsicImpl<T>> registerIntrinsicTag(
                                                                                           String providerName,
                                                                                           String typeName,
                                                                                           ResourceKey<? extends Registry<T>> registry,
                                                                                           Function<T, ResourceKey<T>> keyExtractor) {
        return registerTag(
                providerName,
                registry,
                c -> new RegistryLibTagsProvider.IntrinsicImpl<>(
                        c.parent(), c.type(), typeName, c.output(), registry, c.provider(), keyExtractor));
    }

    static <T extends RegistryLibProvider> T create(
                                                    ProviderType<T> type,
                                                    RegistryCore parent,
                                                    GatherDataEvent event,
                                                    Map<ProviderType<?>, RegistryLibProvider> existing,
                                                    CompletableFuture<HolderLookup.Provider> provider) {
        return type.create(
                new Context<>(
                        type, parent, event, existing, event.getGenerator().getPackOutput(), provider));
    }
}
