package com.gto.registrylibtest.item;

import com.gto.registrylib.composite.ComponentItem;
import com.gto.registrylib.composite.ItemAttachment;
import com.gto.registrylib.tooltip.RootNodeRef;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipNodeCollector;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.gto.registrylib.util.ImageUtil;
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

import java.awt.Color;

/**
 * 使用 ItemBuilder 全部 API 的复杂物品示例。
 *
 * <p>
 * 涵盖：properties / initialProperties / lang / defaultModel / defaultLang / tab / removeTab /
 * model / recipe / tooltip（两种重载）/ attach / tag / 独立 RootNodeRef / ItemAttachment。
 */
public class FullItemExample {

    // ── 独立 Tooltip 根节点 ──────────────────────────────────────────────────

    public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(RegistryLibTest.MOD_ID + ":detail_box", 10, true);

    private static final RootNodeRef[] STRESS_BOXES = createStressBoxes();

    // ── ComponentItem 附件 ──────────────────────────────────────────────────

    static class InspectAttachment extends ItemAttachment<ComponentItem> {

        @Override
        public InteractionResult use(
                                     ComponentItem item, Level level, Player player, InteractionHand hand) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal("Inspecting magic wand..."));
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public void collectTooltipNodes(
                                        ComponentItem item, ItemStack stack, TooltipNodeCollector collector) {
            collector.node(new SubNode.Basic(Component.literal("§eRight-click to inspect"), 100));
        }
    }

    // ── 注册 ────────────────────────────────────────────────────────────────

    public static final ItemEntry<ComponentItem> MAGIC_WAND = RegistryLibTest.REGISTRYLIB
            .componentItem("magic_wand")
            // --- initialProperties: 提供全新的 Properties 作为基础 ---
            .initialProperties(() -> new Item.Properties().stacksTo(1))
            // --- properties: 在已有 Properties 上追加修改 ---
            .properties(Item.Properties::fireResistant)
            // --- lang: 设置英文显示名称 ---
            .lang("Magic Wand")
            // --- lang (zh_cn): 简体中文显示名称 ---
            .lang(ModRegistryCore.LANG_ZH_CN, "魔法杆")
            .texture(
                    () -> ImageUtil.generateIcon(
                            new Color(167, 92, 255), ImageUtil.STAR, new Color(42, 24, 69)))
            // --- defaultModel: 使用默认扁平物品模型 ---
            .defaultModel()
            // --- addDefaultTab: 如果没有添加任何标签页，注册器会自动添加一个默认的，否则不会添加，如果需要添加，就需要调用此方法 ---
            .addDefaultTab()
            // --- addTab: 加入新的工具标签页 ---
            .addTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
            // --- removeTab: 演示从某个标签页中移除（此处移除后又加回，仅展示 API） ---
            .removeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .addTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
            // --- tag: 给物品添加原版标签 ---
            .addTag(ItemTags.DURABILITY_ENCHANTABLE)
            // --- tooltip（快捷版）: 添加单行 Tooltip ---
            .addTooltip(Component.literal("§5A powerful magical artifact"))
            // --- tooltip（完整版）: 动态多行 Tooltip + 独立根节点 ---
            .addTooltip(
                    (collector, stack) -> {
                        collector.node(
                                new SubNode.Basic(Component.literal("§dMagic Wand"), 0), true, false);
                        collector.node(
                                new SubNode.Basic(
                                        Component.literal(
                                                "§7Durability: §f" + (stack.getMaxDamage() - stack.getDamageValue())),
                                        10));
                        // 向独立浮窗写入信息
                        collector.node(
                                DETAIL_BOX, new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
                        collector.node(
                                DETAIL_BOX, new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
                        for (int i = 1; i <= 28; i++) {
                            collector.node(
                                    DETAIL_BOX,
                                    new SubNode.Basic(
                                            Component.literal("§7Arcane calibration line §f" + i), 10 + i));
                        }
                    })
            // --- attach: 绑定 ComponentItem 附件 ---
            .attach(new InspectAttachment())
            .register();

    public static final ItemEntry<Item> TOOLTIP_STRESS_TESTER = RegistryLibTest.REGISTRYLIB
            .item("tooltip_stress_tester")
            .properties(properties -> properties.stacksTo(1))
            .lang("Tooltip Stress Tester")
            .lang(ModRegistryCore.LANG_ZH_CN, "提示框压力测试器")
            .texture(
                    () -> ImageUtil.generateIcon(
                            new Color(48, 170, 255), ImageUtil.STAR, new Color(8, 28, 55)))
            .defaultModel()
            .addDefaultTab()
            .addTooltip(Component.literal("§6RegistryLib tooltip pagination stress test"))
            .addTooltip(
                    (collector, stack) -> {
                        collector.node(
                                new SubNode.Basic(Component.literal("§e20 roots / 100 subnodes"), 0),
                                true,
                                false);
                        for (int rootIndex = 0; rootIndex < STRESS_BOXES.length; rootIndex++) {
                            RootNodeRef root = STRESS_BOXES[rootIndex];
                            int rootNumber = rootIndex + 1;
                            for (int nodeIndex = 1; nodeIndex <= 5; nodeIndex++) {
                                collector.node(
                                        root,
                                        new SubNode.Basic(
                                                Component.literal(
                                                        "§bRoot " + rootNumber + " §8/ §fSubnode " + nodeIndex + " §7- very long tooltip content for overflow testing"),
                                                nodeIndex));
                            }
                        }
                    })
            .register();

    private static RootNodeRef[] createStressBoxes() {
        RootNodeRef[] roots = new RootNodeRef[20];
        for (int i = 0; i < roots.length; i++) {
            roots[i] = TooltipRegistry.rootNode(
                    RegistryLibTest.MOD_ID + ":tooltip_stress_box_" + (i + 1), 100 + i, true);
        }
        return roots;
    }
}
