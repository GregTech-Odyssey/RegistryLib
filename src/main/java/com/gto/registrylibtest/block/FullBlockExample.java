package com.gto.registrylibtest.block;

import com.gto.registrylib.Group;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.item.SimpleItemExample;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * 使用 BlockBuilder 全部 API 的复杂方块示例。
 *
 * <p>涵盖：initialProperties / properties / lang / simpleItem /
 * item（自定义配置）/ defaultBlockstate / defaultLang / defaultLoot /
 * blockstate / loot / recipe / tag / blockEntity（内联）/ Group 系统。
 */
public class FullBlockExample {

    // ── 使用全部 API 的单方块 ───────────────────────────────────────────────

    public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB
            .block("magic_ore", Block::new)
            // --- initialProperties: 复用已有方块的属性 ---
            .initialProperties(() -> Blocks.IRON_ORE)
            // --- properties: 在已有基础上追加修改 ---
            .properties(p -> p.strength(4.0F, 5.0F).requiresCorrectToolForDrops())
            // --- lang: 设置英文显示名称 ---
            .lang("Magic Ore")
            // --- lang (zh_cn): 设置简体中文显示名称 ---
            .lang(RegistryLibTest.LANG_ZH_CN, "魔法矿石")
            // --- loot: 自定义战利品表（矿石掉落逻辑） ---
            .loot((tables, b) ->
                    tables.add(b, tables.createOreDrop(b, SimpleItemExample.COPPER_COIN.get())))
            // --- tag: 给方块添加原版标签 ---
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
            // --- recipe: 通过 DataGen 生成配方 ---
            .recipe((ctx, prov) -> {
                // 此处可添加 ShapedRecipeBuilder 等配方生成逻辑
            })
            // --- item: 自定义 BlockItem 配置，含 Tooltip ---
            .item(item -> item
                .tooltip((collector, stack) -> {
                    collector.node(
                            new SubNode.Basic(Component.literal("§5Drops coins when mined")),
                            true, false);
                })
            )
            .register();

    // ── 使用自定义方块子类 ──────────────────────────────────────────────────

    public static final BlockEntry<TimerBlock> STANDALONE_TIMER = RegistryLibTest.REGISTRYLIB
            .block("standalone_timer", p -> new TimerBlock(p, 4))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .lang("Standalone Timer")
            .lang(RegistryLibTest.LANG_ZH_CN, "独立计时器")
            .defaultLoot()
            .defaultBlockstate()
            .simpleItem()
            .register();

    // ── Group 系统：批量共享配置 ────────────────────────────────────────────

    public static final Group TIMER_GROUP = RegistryLibTest.REGISTRYLIB
            .group("timers")
            .langPrefix("Timer")
            .blockProperties(p -> p.strength(5.0F, 6.0F))
            .build();

    public static final BlockEntry<TimerBlock> TIMER_TIER_1 = TIMER_GROUP
            .block("tier_1", p -> new TimerBlock(p, 1))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .item(item -> item
                .tooltip((collector, stack) -> {
                    collector.node(new SubNode.Basic(Component.literal("§aTier 1"), 0), true, false);
                    collector.node(new SubNode.Basic(Component.literal("§7Tick interval: 20"), 10));
                })
            )
            .register();

    public static final BlockEntry<TimerBlock> TIMER_TIER_2 = TIMER_GROUP
            .block("tier_2", p -> new TimerBlock(p, 2))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .item(item -> item
                .tooltip((collector, stack) -> {
                    collector.node(new SubNode.Basic(Component.literal("§bTier 2"), 0), true, false);
                    collector.node(new SubNode.Basic(Component.literal("§7Tick interval: 10"), 10));
                })
            )
            .register();

    public static final BlockEntry<TimerBlock> TIMER_TIER_3 = TIMER_GROUP
            .block("tier_3", p -> new TimerBlock(p, 3))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .item(item -> item
                .tooltip((collector, stack) -> {
                    collector.node(new SubNode.Basic(Component.literal("§6Tier 3"), 0), true, false);
                    collector.node(new SubNode.Basic(Component.literal("§7Tick interval: 5"), 10));
                })
            )
            .register();
}
