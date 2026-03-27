package com.gto.registrylib;

import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.builders.BlockBuilder;
import com.gto.registrylib.builders.BlockEntityBuilder;
import com.gto.registrylib.builders.EnchantmentBuilder;
import com.gto.registrylib.builders.EntityBuilder;
import com.gto.registrylib.builders.FluidBuilder;
import com.gto.registrylib.builders.ItemBuilder;
import com.gto.registrylib.builders.RecipeTypeBuilder;
import com.gto.registrylib.composite.ComponentItem;
import com.gto.registrylib.composite.IComponentItem;
import com.gto.registrylib.datagen.DataProviderInitializer;
import com.gto.registrylib.datagen.GeneratorType;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.datagen.RegistryLibDataProvider;
import com.gto.registrylib.datagen.provider.RegistryLibLangProvider;
import com.gto.registrylib.datagen.provider.RegistryLibRecipeProvider;
import com.gto.registrylib.util.CreativeModeTabModifier;
import com.gto.registrylib.util.DebugMarkers;
import com.gto.registrylib.util.Environment;
import com.gto.registrylib.util.FunctionUtil;
import com.gto.registrylib.util.Lazy;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.entry.RecipeTypeEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylib.util.map.MultiMap;
import com.gto.registrylib.util.map.NestedMap;
import com.gto.registrylib.util.registry.ListRegistry;
import com.gto.registrylibtest.builder.ModFluidBuilder;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private final ListRegistry<Pair<Supplier<EntityType<?>>, Supplier<AttributeSupplier.Builder>>> entityAttributes = new ListRegistry<>();
    private final ListRegistry<Consumer<RegisterSpawnPlacementsEvent>> spawnPlacements = new ListRegistry<>();

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

    @StandardAPI
    public <R, T extends R> RegistryEntry<R, T> registry(
                                                         String name,
                                                         ResourceKey<? extends Registry<R>> registryType,
                                                         List<Consumer<? super T>> callbacks,
                                                         Function<ResourceKey<R>, ? extends T> factory,
                                                         Function<ResourceKey<R>, ? extends RegistryEntry<R, T>> entryFactory) {
        var reg = new Registration<>(
                registryType,
                Identifier.fromNamespaceAndPath(modid, name),
                factory,
                entryFactory,
                callbacks);
        registrations.put(registryType, reg);
        registryEntry.put(registryType, name, reg.entry);
        return reg.entry;
    }

    @SyntaxSugar("registry(...)")
    public <R, T extends R, E extends RegistryEntry<R, T>> E registry(
                                                                      String name,
                                                                      ResourceKey<? extends Registry<R>> registryType,
                                                                      Function<ResourceKey<R>, ? extends T> factory,
                                                                      Function<ResourceKey<R>, E> entryFactory) {
        return (E) registry(name, registryType, Collections.emptyList(), factory, entryFactory);
    }

    @SyntaxSugar("registry(...)")
    public <R, T extends R> RegistryEntry<R, T> registry(
                                                         @NotNull String name,
                                                         @NotNull ResourceKey<Registry<R>> registryType,
                                                         @NotNull Function<ResourceKey<R>, T> factory) {
        return registry(name, registryType, Collections.emptyList(), factory, RegistryEntry::new);
    }

    @SyntaxSugar("registry(...)")
    public <R, T extends R> T registry(
                                       @NotNull String name, @NotNull T value, @NotNull ResourceKey<Registry<R>> registryType) {
        registry(name, registryType, Collections.emptyList(), k -> value, RegistryEntry::new);
        return value;
    }

    // === Builder Factory Methods ===
    // --- Items ---

    @StandardAPI
    public <T extends Item, P> ItemBuilder<T, P> item(
                                                      @NotNull P parent,
                                                      @NotNull String name,
                                                      @NotNull Function<Item.Properties, T> factory,
                                                      boolean isComponentItem) {
        return ItemBuilder.create(this, parent, name, factory, isComponentItem);
    }

    @StandardAPI("Returns an ItemBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Item> ItemBuilder<T, RegistryCore> item(
                                                              @NotNull String name, @NotNull Function<Item.Properties, T> factory) {
        return item(this, name, factory, false);
    }

    public ItemBuilder<Item, RegistryCore> item(@NotNull String name) {
        return item(this, name, Item::new, false);
    }

    public <T extends Item & IComponentItem<T>> ItemBuilder<T, RegistryCore> componentItem(
                                                                                           @NotNull String name, @NotNull Function<Item.Properties, T> factory) {
        return item(this, name, factory, true);
    }

    public ItemBuilder<ComponentItem, RegistryCore> componentItem(@NotNull String name) {
        return item(this, name, ComponentItem::new, true);
    }

    // --- Blocks ---

    public <T extends Block, P> BlockBuilder<T, P> block(
                                                         @NotNull P parent,
                                                         @NotNull String name,
                                                         @NotNull Function<BlockBehaviour.Properties, T> factory) {
        return BlockBuilder.create(this, parent, name, factory);
    }

    @StandardAPI("Returns a BlockBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Block> BlockBuilder<T, RegistryCore> block(
                                                                 @NotNull String name, @NotNull Function<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    public BlockBuilder<Block, RegistryCore> block(@NotNull String name) {
        return block(this, name, Block::new);
    }

    // --- Block Entities ---

    public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(
                                                                           @NotNull P parent,
                                                                           @NotNull String name,
                                                                           @NotNull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return BlockEntityBuilder.create(this, parent, name, factory);
    }

    @StandardAPI("Returns a BlockEntityBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends BlockEntity> BlockEntityBuilder<T, RegistryCore> blockEntity(
                                                                                   @NotNull String name, @NotNull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return blockEntity(this, name, factory);
    }

    // --- Fluids ---

    protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
                                                                                 @NotNull P parent, @NotNull String name, @NotNull FluidBuilder.FluidFactory<T> fluidFactory) {
        return ModFluidBuilder.create(this, parent, name, fluidFactory);
    }

    @StandardAPI("Returns a FluidBuilder for fluent chain configuration. Call .register() to finalise.")
    public FluidBuilder<BaseFlowingFluid.Flowing, RegistryCore> fluid(
                                                                      @NotNull String name, @NotNull Identifier stillTexture, @NotNull Identifier flowingTexture) {
        return fluid(this, name, stillTexture, flowingTexture, BaseFlowingFluid.Flowing::new);
    }

    @StandardAPI("Returns a FluidBuilder with custom FluidFactory for fluent chain configuration. Call .register() to finalise.")
    public <T extends BaseFlowingFluid> FluidBuilder<T, RegistryCore> fluid(
                                                                            @NotNull String name,
                                                                            @NotNull Identifier stillTexture,
                                                                            @NotNull Identifier flowingTexture,
                                                                            @NotNull FluidBuilder.FluidFactory<T> fluidFactory) {
        return fluid(this, name, stillTexture, flowingTexture, fluidFactory);
    }

    @StandardAPI
    public <T extends BaseFlowingFluid, P> FluidBuilder<T, P> fluid(
                                                                    @NotNull P parent,
                                                                    @NotNull String name,
                                                                    @NotNull Identifier stillTexture,
                                                                    @NotNull Identifier flowingTexture,
                                                                    @NotNull FluidBuilder.FluidFactory<T> fluidFactory) {
        return newFluidBuilder(parent, name, fluidFactory)
                .clientExtension(stillTexture, flowingTexture);
    }

    // --- Group ---

    @StandardAPI
    public Group.Builder group(@NotNull String name) {
        return new Group.Builder(this, name);
    }

    // --- Recipe Types (Simple) ---

    @StandardAPI()
    public <T extends Recipe<?>> RecipeType<T> simpleRecipeType(@NotNull String name) {
        return registry(
                name,
                RecipeType.simple(Identifier.fromNamespaceAndPath(getModid(), name)),
                Registries.RECIPE_TYPE);
    }

    @StandardAPI()
    public <T extends Recipe<?>> RecipeSerializer<T> simpleRecipeSerializer(
                                                                            @NotNull String name,
                                                                            MapCodec<T> codec,
                                                                            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return registry(name, new RecipeSerializer<>(codec, streamCodec), Registries.RECIPE_SERIALIZER);
    }

    // --- Recipe Types (Builder) ---

    @StandardAPI("Returns a RecipeTypeBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Recipe<?>> RecipeTypeBuilder<T, RegistryCore> recipeType(@NotNull String name) {
        return RecipeTypeBuilder.create(this, this, name);
    }

    @StandardAPI
    public <T extends Recipe<?>, P> RecipeTypeBuilder<T, P> recipeType(
                                                                       @NotNull P parent, @NotNull String name) {
        return RecipeTypeBuilder.create(this, parent, name);
    }

    // --- Add Recipes (standalone, no RecipeType registration) ---

    /**
     * 向数据生成器添加一条配方。配方 JSON 将生成到 {@code data/<modid>/recipe/<id>.json}。
     *
     * <p>
     * Adds a recipe for datagen. The JSON file will be emitted at {@code
     * data/<modid>/recipe/<id>.json}. This is the low-level API; for custom recipe types registered
     * via {@link #recipeType}, prefer {@link RecipeTypeEntry#addRecipe}.
     *
     * @param id     the recipe path (e.g. {@code "altar/cobblestone_to_stone"})
     * @param recipe the recipe instance
     */
    @StandardAPI("Adds a recipe instance for datagen.")
    public void addRecipe(@NotNull String id, @NotNull Recipe<?> recipe) {
        addRecipe(id, _reg -> recipe);
    }

    /**
     * 向数据生成器添加一条延迟创建的配方。
     *
     * <p>
     * Adds a lazily-created recipe for datagen.
     *
     * @param id             the recipe path
     * @param recipeSupplier a supplier that provides the recipe instance
     */
    @StandardAPI("Adds a lazily-created recipe for datagen.")
    public void addRecipe(@NotNull String id, @NotNull Supplier<? extends Recipe<?>> recipeSupplier) {
        addRecipe(id, _reg -> recipeSupplier.get());
    }

    /**
     * 向数据生成器添加一条需要注册表查找的配方（例如基于 Tag 的 Ingredient）。
     *
     * <p>
     * Adds a recipe for datagen that requires registry lookups.
     *
     * @param id            the recipe path
     * @param recipeFactory a function that receives the registries and produces a recipe
     */
    @StandardAPI("Adds a recipe for datagen that requires registry lookups.")
    public void addRecipe(
                          @NotNull String id,
                          @NotNull Function<net.minecraft.core.HolderLookup.Provider, ? extends Recipe<?>> recipeFactory) {
        if (doDatagen()) {
            final String modid = getModid();
            addDataGenerator(
                    ProviderType.RECIPE,
                    (RegistryLibRecipeProvider prov) -> prov.accept(
                            ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modid, id)),
                            recipeFactory.apply(prov.registries()),
                            null));
        }
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
     * @return a {@link RegistryEntry} wrapping the registered type
     */
    @StandardAPI("Registers a custom IngredientType and returns a RegistryEntry.")
    public <T extends net.neoforged.neoforge.common.crafting.ICustomIngredient> IngredientType<T> ingredientType(@NotNull String name, @NotNull MapCodec<T> codec) {
        return ingredientType(name, codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }

    /**
     * 注册一个自定义 {@link net.neoforged.neoforge.common.crafting.IngredientType}（提供 MapCodec 和
     * StreamCodec）。
     *
     * <p>
     * Registers a custom {@link net.neoforged.neoforge.common.crafting.IngredientType} with both a
     * {@link com.mojang.serialization.MapCodec} and a {@link
     * net.minecraft.network.codec.StreamCodec}.
     *
     * @param name        the ingredient type registry name
     * @param codec       the MapCodec
     * @param streamCodec the StreamCodec for network syncing
     * @return a {@link RegistryEntry} wrapping the registered type
     */
    @StandardAPI("Registers a custom IngredientType with explicit StreamCodec.")
    public <T extends net.neoforged.neoforge.common.crafting.ICustomIngredient> IngredientType<T> ingredientType(
                                                                                                                 @NotNull String name,
                                                                                                                 @NotNull MapCodec<T> codec,
                                                                                                                 @NotNull StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec) {
        return registry(
                name, new IngredientType<>(codec, streamCodec), NeoForgeRegistries.Keys.INGREDIENT_TYPES);
    }

    // --- Custom Fluid Ingredient Types ---

    @SyntaxSugar()
    public <T extends net.neoforged.neoforge.fluids.crafting.FluidIngredient> FluidIngredientType<T> fluidIngredientType(@NotNull String name, @NotNull MapCodec<T> codec) {
        return fluidIngredientType(name, codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }

    /**
     * 注册一个自定义 {@link net.neoforged.neoforge.fluids.crafting.FluidIngredientType}（提供 MapCodec 和
     * StreamCodec）。
     *
     * <p>
     * Registers a custom {@link net.neoforged.neoforge.fluids.crafting.FluidIngredientType} with
     * both a {@link com.mojang.serialization.MapCodec} and a {@link
     * net.minecraft.network.codec.StreamCodec}.
     *
     * @param name        the fluid ingredient type registry name
     * @param codec       the MapCodec
     * @param streamCodec the StreamCodec for network syncing
     * @return a {@link RegistryEntry} wrapping the registered type
     */
    @StandardAPI("Registers a custom FluidIngredientType with explicit StreamCodec.")
    public <T extends net.neoforged.neoforge.fluids.crafting.FluidIngredient> FluidIngredientType<T> fluidIngredientType(
                                                                                                                         @NotNull String name,
                                                                                                                         @NotNull MapCodec<T> codec,
                                                                                                                         @NotNull StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec) {
        return registry(
                name,
                new FluidIngredientType<>(codec, streamCodec),
                net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES);
    }

    // --- DataComponentType ---

    @SyntaxSugar("dataComponentType(name, Registries.DATA_COMPONENT_TYPE, builder)")
    public <T> DataComponentType<T> dataComponentType(
                                                      @NotNull String name, Consumer<DataComponentType.Builder<T>> builder) {
        return dataComponentType(name, Registries.DATA_COMPONENT_TYPE, builder);
    }

    @StandardAPI("Registers a custom DataComponentType.")
    public <T> DataComponentType<T> dataComponentType(
                                                      @NotNull String name,
                                                      ResourceKey<Registry<DataComponentType<?>>> registriesKey,
                                                      Consumer<DataComponentType.Builder<T>> builder) {
        DataComponentType.Builder<T> b = new DataComponentType.Builder<>();
        builder.accept(b);
        return registry(name, b.build(), registriesKey);
    }

    // --- Enchantments (Builder) ---

    @StandardAPI("Returns an EnchantmentBuilder for fluent chain configuration. Call .register() to finalise.")
    public EnchantmentBuilder<RegistryCore> enchantment(@NotNull String name) {
        return EnchantmentBuilder.create(this, this, name);
    }

    @StandardAPI
    public <P> EnchantmentBuilder<P> enchantment(@NotNull P parent, @NotNull String name) {
        return EnchantmentBuilder.create(this, parent, name);
    }

    // --- Entities ---

    public <T extends Entity, P> EntityBuilder<T, P> entity(
                                                            @NotNull P parent,
                                                            @NotNull String name,
                                                            @NotNull EntityType.EntityFactory<T> factory,
                                                            @NotNull MobCategory category) {
        return EntityBuilder.create(this, parent, name, factory, category);
    }

    @StandardAPI("Returns an EntityBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Entity> EntityBuilder<T, RegistryCore> entity(
                                                                    @NotNull String name,
                                                                    @NotNull EntityType.EntityFactory<T> factory,
                                                                    @NotNull MobCategory category) {
        return entity(this, name, factory, category);
    }

    @SuppressWarnings("unchecked")
    public void registerEntityAttributes(
                                         Supplier<?> entityTypeSupplier, Supplier<AttributeSupplier.Builder> attributesFactory) {
        entityAttributes.add(Pair.of((Supplier<EntityType<?>>) entityTypeSupplier, attributesFactory));
    }

    public <T extends Entity> void registerSpawnPlacement(
                                                          Supplier<EntityType<T>> entityType,
                                                          SpawnPlacementType placementType,
                                                          Heightmap.Types heightmap,
                                                          SpawnPlacements.SpawnPredicate<T> predicate) {
        spawnPlacements.add(
                event -> event.register(
                        entityType.get(),
                        placementType,
                        heightmap,
                        predicate,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE));
    }

    // --- Creative Tab ---

    @SyntaxSugar("creativeTab(name, FunctionUtil.noOpConsumer())")
    public RegistryEntry<CreativeModeTab, CreativeModeTab> creativeTab(String name) {
        return creativeTab(name, FunctionUtil.noOpConsumer());
    }

    @StandardAPI
    public RegistryEntry<CreativeModeTab, CreativeModeTab> creativeTab(
                                                                       String name, Consumer<CreativeModeTab.Builder> config) {
        return this.registry(
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
                },
                RegistryEntry::new);
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
                    core.entityAttributes.consume(
                            pair -> {
                                var type = (EntityType<? extends LivingEntity>) pair.getLeft().get();
                                event.put(type, pair.getRight().get().build());
                            });
                    if (!core.registrations.isEmpty()) {
                        log.error("Registry {} has unregistered entries", core.registrations.getMap().keySet());
                    }
                });
    }

    static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        REGISTRY_CORES.forEach(core -> core.spawnPlacements.consume(action -> action.accept(event)));
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
        private final List<Consumer<? super T>> callbacks;

        private Registration(
                             ResourceKey<? extends Registry<R>> type,
                             Identifier name,
                             Function<ResourceKey<R>, ? extends T> creator,
                             Function<ResourceKey<R>, ? extends RegistryEntry<R, T>> entryFactory,
                             List<Consumer<? super T>> callbacks) {
            this.key = ResourceKey.create(type, name);
            this.creator = creator;
            this.entry = entryFactory.apply(this.key);
            this.callbacks = callbacks;
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
