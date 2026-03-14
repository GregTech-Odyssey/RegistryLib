package com.gto.registrylib;

import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.annotations.SyntaxSugar;
import com.gto.registrylib.builders.*;
import com.gto.registrylib.providers.*;
import com.gto.registrylib.util.*;
import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylib.util.map.MultiMap;
import com.gto.registrylib.util.map.NestedMap;
import com.gto.registrylib.util.map.NestedMultiMap;

import com.google.common.base.Preconditions;
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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RegistryCore {

    private static final Logger log = RegistryLib.LOGGER;

    // === Fields ===
    private final NestedMap<ResourceKey<? extends Registry<?>>, String, Registration<?, ?>> registrations = NestedMap.createIdentity(HashMap::new);
    private final NestedMultiMap<ResourceKey<? extends Registry<?>>, String, Consumer<?>> registerCallbacks = NestedMultiMap.createIdentity(HashMap::new, ArrayList::new);
    private final MultiMap<ResourceKey<? extends Registry<?>>, Runnable> afterRegisterCallbacks = MultiMap.createIdentity(ArrayList::new);
    private final Set<ResourceKey<? extends Registry<?>>> completedRegistrations = new ReferenceOpenHashSet<>();

    private final MultiMap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> creativeModeTabModifiers = MultiMap.createIdentity(ArrayList::new);
    private ResourceKey<CreativeModeTab> defaultCreativeModeTab = CreativeModeTabs.SEARCH;

    private final Table<Pair<String, ResourceKey<? extends Registry<?>>>, GeneratorType<?>, Consumer<?>> datagensByEntry = HashBasedTable.create();
    private final MultiMap<GeneratorType<?>, Consumer<?>> datagens = MultiMap.createIdentity(ArrayList::new);

    private final Supplier<Boolean> doDatagen = Lazy.of(DatagenModLoader::isRunningDataGen);

    @Getter
    private final String modid;

    @Nullable
    private IEventBus modEventBus;
    private boolean skipErrors;

    // === Constructor + Factory ===

    protected RegistryCore(String modid) {
        this.modid = modid;
    }

    public static RegistryCore create(String modid) {
        var ret = new RegistryCore(modid);
        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modid).map(ModContainer::getEventBus);
        modEventBus.ifPresentOrElse(
                ret::registerEventListeners,
                () -> {
                    String message = "# [RegistryCore] Failed to register eventListeners for mod " + modid + " #";
                    log.fatal("#".repeat(message.length()));
                    log.fatal(message);
                    log.fatal("#".repeat(message.length()));
                });
        return ret;
    }

    // === Accessors ===

    public static boolean isDevEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    public Supplier<Boolean> doDatagen() {
        return doDatagen;
    }

    @Nullable
    public IEventBus getModEventBus() {
        return modEventBus;
    }

    public void setModEventBus(@Nullable IEventBus bus) {
        this.modEventBus = bus;
    }

    // === Event Registration ===

    public RegistryCore registerEventListeners(IEventBus bus) {
        if (this.modEventBus == null) {
            this.modEventBus = bus;
        }

        Consumer<RegisterEvent> onRegister = this::onRegister;
        Consumer<RegisterEvent> onRegisterLate = this::onRegisterLate;
        bus.addListener(onRegister);
        bus.addListener(EventPriority.LOWEST, onRegisterLate);
        bus.addListener(this::onBuildCreativeModeTabContents);

        OneTimeEventReceiver.addModListener(
                this,
                FMLCommonSetupEvent.class,
                $ -> {
                    OneTimeEventReceiver.unregister(this, onRegister, RegisterEvent.class);
                    OneTimeEventReceiver.unregister(this, onRegisterLate, RegisterEvent.class);
                });

        if (doDatagen.get()) {
            OneTimeEventReceiver.addModListener(this, GatherDataEvent.Client.class, this::onData);
        }

        return this;
    }

    // === Event Handlers ===

    protected void onRegister(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        if (type == null) {
            log.debug(
                    DebugMarkers.REGISTER,
                    "Skipping invalid registry with no supertype: " + event.getRegistryKey().identifier());
            return;
        }
        if (!registerCallbacks.isEmpty()) {
            registerCallbacks
                    .getMap()
                    .forEach(
                            (k, v) -> log.warn(
                                    "Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?",
                                    v.size(),
                                    k,
                                    k.identifier()));
            registerCallbacks.clear();
            if (isDevEnvironment()) {
                throw new IllegalStateException("Found unused register callbacks, see logs");
            }
        }
        Map<String, Registration<?, ?>> registrationsForType = registrations.get(type);
        if (!registrationsForType.isEmpty()) {
            log.trace(
                    DebugMarkers.REGISTER,
                    "({}) Registering {} known objects of type {}",
                    getModid(),
                    registrationsForType.size(),
                    type.identifier());
            for (Entry<String, Registration<?, ?>> e : registrationsForType.entrySet()) {
                try {
                    e.getValue().register(event);
                    log.trace(
                            DebugMarkers.REGISTER,
                            "Registered {} to registry {}",
                            e.getValue().getName(),
                            event.getRegistryKey().identifier());
                } catch (Exception ex) {
                    String err = "Unexpected error while registering entry " + e.getValue().getName() + " to registry " + event.getRegistryKey().identifier();
                    if (skipErrors) {
                        log.error(DebugMarkers.REGISTER, err);
                    } else {
                        throw new RuntimeException(err, ex);
                    }
                }
            }
        }
    }

    protected void onRegisterLate(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        afterRegisterCallbacks.remove(type).forEach(Runnable::run);
        completedRegistrations.add(type);
    }

    protected void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        var modifier = new CreativeModeTabModifier(
                event::getFlags, event::hasPermissions, event::accept, event::getParameters);
        creativeModeTabModifiers.get(event.getTabKey()).forEach(value -> value.accept(modifier));
    }

    @Nullable
    private RegistryLibDataProvider provider;

    protected void onData(GatherDataEvent event) {
        extraLang.get();
        event
                .getGenerator()
                .addProvider(true, provider = new RegistryLibDataProvider(this, modid, event));
    }

    // === Entry Access ===

    public <R, T extends R> RegistryEntry<R, T> get(
                                                    String name, ResourceKey<? extends Registry<R>> type) {
        return this.<R, T>getRegistration(name, type).getDelegate();
    }

    public <R, T extends R> Optional<RegistryEntry<R, T>> getOptional(
                                                                      String name, ResourceKey<? extends Registry<R>> type) {
        Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, type);
        return reg == null ? Optional.empty() : Optional.of(reg.getDelegate());
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
                .map(r -> (RegistryEntry<R, T>) r.getDelegate())
                .collect(Collectors.toList());
    }

    // === Callback Management ===

    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryCore addRegisterCallback(
                                                             String name, ResourceKey<? extends Registry<R>> registryType, Consumer<? super T> callback) {
        Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, registryType);
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
        if (!doDatagen.get()) return this;
        @SuppressWarnings("null")
        Consumer<?> existing = datagensByEntry.put(Pair.of(entry, registryType), type, cons);
        if (existing != null) {
            datagens.remove(type, existing);
        }
        return addDataGenerator(type, cons);
    }

    public <T> RegistryCore addDataGenerator(
                                             GeneratorType<? extends T> type, Consumer<? extends T> cons) {
        if (doDatagen.get()) {
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
        if (doDatagen.get()) {
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
        if (!doDatagen.get()) return;
        if (provider != null) {
            provider.putSubProvider(type, gen);
        }
        datagens
                .get(type)
                .forEach(
                        cons -> {
                            Optional<Pair<String, ResourceKey<? extends Registry<?>>>> entry = null;
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
                                if (entry == null) {
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
                                                          Supplier<? extends T> creator,
                                                          Function<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
        Registration<R, T> reg = new Registration<>(
                Identifier.fromNamespaceAndPath(modid, name), type, creator, entryFactory);
        log.trace(
                DebugMarkers.REGISTER,
                "Captured registration for entry {}:{} of type {}",
                getModid(),
                name,
                type.identifier());
        registerCallbacks
                .remove(type, name)
                .forEach(
                        callback -> {
                            @SuppressWarnings({ "unchecked", "null" })
                            Consumer<? super T> unsafeCallback = (Consumer<? super T>) callback;
                            reg.addRegisterCallback(unsafeCallback);
                        });
        registrations.put(type, name, reg);
        return reg.getDelegate();
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
                                                       @Nonnull String name,
                                                       @Nonnull ResourceKey<Registry<R>> registryType,
                                                       @Nonnull Supplier<T> factory) {
        return generic(name, registryType, factory).register();
    }

    @StandardAPI
    public <R, T extends R> NoConfigBuilder<R, T, RegistryCore> generic(
                                                                        @Nonnull String name,
                                                                        @Nonnull ResourceKey<Registry<R>> registryType,
                                                                        @Nonnull Supplier<T> factory) {
        return entry(
                callback -> new NoConfigBuilder<>(this, this, name, callback, registryType, factory));
    }

    @SyntaxSugar("generic(...).register()")
    public <R, T extends R, P> RegistryEntry<R, T> simple(
                                                          @Nonnull P parent,
                                                          @Nonnull String name,
                                                          @Nonnull ResourceKey<Registry<R>> registryType,
                                                          @Nonnull Supplier<T> factory) {
        return generic(parent, name, registryType, factory).register();
    }

    @StandardAPI
    public <R, T extends R, P> NoConfigBuilder<R, T, P> generic(
                                                                @Nonnull P parent,
                                                                @Nonnull String name,
                                                                @Nonnull ResourceKey<Registry<R>> registryType,
                                                                @Nonnull Supplier<T> factory) {
        return entry(
                callback -> new NoConfigBuilder<>(this, parent, name, callback, registryType, factory));
    }

    // --- Items ---

    @StandardAPI("Consumer-scoped: configures and auto-registers an Item entry.")
    public <T extends Item> ItemEntry<T> item(
                                              @Nonnull String name,
                                              @Nonnull Function<Item.Properties, T> factory,
                                              @Nonnull Consumer<ItemBuilder<T, RegistryCore>> config) {
        var builder = item(this, name, factory);
        config.accept(builder);
        return builder.register();
    }

    public <T extends Item, P> ItemBuilder<T, P> item(
                                                      @Nonnull P parent, @Nonnull String name, @Nonnull Function<Item.Properties, T> factory) {
        return entry(
                callback -> ItemBuilder.create(this, parent, name, callback, factory)
                        .transform(
                                builder -> this.defaultCreativeModeTab == null ? builder : builder.tab(this.defaultCreativeModeTab)));
    }

    // --- Blocks ---

    @StandardAPI("Consumer-scoped: configures and auto-registers a Block entry.")
    public <T extends Block> BlockEntry<T> block(
                                                 @Nonnull String name,
                                                 @Nonnull Function<BlockBehaviour.Properties, T> factory,
                                                 @Nonnull Consumer<BlockBuilder<T, RegistryCore>> config) {
        var builder = block(this, name, factory);
        config.accept(builder);
        return builder.register();
    }

    public <T extends Block, P> BlockBuilder<T, P> block(
                                                         @Nonnull P parent,
                                                         @Nonnull String name,
                                                         @Nonnull Function<BlockBehaviour.Properties, T> factory) {
        return entry(callback -> BlockBuilder.create(this, parent, name, callback, factory));
    }

    // --- Block Entities ---

    @SuppressWarnings("unchecked")
    @StandardAPI("Consumer-scoped: configures and auto-registers a BlockEntity entry.")
    public <T extends BlockEntity> BlockEntityEntry<T> blockEntity(
                                                                   @Nonnull String name,
                                                                   @Nonnull BlockEntityBuilder.BlockEntityFactory<T> factory,
                                                                   @Nonnull Consumer<BlockEntityBuilder<T, RegistryCore>> config) {
        var builder = blockEntity(this, name, factory);
        config.accept(builder);
        return (BlockEntityEntry<T>) builder.register();
    }

    public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(
                                                                           @Nonnull P parent,
                                                                           @Nonnull String name,
                                                                           @Nonnull BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return entry(callback -> BlockEntityBuilder.create(this, parent, name, callback, factory));
    }

    // --- Fluids ---

    @StandardAPI("Consumer-scoped: configures and auto-registers a Fluid entry with default Flowing type.")
    public FluidEntry<BaseFlowingFluid.Flowing> fluid(
                                                      @Nonnull String name,
                                                      @Nonnull Identifier stillTexture,
                                                      @Nonnull Identifier flowingTexture,
                                                      @Nonnull Consumer<FluidBuilder<BaseFlowingFluid.Flowing, RegistryCore>> config) {
        var builder = fluid(this, name, stillTexture, flowingTexture, BaseFlowingFluid.Flowing::new);
        config.accept(builder);
        return builder.register();
    }

    @StandardAPI("Consumer-scoped: configures and auto-registers a Fluid entry with custom FluidFactory.")
    public <T extends BaseFlowingFluid> FluidEntry<T> fluid(
                                                            @Nonnull String name,
                                                            @Nonnull Identifier stillTexture,
                                                            @Nonnull Identifier flowingTexture,
                                                            @Nonnull FluidBuilder.FluidFactory<T> fluidFactory,
                                                            @Nonnull Consumer<FluidBuilder<T, RegistryCore>> config) {
        var builder = fluid(this, name, stillTexture, flowingTexture, fluidFactory);
        config.accept(builder);
        return builder.register();
    }

    public <T extends BaseFlowingFluid, P> FluidBuilder<T, P> fluid(
                                                                    @Nonnull P parent,
                                                                    @Nonnull String name,
                                                                    @Nonnull Identifier stillTexture,
                                                                    @Nonnull Identifier flowingTexture,
                                                                    @Nonnull FluidBuilder.FluidFactory<T> fluidFactory) {
        return entry(
                callback -> FluidBuilder.create(this, parent, name, callback, FluidType::new, fluidFactory))
                .clientExtension(stillTexture, flowingTexture);
    }

    // --- Group ---

    @StandardAPI
    public Group.Builder group(@Nonnull String name) {
        return new Group.Builder(this, name);
    }

    // --- Creative Tab ---

    @SyntaxSugar("defaultCreativeTab(name, tab -> {})")
    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, RegistryCore> defaultCreativeTab(
                                                                                              String name) {
        return defaultCreativeTab(name, tab -> {});
    }

    @StandardAPI
    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, RegistryCore> defaultCreativeTab(
                                                                                              String name, Consumer<CreativeModeTab.Builder> config) {
        this.defaultCreativeModeTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(this.modid, name));
        return this.generic(
                name,
                Registries.CREATIVE_MODE_TAB,
                () -> {
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
                                            this.defaultCreativeModeTab.identifier(),
                                            RegistryLibLangProvider.toEnglishName(name)));
                    config.accept(builder);
                    return builder.build();
                });
    }

    private static class Registration<R, T extends R> {

        private final Identifier name;
        private final ResourceKey<? extends Registry<R>> type;
        private final Supplier<? extends T> creator;
        private final RegistryEntry<R, T> delegate;
        private final List<Consumer<? super T>> callbacks = new ArrayList<>();

        Registration(
                     Identifier name,
                     ResourceKey<? extends Registry<R>> type,
                     Supplier<? extends T> creator,
                     Function<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
            this.name = name;
            this.type = type;
            this.creator = Lazy.of(creator);
            this.delegate = entryFactory.apply(DeferredHolder.create(type, name));
        }

        Identifier getName() {
            return name;
        }

        ResourceKey<? extends Registry<R>> getType() {
            return type;
        }

        RegistryEntry<R, T> getDelegate() {
            return delegate;
        }

        void register(RegisterEvent event) {
            T entry = creator.get();
            event.register(type, rh -> rh.register(name, entry));
            callbacks.forEach(c -> c.accept(entry));
            callbacks.clear();
        }

        void addRegisterCallback(Consumer<? super T> callback) {
            Preconditions.checkNotNull(callback, "Callback must not be null");
            callbacks.add(callback);
        }
    }
}
