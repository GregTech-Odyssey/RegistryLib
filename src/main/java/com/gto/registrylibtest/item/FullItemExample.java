package com.gto.registrylibtest.item;

import com.gto.registrylib.composite.CompositeItem;
import com.gto.registrylib.composite.CompositeItemAttachment;
import com.gto.registrylib.tooltip.RootNodeRef;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipNodeCollector;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 使用 ItemBuilder 全部 API 的复杂物品示例。
 *
 * <p>涵盖：properties / initialProperties / lang / defaultModel / defaultLang /
 * tab / removeTab / model / recipe / tooltip（两种重载）/ attach / tag /
 * 独立 RootNodeRef / CompositeItemAttachment。
 */
public class FullItemExample {

    // ── 独立 Tooltip 根节点 ──────────────────────────────────────────────────

    public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
            RegistryLibTest.MOD_ID + ":detail_box", 10, true);

    // ── CompositeItem 附件 ──────────────────────────────────────────────────

    static class InspectAttachment extends CompositeItemAttachment<CompositeItem> {

        @Override
        public InteractionResult use(
                CompositeItem item, Level level, Player player, InteractionHand hand) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal("Inspecting magic wand..."));
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public void collectTooltipNodes(
                CompositeItem item, ItemStack stack, TooltipNodeCollector collector) {
            collector.node(new SubNode.Basic(Component.literal("§eRight-click to inspect"), 100));
        }
    }

    // ── 注册 ────────────────────────────────────────────────────────────────

    public static final ItemEntry<CompositeItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB
            .item("magic_wand", CompositeItem::new)
            // --- initialProperties: 提供全新的 Properties 作为基础 ---
            .initialProperties(() -> new Item.Properties().stacksTo(1))
            // --- properties: 在已有 Properties 上追加修改 ---
            .properties(p -> p.fireResistant())
            // --- lang: 设置英文显示名称 ---
            .lang("Magic Wand")
            // --- lang (zh_cn): 简体中文显示名称 ---
            .lang(ModRegistryCore.LANG_ZH_CN, "魔法杆")
            // --- defaultModel: 使用默认扁平物品模型 ---
            .defaultModel()
            // --- tab: 额外加入原版工具标签页 ---
            .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
            // --- removeTab: 演示从某个标签页中移除（此处移除后又加回，仅展示 API） ---
            .removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
            // --- recipe: 通过 DataGen 生成一个合成配方 ---
            .recipe((ctx, prov) -> {
                // ShapedRecipeBuilder.shaped(...) 可在此处添加配方
                // 此处留空仅展示 API 调用方式
            })
            // --- tag: 给物品添加原版标签 ---
            .tag(ItemTags.DURABILITY_ENCHANTABLE)
            // --- tooltip（快捷版）: 添加单行 Tooltip ---
            .tooltip(Component.literal("§5A powerful magical artifact"))
            // --- tooltip（完整版）: 动态多行 Tooltip + 独立根节点 ---
            .tooltip((collector, stack) -> {
                collector.node(
                        new SubNode.Basic(Component.literal("§dMagic Wand"), 0), true, false);
                collector.node(
                        new SubNode.Basic(
                                Component.literal("§7Durability: §f" + (stack.getMaxDamage() - stack.getDamageValue())),
                                10));
                // 向独立浮窗写入信息
                collector.node(
                        DETAIL_BOX,
                        new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
                collector.node(
                        DETAIL_BOX,
                        new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
            })
            // --- attach: 绑定 CompositeItem 附件 ---
            .attach(new InspectAttachment())
            .register();
}
