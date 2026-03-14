package com.gto.registrylibtest;

import com.gto.registrylib.Group;
import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.composite.CompositeItem;
import com.gto.registrylib.composite.CompositeItemAttachment;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.tooltip.RootNodeRef;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylibtest.client.TimerBlockEntityRenderer;

import com.mojang.logging.LogUtils;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import org.slf4j.Logger;

@Mod(RegistryLibTest.MOD_ID)
public class RegistryLibTest {

    public static final String MOD_ID = "registrylibtest";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final RegistryCore REGISTRYLIB = RegistryCore.create(MOD_ID);

    // === Creative Tab ===
    static {
        REGISTRYLIB.defaultCreativeTab("test_tab").register();
    }

    // === Items ===

    public static final ItemEntry<Item> TEST_ITEM = REGISTRYLIB.item(
            "test_item",
            Item::new,
            item -> {
                item.tooltip(
                        (collector, stack) -> {
                            collector.node(new SubNode.Basic(Component.literal("§7A simple test item")));
                        });
            });

    public static final ItemEntry<Item> MAGIC_DUST = REGISTRYLIB.item(
            "magic_dust",
            Item::new,
            item -> {
                item.lang("Magic Dust")
                        .tooltip(
                                (collector, stack) -> {
                                    collector.node(
                                            new SubNode.Basic(Component.literal("§dMagical Dust"), 0), true, false);
                                    collector.node(
                                            new SubNode.Basic(
                                                    Component.literal("§8Amount: §f" + stack.getCount()), 10));
                                });
            });

    // === Tooltip: 独立框根节点示例 ===

    public static final RootNodeRef INFO_BOX = TooltipRegistry.rootNode(MOD_ID + ":info_box", 10, true);

    // === Composite Item: 组合物品示例 ===

    /** 右键使用时发送消息的附件。 */
    static class MessageAttachment extends CompositeItemAttachment<CompositeItem> {

        private final String message;

        MessageAttachment(String message) {
            this.message = message;
        }

