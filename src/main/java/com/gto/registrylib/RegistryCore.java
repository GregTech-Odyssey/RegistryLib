package com.gto.registrylib;

import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.builders.BlockBuilder;
import com.gto.registrylib.builders.BlockEntityBuilder;
import com.gto.registrylib.builders.EnchantmentBuilder;
import com.gto.registrylib.builders.EntityBuilder;
import com.gto.registrylib.builders.FluidBuilder;
import com.gto.registrylib.builders.ItemBuilder;
import com.gto.registrylib.builders.NoConfigBuilder;
import com.gto.registrylib.builders.RecipeTypeBuilder;
import com.gto.registrylib.composite.ComponentItem;
import com.gto.registrylib.composite.IComponentItem;
import com.gto.registrylib.datagen.DataProviderInitializer;
import com.gto.registrylib.datagen.GeneratorType;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.RegistryLibDataProvider;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.util.CreativeModeTabModifier;
import com.gto.registrylib.util.DebugMarkers;
import com.gto.registrylib.util.Environment;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.Lazy;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylib.util.map.MultiMap;
import com.gto.registrylib.util.map.NestedMap;
import com.gto.registrylibtest.builder.ModFluidBuilder;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.RegisterEvent;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class RegistryCore {

    private static final ConcurrentSkipListSet<RegistryCore> REGISTRY_CORES = new ConcurrentSkipListSet<>(
            Comparator.comparingInt(RegistryCore::priority).thenComparing(RegistryCore::getModid));

    private static final ConcurrentHashMap<String, RegistryCore> CORES_BY_MODID = new ConcurrentHashMap<>();
    private static final Logger log = RegistryLib.LOGGER;

    private final NestedMap<ResourceKey<? extends Registry<?>>, String, RegistryEntry<?, ?>> registryEntry = NestedMap.createIdentity(LinkedHashMap::new);
    private final MultiMap<ResourceKey<? extends Registry<?>>, Registration<?, ?>> registrations = MultiMap.createIdentity(ArrayList::new);
    private final MultiMap<ResourceKey<? extends Registry<?>>, Runnable> afterRegisterCallbacks = MultiMap.createIdentity(ArrayList::new);
    private final Set<ResourceKey<? extends Registry<?>>> completedRegistrations = new ReferenceOpenHashSet<>();

    private final MultiMap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> creativeModeTabModifiers = MultiMap.createIdentity(ArrayList::new);
    private final List<Pair<Supplier<EntityType<?>>, Supplier<AttributeSupplier.Builder>>> entityAttributes = new ArrayList<>();

    private final NestedMap<GeneratorType<?>, Pair<ResourceKey<?>, String>, Consumer<?>> dataGensByEntry = NestedMap.createIdentity(HashMap::new);
    private final MultiMap<GeneratorType<?>, Consumer<?>> dataGens = MultiMap.createIdentity(ReferenceOpenHashSet::new);

    @Getter
    protected ResourceKey<CreativeModeTab> defaultCreativeModeTab = null;

    @Getter
    private final String modid;

    private boolean skipErrors;

    // === Constructor + Factory ===

    protected RegistryCore(String modid) {
        this.modid = modid;
        if (doDatagen()) {
            ModList.get()
                    .getModContainerById(modid)
                    .ifPresent(c -> c.getEventBus().addListener(this::onGatherData));
        }
        RegistryCore existing = CORES_BY_MODID.putIfAbsent(modid, this);
        if (existing != null) {
            throw new IllegalStateException("Duplicate RegistryCore for mod: " + modid);
        }
        REGISTRY_CORES.add(this);
    }

    public static RegistryCore create(String modid) {
        return new RegistryCore(modid);
    }

    public static Optional<RegistryCore> getCoreByModId(String modId) {
        return Optional.ofNullable(CORES_BY_MODID.get(modId));
    }

    public boolean doDatagen() {
        return Environment.isDatagen;
    }

    public int priority() {
        return 0;
    }

    @Nullable
    private RegistryLibDataProvider provider;

    // === Entry Access ===
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<R, T> get(
                                                    String name, ResourceKey<? extends Registry<R>> type) {
        return (RegistryEntry<R, T>) this.registryEntry.get(type, name);
    }

    @SuppressWarnings("unchecked")
    public <R, T extends R> Collection<RegistryEntry<R, T>> getAll(
                                                                   ResourceKey<? extends Registry<R>> type) {
        return (Collection) registryEntry.get(type).values();
    }

    // === Callback Management ===

    public <R> void addRegisterCallback(
                                        ResourceKey<? extends Registry<R>> registryType, Runnable callback) {
        afterRegisterCallbacks.put(registryType, callback);
    }

    public <R> boolean isRegistered(ResourceKey<? extends Registry<R>> registryType) {
        return completedRegistrations.contains(registryType);
    }

    // === Data Generation ===

    public <P> Optional<P> getDataProvider(GeneratorType<P> type) {
        RegistryLibDataProvider provider = this.provider;
        if (provider != null) return provider.getSubProvider(type);
        throw new IllegalStateException("Cannot get data provider before datagen is started");
    }

    public <P> void setDataGenerator(
                                     String name,
                                     ResourceKey<?> key,
                                     GeneratorType<? extends P> type,
                                     Consumer<? extends P> cons) {
        if (!doDatagen()) return;
        @SuppressWarnings("null")
        Consumer<?> existing = dataGensByEntry.put(type, Pair.of(key, name), cons);
        if (existing != null) {
            dataGens.remove(type, existing);
        }
        addDataGenerator(type, cons);
    }

    public <T> void addDataGenerator(GeneratorType<? extends T> type, Consumer<? extends T> cons) {
        if (doDatagen()) {
            if (provider != null)
                throw new IllegalStateException(
                        "Cannot add data generator after construction of root generator");
            dataGens.put(type, cons);
        }
    }

    @Nullable
    private DataProviderInitializer initializer;

    public DataProviderInitializer getDataGenInitializer() {
        if (initializer == null) {
            initializer = new DataProviderInitializer();
        }
        return initializer;
    }

    // === Lang ===

    private final Supplier<List<Pair<String, String>>> extraLang = Lazy.of(
            () -> {
                final List<Pair<String, String>> ret = new ArrayList<>();
                addDataGenerator(
                        ProviderType.LANG, prov -> ret.forEach(p -> prov.add(p.getKey(), p.getValue())));
                return ret;
            });

    public MutableComponent addLang(String type, Identifier id, String localizedName) {
        return addRawLang(id.toLanguageKey(type), localizedName);
    }

    public MutableComponent addLang(String type, Identifier id, String suffix, String localizedName) {
        return addRawLang(id.toLanguageKey(type) + "." + suffix, localizedName);
    }

    public MutableComponent addRawLang(String key, String value) {
        if (doDatagen()) {
            extraLang.get().add(Pair.of(key, value));
        }
        return Component.translatable(key);
    }

    // === Data Gen Execution ===
    @SuppressWarnings("unchecked")
    public <T> void genData(GeneratorType<? extends T> type, T gen) {
        if (!doDatagen()) return;
        if (provider != null) {
            provider.putSubProvider(type, gen);
        }
        dataGens
                .get(type)
                .forEach(
                        cons -> {
                            try {
                                ((Consumer<T>) cons).accept(gen);
                            } catch (Exception e) {
                                if (skipErrors) {
                                    log.error(e);
                                } else {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
    }

    // === Configuration ===

    public RegistryCore skipErrors(boolean skipErrors) {
        if (skipErrors && Environment.isProd) {
            log.error("Ignoring skipErrors(true) as this is not a development environment!");
        } else {
            this.skipErrors = skipErrors;
        }
        return this;
    }

    public void defaultCreativeTab(ResourceKey<CreativeModeTab> creativeModeTab) {
        defaultCreativeModeTab = creativeModeTab;
    }

    public void modifyCreativeModeTab(
                                      ResourceKey<CreativeModeTab> creativeModeTab, Consumer<CreativeModeTabModifier> modifier) {
        if (creativeModeTab == CreativeModeTabs.SEARCH)
            throw new RuntimeException("SEARCH is a reserved tab name");
        creativeModeTabModifiers.put(creativeModeTab, modifier);
    }

    // === Core Registration ===

    public <R, T extends R> RegistryEntry<R, T> registry(
                                                         String name,
                                                         ResourceKey<? extends Registry<R>> type,
                                                         List<Consumer<? super T>> callbacks,
                                                         Function<ResourceKey<R>, ? extends T> factory,
                                                         Function<ResourceKey<R>, ? extends RegistryEntry<R, T>> entryFactory) {
        var reg = new Registration<>(
                type, Identifier.fromNamespaceAndPath(modid, name), factory, entryFactory);
        reg.callbacks.addAll(callbacks);
        callbacks.clear();
        registrations.put(type, reg);
        registryEntry.put(type, name, reg.entry);
        return reg.entry;
    }

    // === Builder Factory Methods ===
    // --- Generic ---

    @SyntaxSugar("generic(...).register()")
    public <R, T extends R> RegistryEntry<R, T> simple(
                                                       @NotNull String name,
                                                       @NotNull ResourceKey<Registry<R>> registryType,
                                                       @NotNull Function<ResourceKey<R>, T> factory) {
        return generic(name, registryType, factory).register();
    }

    @StandardAPI
    public <R, T extends R> NoConfigBuilder<R, T, RegistryCore> generic(
                                                                        @NotNull String name,
                                                                        @NotNull ResourceKey<Registry<R>> registryType,
                                                                        @NotNull Function<ResourceKey<R>, T> factory) {
        return new NoConfigBuilder<>(this, this, name, registryType, factory);
    }

    @SyntaxSugar("generic(...).register()")
    public <R, T extends R, P> RegistryEntry<R, T> simple(
                                                          @NotNull P parent,
                                                          @NotNull String name,
                                                          @NotNull ResourceKey<Registry<R>> registryType,
                                                          @NotNull Function<ResourceKey<R>, T> factory) {
        return generic(parent, name, registryType, factory).register();
    }

    @StandardAPI
    public <R, T extends R, P> NoConfigBuilder<R, T, P> generic(
                                                                @NotNull P parent,
                                                                @NotNull String name,
                                                                @NotNull ResourceKey<Registry<R>> registryType,
                                                                @NotNull Function<ResourceKey<R>, T> factory) {
        return new NoConfigBuilder<>(this, parent, name, registryType, factory);
    }

    // --- Items ---

    public <T extends Item, P> ItemBuilder<T, P> item(
                                                      @Nonnull P parent,
                                                      @Nonnull String name,
                                                      @Nonnull Function<Item.Properties, T> factory,
                                                      boolean isComponentItem) {
        return ItemBuilder.create(this, parent, name, factory, isComponentItem);
    }

    @StandardAPI("Returns an ItemBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Item> ItemBuilder<T, RegistryCore> item(
                                                              @Nonnull String name, @Nonnull Function<Item.Properties, T> factory) {
        return item(this, name, factory, false);
    }

    public ItemBuilder<Item, RegistryCore> item(@Nonnull String name) {
        return item(this, name, Item::new, false);
    }

    public <T extends Item & IComponentItem<T>> ItemBuilder<T, RegistryCore> componentItem(
                                                                                           @Nonnull String name, @Nonnull Function<Item.Properties, T> factory) {
        return item(this, name, factory, true);
    }

    public ItemBuilder<ComponentItem, RegistryCore> componentItem(@Nonnull String name) {
        return item(this, name, ComponentItem::new, true);
    }

    // --- Blocks ---

    public <T extends Block, P> BlockBuilder<T, P> block(
                                                         @Nonnull P parent,
                                                         @Nonnull String name,
                                                         @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return BlockBuilder.create(this, parent, name, factory);
    }

    @StandardAPI("Returns a BlockBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Block> BlockBuilder<T, RegistryCore> block(
                                                                 @Nonnull String name, @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    public BlockBuilder<Block, RegistryCore> block(@Nonnull String name) {
        return block(this, name, Block::new);
    }

    // --- Block Entities ---

    public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(
                                                                           @Nonnull P parent,
                                                                           @Nonnull String name,
                                                                           @Nonnull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return BlockEntityBuilder.create(this, parent, name, factory);
    }

    @StandardAPI("Returns a BlockEntityBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends BlockEntity> BlockEntityBuilder<T, RegistryCore> blockEntity(
                                                                                   @Nonnull String name, @Nonnull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return blockEntity(this, name, factory);
    }

    // --- Fluids ---

    protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
                                                                                 @Nonnull P parent, @Nonnull String name, @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return ModFluidBuilder.create(this, parent, name, fluidFactory);
    }

    @StandardAPI("Returns a FluidBuilder for fluent chain configuration. Call .register() to finalise.")
    public FluidBuilder<BaseFlowingFluid.Flowing, RegistryCore> fluid(
                                                                      @Nonnull String name, @Nonnull Identifier stillTexture, @Nonnull Identifier flowingTexture) {
        return fluid(this, name, stillTexture, flowingTexture, BaseFlowingFluid.Flowing::new);
    }

    @StandardAPI("Returns a FluidBuilder with custom FluidFactory for fluent chain configuration. Call .register() to finalise.")
    public <T extends BaseFlowingFluid> FluidBuilder<T, RegistryCore> fluid(
                                                                            @Nonnull String name,
                                                                            @Nonnull Identifier stillTexture,
                                                                            @Nonnull Identifier flowingTexture,
                                                                            @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return fluid(this, name, stillTexture, flowingTexture, fluidFactory);
    }

    @StandardAPI
    public <T extends BaseFlowingFluid, P> FluidBuilder<T, P> fluid(
                                                                    @Nonnull P parent,
                                                                    @Nonnull String name,
                                                                    @Nonnull Identifier stillTexture,
                                                                    @Nonnull Identifier flowingTexture,
                                                                    @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return newFluidBuilder(parent, name, fluidFactory)
                .clientExtension(stillTexture, flowingTexture);
    }

    // --- Group ---

    @StandardAPI
    public Group.Builder group(@NotNull String name) {
        return new Group.Builder(this, name);
    }

    // --- Recipe Types (Simple) ---

    @SuppressWarnings("unchecked")
    @StandardAPI("Registers a RecipeType and returns a typed RegistryEntry. Prefer recipeType(String) builder API.")
    public <T extends net.minecraft.world.item.crafting.Recipe<?>> RegistryEntry<net.minecraft.world.item.crafting.RecipeType<?>, net.minecraft.world.item.crafting.RecipeType<T>> simpleRecipeType(@Nonnull String name) {
        return (RegistryEntry) simple(
                name,
                Registries.RECIPE_TYPE,
                key -> net.minecraft.world.item.crafting.RecipeType.simple(key.identifier()));
    }

    @SuppressWarnings("unchecked")
    @StandardAPI("Registers a RecipeSerializer and returns a typed RegistryEntry. Prefer recipeType(String) builder API.")
    public <T extends net.minecraft.world.item.crafting.Recipe<?>> RegistryEntry<net.minecraft.world.item.crafting.RecipeSerializer<?>, net.minecraft.world.item.crafting.RecipeSerializer<T>> simpleRecipeSerializer(
                                                                                                                                                                                                                      @Nonnull String name,
                                                                                                                                                                                                                      @Nonnull Supplier<net.minecraft.world.item.crafting.RecipeSerializer<T>> factory) {
        return (RegistryEntry) simple(name, Registries.RECIPE_SERIALIZER, key -> factory.get());
    }

    // --- Recipe Types (Builder) ---

    @StandardAPI("Returns a RecipeTypeBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeTypeBuilder<T, RegistryCore> recipeType(@Nonnull String name) {
        return RecipeTypeBuilder.create(this, this, name);
    }

    @StandardAPI
    public <T extends net.minecraft.world.item.crafting.Recipe<?>, P> RecipeTypeBuilder<T, P> recipeType(@Nonnull P parent, @Nonnull String name) {
        return RecipeTypeBuilder.create(this, parent, name);
    }

    /**
     * @deprecated Use {@link #recipeType(String)} instead.
     */
    @Deprecated(forRemoval = true)
    public <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeTypeBuilder<T, RegistryCore> recipe(@Nonnull String name) {
        return recipeType(name);
    }

    /**
     * @deprecated Use {@link #recipeType(Object, String)} instead.
     */
    @Deprecated(forRemoval = true)
    public <T extends net.minecraft.world.item.crafting.Recipe<?>, P> RecipeTypeBuilder<T, P> recipe(
                                                                                                     @Nonnull P parent, @Nonnull String name) {
        return recipeType(parent, name);
    }

    // --- Custom Ingredient Types ---

    /**
     * 注册一个自定义 {@link net.neoforged.neoforge.common.crafting.IngredientType}（仅需 MapCodec）。
     *
     * <p>
     * Registers a custom {@link net.neoforged.neoforge.common.crafting.IngredientType} with just a
     * {@link com.mojang.serialization.MapCodec}. A {@link net.minecraft.network.codec.StreamCodec}
     * will be derived automatically.
     *
     * @param name  the ingredient type registry name
     * @param codec the MapCodec for serializing / deserializing the custom ingredient
     * @return an {@link com.gto.registrylib.util.entry.IngredientTypeEntry} wrapping the registered type
     */
    @SuppressWarnings("unchecked")
    @StandardAPI("Registers a custom IngredientType and returns a typed IngredientTypeEntry.")
    public <T extends net.neoforged.neoforge.common.crafting.ICustomIngredient>
            com.gto.registrylib.util.entry.IngredientTypeEntry<T> ingredientType(
                                                                                  @Nonnull String name,
                                                                                  @Nonnull com.mojang.serialization.MapCodec<T> codec) {
        var entry = (RegistryEntry<net.neoforged.neoforge.common.crafting.IngredientType<?>,
                net.neoforged.neoforge.common.crafting.IngredientType<T>>) (RegistryEntry) simple(
                        name,
                        net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.INGREDIENT_TYPES,
                        key -> new net.neoforged.neoforge.common.crafting.IngredientType<>(codec));
        return new com.gto.registrylib.util.entry.IngredientTypeEntry<>(entry);
    }

    /**
     * 注册一个自定义 {@link net.neoforged.neoforge.common.crafting.IngredientType}（提供 MapCodec 和
     * StreamCodec）。
     *
     * <p>
     * Registers a custom {@link net.neoforged.neoforge.common.crafting.IngredientType} with both a
     * {@link com.mojang.serialization.MapCodec} and a
     * {@link net.minecraft.network.codec.StreamCodec}.
     *
     * @param name        the ingredient type registry name
     * @param codec       the MapCodec
     * @param streamCodec the StreamCodec for network syncing
     * @return an {@link com.gto.registrylib.util.entry.IngredientTypeEntry}
     */
    @SuppressWarnings("unchecked")
    @StandardAPI("Registers a custom IngredientType with explicit StreamCodec.")
    public <T extends net.neoforged.neoforge.common.crafting.ICustomIngredient>
            com.gto.registrylib.util.entry.IngredientTypeEntry<T> ingredientType(
                                                                                  @Nonnull String name,
                                                                                  @Nonnull com.mojang.serialization.MapCodec<T> codec,
                                                                                  @Nonnull net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec) {
        var entry = (RegistryEntry<net.neoforged.neoforge.common.crafting.IngredientType<?>,
                net.neoforged.neoforge.common.crafting.IngredientType<T>>) (RegistryEntry) simple(
                        name,
                        net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.INGREDIENT_TYPES,
                        key -> new net.neoforged.neoforge.common.crafting.IngredientType<>(codec, streamCodec));
        return new com.gto.registrylib.util.entry.IngredientTypeEntry<>(entry);
    }

    // --- Custom Fluid Ingredient Types ---

    /**
     * 注册一个自定义 {@link net.neoforged.neoforge.fluids.crafting.FluidIngredientType}（仅需 MapCodec）。
     *
     * <p>
     * Registers a custom {@link net.neoforged.neoforge.fluids.crafting.FluidIngredientType} with
     * just a {@link com.mojang.serialization.MapCodec}. A
     * {@link net.minecraft.network.codec.StreamCodec} will be derived automatically.
     *
     * @param name  the fluid ingredient type registry name
     * @param codec the MapCodec for serializing / deserializing the custom fluid ingredient
     * @return a {@link com.gto.registrylib.util.entry.FluidIngredientTypeEntry} wrapping the registered type
     */
    @SuppressWarnings("unchecked")
    @StandardAPI("Registers a custom FluidIngredientType and returns a typed FluidIngredientTypeEntry.")
    public <T extends net.neoforged.neoforge.fluids.crafting.FluidIngredient>
            com.gto.registrylib.util.entry.FluidIngredientTypeEntry<T> fluidIngredientType(
                                                                                            @Nonnull String name,
                                                                                            @Nonnull com.mojang.serialization.MapCodec<T> codec) {
        var entry = (RegistryEntry<net.neoforged.neoforge.fluids.crafting.FluidIngredientType<?>,
                net.neoforged.neoforge.fluids.crafting.FluidIngredientType<T>>) (RegistryEntry) simple(
                        name,
                        net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES,
                        key -> new net.neoforged.neoforge.fluids.crafting.FluidIngredientType<>(codec));
        return new com.gto.registrylib.util.entry.FluidIngredientTypeEntry<>(entry);
    }

    /**
     * 注册一个自定义 {@link net.neoforged.neoforge.fluids.crafting.FluidIngredientType}（提供 MapCodec 和
     * StreamCodec）。
     *
     * <p>
     * Registers a custom {@link net.neoforged.neoforge.fluids.crafting.FluidIngredientType} with
     * both a {@link com.mojang.serialization.MapCodec} and a
     * {@link net.minecraft.network.codec.StreamCodec}.
     *
     * @param name        the fluid ingredient type registry name
     * @param codec       the MapCodec
     * @param streamCodec the StreamCodec for network syncing
     * @return a {@link com.gto.registrylib.util.entry.FluidIngredientTypeEntry}
     */
    @SuppressWarnings("unchecked")
    @StandardAPI("Registers a custom FluidIngredientType with explicit StreamCodec.")
    public <T extends net.neoforged.neoforge.fluids.crafting.FluidIngredient>
            com.gto.registrylib.util.entry.FluidIngredientTypeEntry<T> fluidIngredientType(
                                                                                            @Nonnull String name,
                                                                                            @Nonnull com.mojang.serialization.MapCodec<T> codec,
                                                                                            @Nonnull net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec) {
        var entry = (RegistryEntry<net.neoforged.neoforge.fluids.crafting.FluidIngredientType<?>,
                net.neoforged.neoforge.fluids.crafting.FluidIngredientType<T>>) (RegistryEntry) simple(
                        name,
                        net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES,
                        key -> new net.neoforged.neoforge.fluids.crafting.FluidIngredientType<>(codec, streamCodec));
        return new com.gto.registrylib.util.entry.FluidIngredientTypeEntry<>(entry);
    }

    // --- Enchantments (Builder) ---

    @StandardAPI("Returns an EnchantmentBuilder for fluent chain configuration. Call .register() to finalise.")
    public EnchantmentBuilder<RegistryCore> enchantment(@Nonnull String name) {
        return EnchantmentBuilder.create(this, this, name);
    }

    @StandardAPI
    public <P> EnchantmentBuilder<P> enchantment(@Nonnull P parent, @Nonnull String name) {
        return EnchantmentBuilder.create(this, parent, name);
    }

    // --- Entities ---

    public <T extends Entity, P> EntityBuilder<T, P> entity(
                                                            @Nonnull P parent,
                                                            @Nonnull String name,
                                                            @Nonnull EntityType.EntityFactory<T> factory,
                                                            @Nonnull MobCategory category) {
        return EntityBuilder.create(this, parent, name, factory, category);
    }

    @StandardAPI("Returns an EntityBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Entity> EntityBuilder<T, RegistryCore> entity(
                                                                   @Nonnull String name,
                                                                   @Nonnull EntityType.EntityFactory<T> factory,
                                                                   @Nonnull MobCategory category) {
        return entity(this, name, factory, category);
    }

    @SuppressWarnings("unchecked")
    public void registerEntityAttributes(
                                         Supplier<?> entityTypeSupplier,
                                         Supplier<AttributeSupplier.Builder> attributesFactory) {
        entityAttributes.add(Pair.of((Supplier<EntityType<?>>) entityTypeSupplier, attributesFactory));
    }

    // --- Creative Tab ---

    @SyntaxSugar("creativeTab(name, FunctionUtil.noOpConsumer())")
    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, RegistryCore> creativeTab(String name) {
        return creativeTab(name, FunctionUtil.noOpConsumer());
    }

    @StandardAPI
    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, RegistryCore> creativeTab(
                                                                                       String name, Consumer<CreativeModeTab.Builder> config) {
        return this.generic(
                name,
                Registries.CREATIVE_MODE_TAB,
                k -> {
                    var builder = CreativeModeTab.builder()
                            .icon(
                                    () -> getAll(Registries.ITEM).stream()
                                            .findFirst()
                                            .map(ItemEntry::cast)
                                            .map(ItemEntry::asStack)
                                            .orElse(new ItemStack(Items.AIR)))
                            .title(
                                    this.addLang(
                                            "itemGroup",
                                            k.identifier(),
                                            RegistryLibLangProvider.toEnglishName(name)));
                    config.accept(builder);
                    return builder.build();
                });
    }

    static void onRegister(RegisterEvent event) {
        var type = event.getRegistry();
        var key = type.key();
        REGISTRY_CORES.forEach(
                core -> core.registrations
                        .remove(key)
                        .forEach(
                                r -> {
                                    try {
                                        r.register((Registry) type);
                                    } catch (Exception ex) {
                                        String err = "Unexpected error while registering entry " + r.key.identifier() + " to registry " + key.identifier();
                                        if (core.skipErrors) {
                                            log.error(DebugMarkers.REGISTER, err);
                                        } else {
                                            throw new RuntimeException(err, ex);
                                        }
                                    }
                                }));
    }

    static void onRegisterLate(RegisterEvent event) {
        var type = event.getRegistryKey();
        REGISTRY_CORES.forEach(
                core -> {
                    core.afterRegisterCallbacks.remove(type).forEach(Runnable::run);
                    core.completedRegistrations.add(type);
                });
    }

    static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        var modifier = new CreativeModeTabModifier(event);
        REGISTRY_CORES.forEach(
                core -> core.creativeModeTabModifiers
                        .get(event.getTabKey())
                        .forEach(value -> value.accept(modifier)));
    }

    @SuppressWarnings("unchecked")
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        REGISTRY_CORES.forEach(
                core -> {
                    core.entityAttributes.forEach(pair -> {
                        var type = (EntityType<? extends LivingEntity>) pair.getLeft().get();
                        event.put(type, pair.getRight().get().build());
                    });
                    if (!core.registrations.isEmpty()) {
                        log.error("Registry {} has unregistered entries", core.registrations.getMap().keySet());
                    }
                });
    }

    private void onGatherData(GatherDataEvent.Client event) {
        extraLang.get();
        event
                .getGenerator()
                .addProvider(true, provider = new RegistryLibDataProvider(this, modid, event));
    }

    private static final class Registration<R, T extends R> {

        private final ResourceKey<R> key;
        private final RegistryEntry<R, T> entry;
        private final Function<ResourceKey<R>, ? extends T> creator;
        private final List<Consumer<? super T>> callbacks = new ArrayList<>();

        private Registration(
                             ResourceKey<? extends Registry<R>> type,
                             Identifier name,
                             Function<ResourceKey<R>, ? extends T> creator,
                             Function<ResourceKey<R>, ? extends RegistryEntry<R, T>> entryFactory) {
            this.key = ResourceKey.create(type, name);
            this.creator = creator;
            this.entry = entryFactory.apply(this.key);
        }

        @SuppressWarnings("all")
        private void register(Registry<R> registry) {
            T value = creator.apply(key);
            this.entry.bound(value);
            ((WritableRegistry) registry).register(key, value, RegistrationInfo.BUILT_IN);
            callbacks.forEach(c -> c.accept(value));
        }
    }
}
