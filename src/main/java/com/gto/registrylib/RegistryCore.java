package com.gto.registrylib;

import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.builders.*;
import com.gto.registrylib.composite.ComponentItem;
import com.gto.registrylib.composite.IComponentItem;
import com.gto.registrylib.providers.*;
import com.gto.registrylib.util.*;
import com.gto.registrylib.util.entry.*;
import com.gto.registrylib.util.map.MultiMap;
import com.gto.registrylib.util.map.NestedMap;
import com.gto.registrylib.util.map.NestedMultiMap;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class RegistryCore {

    private static final ConcurrentHashMap<String, RegistryCore> REGISTRY_CORES = new ConcurrentHashMap<>();
    private static final Logger log = RegistryLib.LOGGER;

    private final NestedMap<ResourceKey<? extends Registry<?>>, String, Registration<?, ?>> registrations = NestedMap.createIdentity(HashMap::new);
    private final NestedMultiMap<ResourceKey<? extends Registry<?>>, String, Consumer<?>> registerCallbacks = NestedMultiMap.createIdentity(HashMap::new, ArrayList::new);
    private final MultiMap<ResourceKey<? extends Registry<?>>, Runnable> afterRegisterCallbacks = MultiMap.createIdentity(ArrayList::new);
    private final Set<ResourceKey<? extends Registry<?>>> completedRegistrations = new ReferenceOpenHashSet<>();

    private final MultiMap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> creativeModeTabModifiers = MultiMap.createIdentity(ArrayList::new);
    private ResourceKey<CreativeModeTab> defaultCreativeModeTab = CreativeModeTabs.SEARCH;

    private final Table<Pair<String, ResourceKey<? extends Registry<?>>>, GeneratorType<?>, Consumer<?>> datagensByEntry = HashBasedTable.create();
    private final MultiMap<GeneratorType<?>, Consumer<?>> datagens = MultiMap.createIdentity(ArrayList::new);

    @Getter
    private final String modid;

    @Nullable
    private IEventBus modEventBus;
    private boolean skipErrors;

    // === Constructor + Factory ===

    protected RegistryCore(String modid) {
        this.modid = modid;
        REGISTRY_CORES.put(modid, this);
        if (doDatagen()) {
            ModList.get()
                    .getModContainerById(modid)
                    .ifPresent(c -> c.getEventBus().addListener(this::onGatherData));
        }
    }

    public static RegistryCore create(String modid) {
        return new RegistryCore(modid);
    }

    // === Accessors ===

    public static boolean isDevEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    public boolean doDatagen() {
        return DatagenModLoader.isRunningDataGen();
    }

    @Nullable
    public IEventBus getModEventBus() {
        return modEventBus;
    }

    public void setModEventBus(@Nullable IEventBus bus) {
        this.modEventBus = bus;
    }

    @Nullable
    private RegistryLibDataProvider provider;

    // === Entry Access ===

    public <R, T extends R> RegistryEntry<R, T> get(
                                                    String name, ResourceKey<? extends Registry<R>> type) {
        return this.<R, T>getRegistration(name, type).entry;
    }

    public <R, T extends R> Optional<RegistryEntry<R, T>> getOptional(
                                                                      String name, ResourceKey<? extends Registry<R>> type) {
        Registration<R, T> reg = this.getRegistrationUnchecked(name, type);
        return reg == null ? Optional.empty() : Optional.of(reg.entry);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <R, T extends R> Registration<R, T> getRegistrationUnchecked(
                                                                         String name, ResourceKey<? extends Registry<R>> type) {
        return (Registration<R, T>) registrations.get(type, name);
    }

    private <R, T extends R> Registration<R, T> getRegistration(
                                                                String name, ResourceKey<? extends Registry<R>> type) {
        Registration<R, T> reg = this.getRegistrationUnchecked(name, type);
        if (reg != null) return reg;
        throw new IllegalArgumentException(
                "Unknown registration " + name + " for type " + type.identifier());
    }

    @SuppressWarnings({ "null", "unchecked" })
    public <R, T extends R> Collection<RegistryEntry<R, T>> getAll(
                                                                   ResourceKey<? extends Registry<R>> type) {
        return registrations.get(type).values().stream()
                .map(r -> (RegistryEntry<R, T>) r.entry)
                .toList();
    }

    // === Callback Management ===

    public <R, T extends R> RegistryCore addRegisterCallback(
                                                             String name, ResourceKey<? extends Registry<R>> registryType, Consumer<? super T> callback) {
        Registration<R, T> reg = this.getRegistrationUnchecked(name, registryType);
        if (reg == null) {
            registerCallbacks.put(registryType, name, callback);
        } else {
            reg.addRegisterCallback(callback);
        }
        return this;
    }

    public <R> RegistryCore addRegisterCallback(
                                                ResourceKey<? extends Registry<R>> registryType, Runnable callback) {
        afterRegisterCallbacks.put(registryType, callback);
        return this;
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

    public <P, R> RegistryCore setDataGenerator(
                                                Builder<R, ?, ?, ?> builder, GeneratorType<? extends P> type, Consumer<? extends P> cons) {
        return this.setDataGenerator(builder.getName(), builder.getRegistryKey(), type, cons);
    }

    public <P, R> RegistryCore setDataGenerator(
                                                String entry,
                                                ResourceKey<? extends Registry<R>> registryType,
                                                GeneratorType<? extends P> type,
                                                Consumer<? extends P> cons) {
        if (!doDatagen()) return this;
        @SuppressWarnings("null")
        Consumer<?> existing = datagensByEntry.put(Pair.of(entry, registryType), type, cons);
        if (existing != null) {
            datagens.remove(type, existing);
        }
        return addDataGenerator(type, cons);
    }

    public <T> RegistryCore addDataGenerator(
                                             GeneratorType<? extends T> type, Consumer<? extends T> cons) {
        if (doDatagen()) {
            if (provider != null)
                throw new IllegalStateException(
                        "Cannot add data generator after construction of root generator");
            datagens.put(type, cons);
        }
        return this;
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

    @SuppressWarnings("null")
    private Optional<Pair<String, ResourceKey<? extends Registry<?>>>> getEntryForGenerator(
                                                                                            GeneratorType<?> type, Consumer<?> generator) {
        for (Map.Entry<Pair<String, ResourceKey<? extends Registry<?>>>, Consumer<?>> e : datagensByEntry.column(type).entrySet()) {
            if (e.getValue() == generator) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> void genData(GeneratorType<? extends T> type, T gen) {
        if (!doDatagen()) return;
        if (provider != null) {
            provider.putSubProvider(type, gen);
        }
        datagens
                .get(type)
                .forEach(
                        cons -> {
                            Optional<Pair<String, ResourceKey<? extends Registry<?>>>> entry = Optional.empty();
                            if (log.isEnabled(Level.DEBUG, DebugMarkers.DATA)) {
                                entry = getEntryForGenerator(type, cons);
                                if (entry.isPresent()) {
                                    log.debug(
                                            DebugMarkers.DATA,
                                            "Generating data of type {} for entry {} [{}]",
                                            RegistryLibDataProvider.getTypeName(type),
                                            entry.get().getLeft(),
                                            entry.get().getRight().identifier());
                                } else {
                                    log.debug(
                                            DebugMarkers.DATA,
                                            "Generating unassociated data of type {} ({})",
                                            RegistryLibDataProvider.getTypeName(type),
                                            type);
                                }
                            }
                            try {
                                ((Consumer<T>) cons).accept(gen);
                            } catch (Exception e) {
                                if (entry.isEmpty()) {
                                    entry = getEntryForGenerator(type, cons);
                                }
                                Message err;
                                if (entry.isPresent()) {
                                    err = log.getMessageFactory()
                                            .newMessage(
                                                    "Unexpected error while running data generator of type {} for entry {} [{}]",
                                                    RegistryLibDataProvider.getTypeName(type),
                                                    entry.get().getLeft(),
                                                    entry.get().getRight().identifier());
                                } else {
                                    err = log.getMessageFactory()
                                            .newMessage(
                                                    "Unexpected error while running unassociated data generator of type {} ({})",
                                                    RegistryLibDataProvider.getTypeName(type),
                                                    type);
                                }
                                if (skipErrors) {
                                    log.error(err);
                                } else {
                                    throw new RuntimeException(err.getFormattedMessage(), e);
                                }
                            }
                        });
    }

    // === Configuration ===

    public RegistryCore skipErrors(boolean skipErrors) {
        if (skipErrors && !isDevEnvironment()) {
            log.error("Ignoring skipErrors(true) as this is not a development environment!");
        } else {
            this.skipErrors = skipErrors;
        }
        return this;
    }

    public RegistryCore defaultCreativeTab(ResourceKey<CreativeModeTab> creativeModeTab) {
        defaultCreativeModeTab = creativeModeTab;
        return this;
    }

    public RegistryCore modifyCreativeModeTab(
                                              ResourceKey<CreativeModeTab> creativeModeTab, Consumer<CreativeModeTabModifier> modifier) {
        creativeModeTabModifiers.put(creativeModeTab, modifier);
        return this;
    }

    // === Entry Helper ===

    public <R, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 entry(
                                                                         Function<BuilderCallback, S2> factory) {
        return factory.apply(this::accept);
    }

    // === Core Registration ===

    protected <R, T extends R> RegistryEntry<R, T> accept(
                                                          String name,
                                                          ResourceKey<? extends Registry<R>> type,
                                                          Builder<R, T, ?, ?> builder,
                                                          Function<ResourceKey<R>, ? extends T> factory,
                                                          Function<ResourceKey<R>, ? extends RegistryEntry<R, T>> entryFactory) {
        Registration<R, T> reg = new Registration<>(
                type, Identifier.fromNamespaceAndPath(modid, name), factory, entryFactory);
        registerCallbacks
                .remove(type, name)
                .forEach(
                        callback -> {
                            @SuppressWarnings({ "unchecked", "null" })
                            Consumer<? super T> unsafeCallback = (Consumer<? super T>) callback;
                            reg.addRegisterCallback(unsafeCallback);
                        });
        registrations.put(type, name, reg);
        return reg.entry;
    }

    // === RegistryCore Creation ===

    public <R> ResourceKey<Registry<R>> makeRegistry(
                                                     String name, Function<ResourceKey<Registry<R>>, RegistryBuilder<R>> builder) {
        final ResourceKey<Registry<R>> registryId = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(getModid(), name));
        OneTimeEventReceiver.addModListener(
                this, NewRegistryEvent.class, e -> e.register(builder.apply(registryId).create()));
        return registryId;
    }

    public <R> ResourceKey<Registry<R>> makeDatapackRegistry(String name, Codec<R> codec) {
        return makeDatapackRegistry(name, codec, null);
    }

    public <R> ResourceKey<Registry<R>> makeDatapackRegistry(
                                                             String name, Codec<R> codec, @Nullable Codec<R> networkCodec) {
        final ResourceKey<Registry<R>> registryId = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(getModid(), name));
        OneTimeEventReceiver.addModListener(
                this,
                DataPackRegistryEvent.NewRegistry.class,
                event -> event.dataPackRegistry(registryId, codec, networkCodec));
        return registryId;
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
        return entry(
                callback -> new NoConfigBuilder<>(this, this, name, callback, registryType, factory));
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
        return entry(
                callback -> new NoConfigBuilder<>(this, parent, name, callback, registryType, factory));
    }

    // --- Items ---

    @StandardAPI("Returns an ItemBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Item> ItemBuilder<T, RegistryCore> item(
                                                              @Nonnull String name, @Nonnull Function<Item.Properties, T> factory) {
        return item(this, name, factory, false);
    }

    public <T extends Item & IComponentItem<T>> ItemBuilder<T, RegistryCore> componentItem(
                                                                                           @Nonnull String name, @Nonnull Function<Item.Properties, T> factory) {
        return item(this, name, factory, true);
    }

    public ItemBuilder<ComponentItem, RegistryCore> componentItem(@Nonnull String name) {
        return componentItem(name, ComponentItem::new);
    }

    public <T extends Item, P> ItemBuilder<T, P> item(
                                                      @Nonnull P parent,
                                                      @Nonnull String name,
                                                      @Nonnull Function<Item.Properties, T> factory,
                                                      boolean isComponentItem) {
        return entry(
                callback -> newItemBuilder(parent, name, callback, factory, isComponentItem)
                        .transform(
                                builder -> this.defaultCreativeModeTab == null ? builder : builder.tab(this.defaultCreativeModeTab)));
    }

    protected <T extends Item, P> ItemBuilder<T, P> newItemBuilder(
                                                                   @Nonnull P parent,
                                                                   @Nonnull String name,
                                                                   @Nonnull BuilderCallback callback,
                                                                   @Nonnull Function<Item.Properties, T> factory,
                                                                   boolean isComponentItem) {
        return ItemBuilder.create(this, parent, name, callback, factory, isComponentItem);
    }

    // --- Blocks ---

    @StandardAPI("Returns a BlockBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends Block> BlockBuilder<T, RegistryCore> block(
                                                                 @Nonnull String name, @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    public <T extends Block, P> BlockBuilder<T, P> block(
                                                         @Nonnull P parent,
                                                         @Nonnull String name,
                                                         @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return entry(callback -> newBlockBuilder(parent, name, callback, factory));
    }

    protected <T extends Block, P> BlockBuilder<T, P> newBlockBuilder(
                                                                      @Nonnull P parent,
                                                                      @Nonnull String name,
                                                                      @Nonnull BuilderCallback callback,
                                                                      @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return BlockBuilder.create(this, parent, name, callback, factory);
    }

    // --- Block Entities ---

    @StandardAPI("Returns a BlockEntityBuilder for fluent chain configuration. Call .register() to finalise.")
    public <T extends BlockEntity> BlockEntityBuilder<T, RegistryCore> blockEntity(
                                                                                   @Nonnull String name, @Nonnull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return blockEntity(this, name, factory);
    }

    public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(
                                                                           @Nonnull P parent,
                                                                           @Nonnull String name,
                                                                           @Nonnull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return entry(callback -> newBlockEntityBuilder(parent, name, callback, factory));
    }

    protected <T extends BlockEntity, P> BlockEntityBuilder<T, P> newBlockEntityBuilder(
                                                                                        @Nonnull P parent,
                                                                                        @Nonnull String name,
                                                                                        @Nonnull BuilderCallback callback,
                                                                                        @Nonnull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return BlockEntityBuilder.create(this, parent, name, callback, factory);
    }

    // --- Fluids ---

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
        return entry(callback -> newFluidBuilder(parent, name, callback, fluidFactory))
                .clientExtension(stillTexture, flowingTexture);
    }

    protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
                                                                                 @Nonnull P parent,
                                                                                 @Nonnull String name,
                                                                                 @Nonnull BuilderCallback callback,
                                                                                 @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return FluidBuilder.create(this, parent, name, callback, FluidType::new, fluidFactory);
    }

    // --- Group ---

    @StandardAPI
    public Group.Builder group(@NotNull String name) {
        return new Group.Builder(this, name);
    }

    // --- Creative Tab ---

    @StandardAPI
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
        var type = event.getRegistryKey();
        REGISTRY_CORES
                .values()
                .forEach(
                        core -> {
                            if (!core.registerCallbacks.isEmpty()) {
                                core.registerCallbacks
                                        .getMap()
                                        .forEach(
                                                (k, v) -> log.warn(
                                                        "Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?",
                                                        v.size(),
                                                        k,
                                                        k.identifier()));
                                core.registerCallbacks.clear();
                                if (isDevEnvironment()) {
                                    throw new IllegalStateException("Found unused register callbacks, see logs");
                                }
                            }
                            var registrationsForType = core.registrations.get(type);
                            if (!registrationsForType.isEmpty()) {
                                log.trace(
                                        DebugMarkers.REGISTER,
                                        "({}) Registering {} known objects of type {}",
                                        core.getModid(),
                                        registrationsForType.size(),
                                        type.identifier());
                                registrationsForType
                                        .values()
                                        .forEach(
                                                r -> {
                                                    try {
                                                        r.register((ResourceKey) type, event);
                                                    } catch (Exception ex) {
                                                        String err = "Unexpected error while registering entry " + r.key.identifier() + " to registry " + event.getRegistryKey().identifier();
                                                        if (core.skipErrors) {
                                                            log.error(DebugMarkers.REGISTER, err);
                                                        } else {
                                                            throw new RuntimeException(err, ex);
                                                        }
                                                    }
                                                });
                            }
                        });
    }

    static void onRegisterLate(RegisterEvent event) {
        var type = event.getRegistryKey();
        REGISTRY_CORES
                .values()
                .forEach(
                        core -> {
                            core.afterRegisterCallbacks.remove(type).forEach(Runnable::run);
                            core.completedRegistrations.add(type);
                        });
    }

    static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        var modifier = new CreativeModeTabModifier(event);
        REGISTRY_CORES
                .values()
                .forEach(
                        core -> core.creativeModeTabModifiers
                                .get(event.getTabKey())
                                .forEach(value -> value.accept(modifier)));
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
        private Function<ResourceKey<R>, ? extends T> creator;
        private List<Consumer<? super T>> callbacks = new ArrayList<>();

        private Registration(
                             ResourceKey<? extends Registry<R>> type,
                             Identifier name,
                             Function<ResourceKey<R>, ? extends T> creator,
                             Function<ResourceKey<R>, ? extends RegistryEntry<R, T>> entryFactory) {
            this.key = ResourceKey.create(type, name);
            this.creator = creator;
            this.entry = entryFactory.apply(this.key);
        }

        private void register(ResourceKey<? extends Registry<R>> type, RegisterEvent event) {
            T entry = creator.apply(key);
            this.entry.set(entry);
            event.register(type, rh -> rh.register(key.identifier(), entry));
            callbacks.forEach(c -> c.accept(entry));
            creator = null;
            callbacks = null;
        }

        private void addRegisterCallback(Consumer<? super T> callback) {
            callbacks.add(callback);
        }
    }
}
