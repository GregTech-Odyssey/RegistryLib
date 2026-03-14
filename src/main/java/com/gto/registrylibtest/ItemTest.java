package com.gto.registrylibtest;

import com.gto.registrylib.composite.CompositeItem;
import com.gto.registrylib.composite.CompositeItemAttachment;
import com.gto.registrylib.tooltip.RootNodeRef;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.gto.registrylib.util.entry.ItemEntry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 示例：物品注册
 *
 * <p>演示如何使用 RegistryCore 注册物品：
 * <ul>
 *   <li>{@link #TEST_ITEM} — 最简单的物品注册，仅附加一行 Tooltip。
 *   <li>{@link #MAGIC_DUST} — 自定义 lang 名称 + 多行 Tooltip（含优先级排序）。
 *   <li>{@link #COMPOSITE_ITEM} — 使用 {@link CompositeItem} 附件系统，实现右键交互与独立 Tooltip 框。
 * </ul>
 */
public class ItemTest {

    // === Basic Item ===

    /**
     * 最简单的物品注册示例。
     *
     * <p>只需传入 id、工厂方法，再在 consumer 中调用 {@code .tooltip()} 即可。
     */
    public static final ItemEntry<Item> TEST_ITEM = RegistryLibTest.REGISTRYLIB.item(
            "test_item",
            Item::new,
            item -> {
                item.tooltip(
                        (collector, stack) -> {
                            collector.node(new SubNode.Basic(Component.literal("§7A simple test item")));
                        });
            });

    /**
     * 带有自定义显示名称和多行 Tooltip 的物品。
     *
     * <p>通过 {@code .lang()} 指定英文名；Tooltip 中每个 {@link SubNode} 可指定优先级，
     * 数值越小越靠前。第一行以 {@code header=true, footer=false} 标记为标题节点。
     */
    public static final ItemEntry<Item> MAGIC_DUST = RegistryLibTest.REGISTRYLIB.item(
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

    // === Tooltip: 独立框根节点 ===

    /**
     * 通过 {@link TooltipRegistry#rootNode} 注册一个独立的 Tooltip 根节点。
     *
     * <p>可在多个物品的 {@code .tooltip()} 中将节点写入同一个 {@code RootNodeRef}，
     * 游戏内该框会以独立区块渲染，与主 Tooltip 视觉分离。
     */
    public static final RootNodeRef INFO_BOX = TooltipRegistry.rootNode(
            RegistryLibTest.MOD_ID + ":info_box", 10, true);

    // === Composite Item ===

    /**
     * 右键使用时向玩家发送消息的附件示例。
     *
     * <p>继承 {@link CompositeItemAttachment} 并重写 {@code use()} 即可在右键时触发逻辑；
     * 重写 {@code collectTooltipNodes()} 可向主 Tooltip 追加节点。
     */
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

    /**
     * 组合物品示例：通过 {@code .attach()} 绑定一个或多个附件，实现模块化行为复用。
     *
     * <p>此物品还演示了如何向独立根节点 {@link #INFO_BOX} 写入多行信息。
     */
    public static final ItemEntry<CompositeItem> COMPOSITE_ITEM = RegistryLibTest.REGISTRYLIB.item(
            "composite_item",
            CompositeItem::new,
            item -> {
                item.lang("Composite Item")
                        .attach(new MessageAttachment("Hello from CompositeItem!"))
                        .tooltip(
                                (collector, stack) -> {
                                    collector.node(
                                            new SubNode.Basic(Component.literal("§6Composite Item"), 0), true, false);
                                    collector.node(
                                            INFO_BOX,
                                            new SubNode.Basic(Component.literal("§bThis is a separate info box"), 0));
                                    collector.node(
                                            INFO_BOX,
                                            new SubNode.Basic(Component.literal("§7With multiple lines"), 10));
                                });
            });
}
