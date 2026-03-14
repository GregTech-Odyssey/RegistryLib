package com.gto.registrylibtest;

import com.gto.registrylib.Group;
import com.gto.registrylib.providers.ProviderType;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.util.entry.BlockEntry;

import com.gto.registrylibtest.block.TimerBlock;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * 示例：方块注册
 *
 * <p>演示如何使用 RegistryCore 注册方块，以及如何在方块上挂载物品、Tooltip 和战利品表：
 * <ul>
 *   <li>{@link #TEST_BLOCK} — 最简洁的方块注册，通过 {@code .item()} 同步生成 BlockItem。
 *   <li>{@link #MAGIC_ORE} — 自定义战利品表（挖掘掉落 {@link ItemTest#MAGIC_DUST}）+ 物品 Tooltip。
 *   <li>{@link #ADVANCED_TIMER} — 直接在注册时指定自定义方块子类，并设置 lang 名称。
 *   <li>{@link #TIMER_GROUP} / {@code TIMER_TIER_*} — 使用 {@link Group} 批量共享属性配置
 *       （lang 前缀、方块硬度），逐级扩展成套物品。
 * </ul>
 */
public class BlockTest {

    // === Basic Blocks ===

    /**
     * 最简单的方块注册示例。
     *
     * <p>通过 {@code .initialProperties()} 复用已有方块的属性；{@code .item()} 内可链式配置
     * 对应 BlockItem 的 Tooltip。
     */
    public static final BlockEntry<Block> TEST_BLOCK = RegistryLibTest.REGISTRYLIB.block(
            "test_block",
            Block::new,
            block -> {
                block
                        .initialProperties(() -> Blocks.STONE)
                        .item(
                                itemBuilder -> itemBuilder.tooltip(Component.literal("§7A solid test block")));
            });

    /**
     * 带有自定义战利品表的矿石方块。
     *
     * <p>{@code .loot()} 接受一个 BiConsumer，参数为 {@code (LootTableProvider, Block)}，
     * 可直接调用辅助方法生成常见的矿石掉落逻辑（时运兼容）。
     * 物品 Tooltip 使用带优先级排序的多行写法。
     */
    public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB.block(
            "magic_ore",
            Block::new,
            block -> {
                block
                        .initialProperties(() -> Blocks.IRON_ORE)
                        .loot((tables, b) ->
                                tables.add(b, tables.createOreDrop(b, ItemTest.MAGIC_DUST.get())))
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

    /**
     * 使用自定义方块子类（{@link TimerBlock}）的注册示例。
     *
     * <p>工厂 lambda {@code p -> new TimerBlock(p, 4)} 传入额外构造参数。
     * {@code .lang()} 可直接在此处覆盖英文显示名称。
     */
    public static final BlockEntry<TimerBlock> ADVANCED_TIMER = RegistryLibTest.REGISTRYLIB.block(
            "advanced_timer",
            p -> new TimerBlock(p, 4),
            block -> {
                block
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .lang("Advanced Timer")
                        .item(item -> {});
            });

    // === Group System ===

    /**
     * 使用 {@link Group} 为一批同系方块共享配置。
     *
     * <p>{@code .langPrefix("Timer")} 让所有成员自动获得 "Timer " 前缀名称；
     * {@code .blockProperties()} 为组内方块统一设置硬度/爆炸抗性。
     * 组本身不注册任何对象，仅作元信息容器，由各 {@code .block()} 调用产出实际注册项。
     */
    public static final Group TIMER_GROUP = RegistryLibTest.REGISTRYLIB
            .group("timers")
            .langPrefix("Timer")
            .blockProperties(p -> p.strength(5.0F, 6.0F))
            .build();

    /**
     * Tier 1 — 刻间隔 20 tick。
     *
     * <p>通过 {@link Group#block(String, java.util.function.Function, java.util.function.Consumer)}
     * 注册，会自动继承组的 lang 前缀和方块属性；仅需在 consumer 中追加差异配置。
     */
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

    /** Tier 2 — 刻间隔 10 tick。 */
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

    /** Tier 3 — 刻间隔 5 tick，最高级别。 */
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

    // === Advancements ===

    /**
     * 为本文件中的方块注册成就进度（高级分支）。
     *
     * <p>在 {@link RegistryLibTest} 的 {@code static} 块中调用此方法以接入数据生成。
     * 进度树以铁锭为触发根节点，分支为"挖掘魔法矿石"和"合成 Tier 3 计时器"两个挑战。
     */
    static void registerAdvancements() {
        RegistryLibTest.REGISTRYLIB.addDataGenerator(
                ProviderType.ADVANCEMENT,
                adv -> {
                    String cat = RegistryLibTest.MOD_ID;

                    AdvancementHolder advRoot = Advancement.Builder.advancement()
                            .display(
                                    MAGIC_ORE.get().asItem(),
                                    adv.title(cat, "advanced/root", "Advanced Crafting"),
                                    adv.desc(cat, "advanced/root", "Explore advanced features of RegistryCore"),
                                    Identifier.withDefaultNamespace(
                                            "textures/gui/advancements/backgrounds/nether.png"),
                                    AdvancementType.TASK,
                                    false,
                                    false,
                                    false)
                            .addCriterion(
                                    "has_iron",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/root"));

                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    MAGIC_ORE.get().asItem(),
                                    adv.title(cat, "advanced/mine_magic_ore", "Magical Mining"),
                                    adv.desc(cat, "advanced/mine_magic_ore", "Mine a block of Magic Ore"),
                                    null,
                                    AdvancementType.GOAL,
                                    true,
                                    true,
                                    false)
                            .addCriterion(
                                    "has_magic_ore",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(MAGIC_ORE.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/mine_magic_ore"));

                    Advancement.Builder.advancement()
                            .parent(advRoot)
                            .display(
                                    TIMER_TIER_3.get().asItem(),
                                    adv.title(cat, "advanced/build_timer", "Time Lord"),
                                    adv.desc(cat, "advanced/build_timer", "Craft a Tier 3 Timer"),
                                    null,
                                    AdvancementType.CHALLENGE,
                                    true,
                                    true,
                                    true)
                            .addCriterion(
                                    "has_timer_3",
                                    InventoryChangeTrigger.TriggerInstance.hasItems(TIMER_TIER_3.get().asItem()))
                            .save(adv, Identifier.fromNamespaceAndPath(cat, "advanced/build_timer"));
                });
    }
}