        @Override
        public InteractionResult use(
                                     CompositeItem item, Level level, Player player, InteractionHand hand) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal(message));
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public void collectTooltipNodes(
                                        CompositeItem item,
                                        net.minecraft.world.item.ItemStack stack,
                                        com.gto.registrylib.tooltip.TooltipNodeCollector collector) {
            collector.node(new SubNode.Basic(Component.literal("§eRight-click: " + message), 100));
        }
    }

    public static final ItemEntry<CompositeItem> COMPOSITE_ITEM = REGISTRYLIB.item(
            "composite_item",
            CompositeItem::new,
            item -> {
                item.lang("Composite Item")
                        .attach(new MessageAttachment("Hello from CompositeItem!"))
                        .tooltip(
                                (collector, stack) -> {
                                    collector.node(
                                            new SubNode.Basic(Component.literal("§6Composite Item"), 0), true, false);
                                    // 使用独立信息框
                                    collector.node(
                                            INFO_BOX,
                                            new SubNode.Basic(Component.literal("§bThis is a separate info box"), 0));
                                    collector.node(
                                            INFO_BOX,
                                            new SubNode.Basic(Component.literal("§7With multiple lines"), 10));
                                });
            });

    // === Blocks ===

    public static final BlockEntry<Block> TEST_BLOCK = REGISTRYLIB.block(
            "test_block",
            Block::new,
            block -> {
                block
                        .initialProperties(() -> Blocks.STONE)
                        .item(
                                itemBuilder -> itemBuilder.tooltip(Component.literal("§7A solid test block")));
            });

    public static final BlockEntry<Block> MAGIC_ORE = REGISTRYLIB.block(
            "magic_ore",
            Block::new,
            block -> {
                block
                        .initialProperties(() -> Blocks.IRON_ORE)
                        .loot((tables, b) -> tables.add(b, tables.createOreDrop(b, MAGIC_DUST.get())))
                        .item(
                                itemBuilder -> itemBuilder.tooltip(
                                        (collector, stack) -> {
                                            collector.node(
                                                    new SubNode.Basic(
                                                            Component.literal("§5Drops Magic Dust when mined")),
                                                    true,
                                                    false);
                                        }));
            });

    // Block with inline Item configuration
    public static final BlockEntry<TimerBlock> ADVANCED_TIMER = REGISTRYLIB.block(
            "advanced_timer",
            p -> new TimerBlock(p, 4),
            block -> {
                block
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .lang("Advanced Timer")
                        .item(item -> {});
            });

    // === Group System Example ===
    // Group applies shared defaults (lang prefix, block properties) to all entries within it.

    public static final Group TIMER_GROUP = REGISTRYLIB
            .group("timers")
            .langPrefix("Timer")
            .blockProperties(p -> p.strength(5.0F, 6.0F))
            .build();

    public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP.block(
            "tier_1",
            p -> new TimerBlock(p, 1),
            block -> {
                block
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .item(
                                itemBuilder -> itemBuilder.tooltip(
                                        (collector, stack) -> {
                                            collector.node(
                                                    new SubNode.Basic(Component.literal("§aTier 1 Timer"), 0),
                                                    true,
                                                    false);
                                            collector.node(
                                                    new SubNode.Basic(Component.literal("§7Tick interval: 20"), 10));
                                        }));
            });

    public static final BlockEntry<TimerBlock> TIMER_TIER_2 = TIMER_GROUP.block(
            "tier_2",
            p -> new TimerBlock(p, 2),
            block -> {
                block
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .item(
                                itemBuilder -> itemBuilder.tooltip(
                                        (collector, stack) -> {
                                            collector.node(
                                                    new SubNode.Basic(Component.literal("§bTier 2 Timer"), 0),
                                                    true,
                                                    false);
                                            collector.node(
                                                    new SubNode.Basic(Component.literal("§7Tick interval: 10"), 10));
                                        }));
            });

    public static final BlockEntry<TimerBlock> TIMER_TIER_3 = TIMER_GROUP.block(
            "tier_3",
            p -> new TimerBlock(p, 3),
            block -> {
                block
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .item(
                                itemBuilder -> itemBuilder.tooltip(
                                        (collector, stack) -> {
                                            collector.node(
                                                    new SubNode.Basic(Component.literal("§6Tier 3 Timer"), 0),
                                                    true,
                                                    false);
                                            collector.node(
                                                    new SubNode.Basic(Component.literal("§7Tick interval: 5"), 10));
                                        }));
            });

    public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY = REGISTRYLIB.blockEntity(
            "timer",
            TimerBlockEntity::new,
            be -> {
                be.validBlocks(TIMER_TIER_1, TIMER_TIER_2, TIMER_TIER_3)
                        .renderer(() -> TimerBlockEntityRenderer::new);
            });

    // === Fluids ===

    // registrylib 默认流体纹理（灰色可着色）
    private static final Identifier FLUID_STILL = Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
    private static final Identifier FLUID_FLOW = Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

    public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON = REGISTRYLIB.fluid(
            "molten_iron",
            FLUID_STILL,
            FLUID_FLOW,
            fluid -> {
                fluid
                        .properties(p -> p.density(3000).viscosity(6000).temperature(1800))
                        .lang("Molten Iron")
                        .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400);
            });

    public static final FluidEntry<BaseFlowingFluid.Flowing> ACID = REGISTRYLIB.fluid(
            "acid",
            FLUID_STILL,
            FLUID_FLOW,
            fluid -> {
                fluid
                        .properties(p -> p.density(1200).viscosity(800))
                        .lang("Acid")
                        .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFF55DD00);
            });

    // Multi-layer Fluid: consumer-scoped block + bucket configuration
    public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC = REGISTRYLIB.fluid(
            "liquid_magic",
            Identifier.withDefaultNamespace("block/water_still"),
            Identifier.withDefaultNamespace("block/water_flow"),
            fluid -> {
                fluid
                        .properties(p -> p.lightLevel(15).density(500).viscosity(200))
                        .lang("Liquid Magic");
                fluid.block(block -> block.properties(p -> p.lightLevel(s -> 15)));
                fluid.bucket(bucket -> bucket.lang("Liquid Magic Bucket"));
            });

    // === Advancements ===

    static {
        REGISTRYLIB.addDataGenerator(
                ProviderType.ADVANCEMENT,
                adv -> {
                    // --- Tab 1: RegistryCore Basics (3 advancements) ---
                    String cat1 = MOD_ID;

                    AdvancementHolder root = Advancement.Builder.advancement()
                            .display(
                                    TEST_ITEM.get(),
                                    adv.title(cat1, "root", "RegistryCore Basics"),
                                    adv.desc(cat1, "root", "Getting started with RegistryCore"),
                                    Identifier.withDefaultNamespace(
                                            "textures/gui/advancements/backgrounds/stone.png"),
                                    AdvancementType.TASK,
                                    false,
                                    false,
                                    false)
                            .addCriterion(
                                    "has_crafting_table",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
                            .save(adv, Identifier.fromNamespaceAndPath(MOD_ID, "basics/root"));

                    Advancement.Builder.advancement()
                            .parent(root)
                            .display(
                                    TEST_ITEM.get(),
                                    adv.title(cat1, "basics/get_test_item", "First Item"),
                                    adv.desc(cat1, "basics/get_test_item", "Obtain a Test Item"),
                                    null,
                                    AdvancementType.TASK,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_test_item", InventoryChangeTrigger.TriggerInstance.hasItems(TEST_ITEM.get()))
                            .save(adv, Identifier.fromNamespaceAndPath(MOD_ID, "basics/get_test_item"));

                    Advancement.Builder.advancement()
                            .parent(root)
                            .display(
                                    MAGIC_DUST.get(),
                                    adv.title(cat1, "basics/get_magic_dust", "Sparkling Discovery"),
                                    adv.desc(cat1, "basics/get_magic_dust", "Find some Magic Dust"),
                                    null,
                                    AdvancementType.TASK,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_magic_dust",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(MAGIC_DUST.get()))
                            .save(adv, Identifier.fromNamespaceAndPath(MOD_ID, "basics/get_magic_dust"));

                    // --- Tab 2: Advanced (3 advancements) ---
                    AdvancementHolder advRoot = Advancement.Builder.advancement()
                            .display(
                                    MAGIC_ORE.get().asItem(),
                                    adv.title(cat1, "advanced/root", "Advanced Crafting"),
                                    adv.desc(cat1, "advanced/root", "Explore advanced features of RegistryCore"),
                                    Identifier.withDefaultNamespace(
                                            "textures/gui/advancements/backgrounds/nether.png"),
                                    AdvancementType.TASK,
                                    false,
                                    false,
                                    false)
                            .addCriterion(
                                    "has_iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                            .save(adv, Identifier.fromNamespaceAndPath(MOD_ID, "advanced/root"));

                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    MAGIC_ORE.get().asItem(),
                                    adv.title(cat1, "advanced/mine_magic_ore", "Magical Mining"),
                                    adv.desc(cat1, "advanced/mine_magic_ore", "Mine a block of Magic Ore"),
                                    null,
                                    AdvancementType.GOAL,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_magic_ore",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(MAGIC_ORE.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(MOD_ID, "advanced/mine_magic_ore"));

                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    TIMER_TIER_3.get().asItem(),
                                    adv.title(cat1, "advanced/build_timer", "Time Lord"),
                                    adv.desc(cat1, "advanced/build_timer", "Craft a Tier 3 Timer"),
                                    null,
                                    AdvancementType.CHALLENGE,
                                    true,
                                    true,
                                    true)
                            .addCriterion(
                                    "has_timer_3",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(TIMER_TIER_3.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(MOD_ID, "advanced/build_timer"));
                });
    }

    public RegistryLibTest(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("registrylib initializing...");
        LOGGER.info(
                "Registered {} items and {} blocks",
                REGISTRYLIB.getAll(Registries.ITEM).size(),
                REGISTRYLIB.getAll(Registries.BLOCK).size());
    }
}
