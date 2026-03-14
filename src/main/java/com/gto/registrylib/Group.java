package com.gto.registrylib;

import com.gto.registrylib.annotations.StandardAPI;
import com.gto.registrylib.builders.BlockBuilder;
import com.gto.registrylib.builders.BlockEntityBuilder;
import com.gto.registrylib.builders.FluidBuilder;
import com.gto.registrylib.builders.ItemBuilder;
import com.gto.registrylib.providers.RegistryLibLangProvider;
import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylib.util.entry.ItemEntry;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

/**
 * A lightweight grouping wrapper around {@link RegistryCore} that applies shared defaults (creative
 * tab, property modifiers, lang prefix) to all entries created through it.
 *
 * <p>
 * Construct via {@link RegistryCore#group(String)}:
 *
 * <pre>{@code
 * 
 * Group MACHINES = REGISTRYLIB.group("machines")
 *         .defaultTab(MACHINE_TAB)
 *         .langPrefix("Machine")
 *         .blockProperties(p -> p.strength(3.5F))
 *         .build();
 *
 * BlockEntry<Block> CRUSHER = MACHINES.block("crusher", Block::new, block -> {
 *     block.simpleItem();
 * });
 * }</pre>
 */
public class Group {

    private final RegistryCore registryCore;
    @Nullable
    private final ResourceKey<CreativeModeTab> defaultTab;
    @Nullable
    private final String langPrefix;
    private final UnaryOperator<BlockBehaviour.Properties> blockPropertiesModifier;
    private final UnaryOperator<Item.Properties> itemPropertiesModifier;

    private Group(Builder builder) {
        this.registryCore = builder.registryCore;
        this.defaultTab = builder.defaultTab;
        this.langPrefix = builder.langPrefix;
        this.blockPropertiesModifier = builder.blockPropertiesModifier;
        this.itemPropertiesModifier = builder.itemPropertiesModifier;
    }

    // === Accessors ===

    public RegistryCore getRegistryLib() {
        return registryCore;
    }

    // === Factory Methods ===

    @StandardAPI("Consumer-scoped: configures and auto-registers a Block entry with group defaults.")
    public <T extends Block> BlockEntry<T> block(
                                                 String name,
                                                 Function<BlockBehaviour.Properties, T> factory,
                                                 Consumer<BlockBuilder<T, Group>> config) {
        var builder = registryCore.<T, Group>block(this, name, factory).transform(this::applyBlockDefaults);
        config.accept(builder);
        return builder.register();
    }

    @StandardAPI("Consumer-scoped: configures and auto-registers an Item entry with group defaults.")
    public <T extends Item> ItemEntry<T> item(
                                              String name, Function<Item.Properties, T> factory, Consumer<ItemBuilder<T, Group>> config) {
        var builder = registryCore.<T, Group>item(this, name, factory).transform(this::applyItemDefaults);
        config.accept(builder);
        return builder.register();
    }

    @SuppressWarnings("unchecked")
    @StandardAPI("Consumer-scoped: configures and auto-registers a BlockEntity entry with group defaults.")
    public <T extends BlockEntity> BlockEntityEntry<T> blockEntity(
                                                                   String name,
                                                                   BlockEntityBuilder.BlockEntityFactory<T> factory,
                                                                   Consumer<BlockEntityBuilder<T, Group>> config) {
        var builder = registryCore.<T, Group>blockEntity(this, name, factory);
        config.accept(builder);
        return (BlockEntityEntry<T>) builder.register();
    }

    @StandardAPI("Consumer-scoped: configures and auto-registers a Fluid entry with group defaults.")
    public FluidEntry<BaseFlowingFluid.Flowing> fluid(
                                                      String name,
                                                      Identifier stillTexture,
                                                      Identifier flowingTexture,
                                                      Consumer<FluidBuilder<BaseFlowingFluid.Flowing, Group>> config) {
        var builder = registryCore.<BaseFlowingFluid.Flowing, Group>fluid(
                this, name, stillTexture, flowingTexture, BaseFlowingFluid.Flowing::new)
                .transform(this::applyFluidDefaults);
        config.accept(builder);
        return builder.register();
    }

    @StandardAPI("Consumer-scoped: configures and auto-registers a Fluid entry with custom FluidFactory and group defaults.")
    public <T extends BaseFlowingFluid> FluidEntry<T> fluid(
                                                            String name,
                                                            Identifier stillTexture,
                                                            Identifier flowingTexture,
                                                            FluidBuilder.FluidFactory<T> fluidFactory,
                                                            Consumer<FluidBuilder<T, Group>> config) {
        var builder = registryCore
                .<T, Group>fluid(this, name, stillTexture, flowingTexture, fluidFactory)
                .transform(this::applyFluidDefaults);
        config.accept(builder);
        return builder.register();
    }

    // === Default Application ===

    private <T extends Block> BlockBuilder<T, Group> applyBlockDefaults(
                                                                        BlockBuilder<T, Group> builder) {
        var b = builder.properties(blockPropertiesModifier);
        if (langPrefix != null) {
            b = b.lang(langPrefix + " " + RegistryLibLangProvider.toEnglishName(builder.getName()));
        }
        return b;
    }

    private <T extends Item> ItemBuilder<T, Group> applyItemDefaults(ItemBuilder<T, Group> builder) {
        var b = builder.properties(itemPropertiesModifier);
        if (defaultTab != null) {
            b = b.tab(defaultTab);
        }
        if (langPrefix != null) {
            b = b.lang(langPrefix + " " + RegistryLibLangProvider.toEnglishName(builder.getName()));
        }
        return b;
    }

    private <T extends BaseFlowingFluid> FluidBuilder<T, Group> applyFluidDefaults(
                                                                                   FluidBuilder<T, Group> builder) {
        if (langPrefix != null) {
            builder = builder.lang(langPrefix + " " + RegistryLibLangProvider.toEnglishName(builder.getName()));
        }
        return builder;
    }

    // === Builder ===

    public static class Builder {

        private final RegistryCore registryCore;
        @Nullable
        private ResourceKey<CreativeModeTab> defaultTab;
        @Nullable
        private String langPrefix;
        private UnaryOperator<BlockBehaviour.Properties> blockPropertiesModifier = UnaryOperator.identity();
        private UnaryOperator<Item.Properties> itemPropertiesModifier = UnaryOperator.identity();

        Builder(RegistryCore registryCore) {
            this.registryCore = registryCore;
        }

        @StandardAPI
        public Builder defaultTab(ResourceKey<CreativeModeTab> tab) {
            this.defaultTab = tab;
            return this;
        }

        @StandardAPI
        public Builder langPrefix(String prefix) {
            this.langPrefix = prefix;
            return this;
        }

        @StandardAPI
        public Builder blockProperties(UnaryOperator<BlockBehaviour.Properties> modifier) {
            this.blockPropertiesModifier = modifier;
            return this;
        }

        @StandardAPI
        public Builder itemProperties(UnaryOperator<Item.Properties> modifier) {
            this.itemPropertiesModifier = modifier;
            return this;
        }

        @StandardAPI
        public Group build() {
            return new Group(this);
        }
    }
}
