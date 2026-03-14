package com.gto.registrylib.tooltip;

import com.mojang.datafixers.util.Either;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.*;

/**
 * RegistryLib Tooltip 全局注册表。
 *
 * <p>管理 {@link RootNode} 注册和按物品的 {@link SubNode} 配置回调。
 * 首次 {@link #resolve} 时延迟构建 {@link Item} → 配置列表的查找表。
 */
public final class TooltipRegistry {

    private static final List<PendingEntry> pendingEntries = new ArrayList<>();

    /** 已注册的 RootNode 实例映射。 */
    private static final Map<RootNodeRef, RootNode> rootNodes = new HashMap<>();

    /** 内建默认根节点——内嵌于原版提示框（不独立成框）。 */
    private static final RootNodeRef DEFAULT_ROOT_REF = new RootNodeRef("registrylib:default");

    static {
        rootNodes.put(DEFAULT_ROOT_REF, new RootNode("registrylib:default", 0, false));
    }

    /** 已解析的查找表。 */
    private static Reference2ObjectOpenHashMap<Item, List<TooltipNodeCollector.TooltipConfig>> resolvedMap;

    /** 复用的收集器，避免每次 resolve 创建新实例。 */
    private static final TooltipNodeCollector reusableCollector = new TooltipNodeCollector();

    /** 共享的分隔线实例。 */
    private static final SeparatorNode SEPARATOR = new SeparatorNode();

    private TooltipRegistry() {}

    public static RootNodeRef defaultRootRef() {
        return DEFAULT_ROOT_REF;
    }

    /** 注册一个 {@link RootNode} 实例。 */
    public static void registerRootNode(RootNodeRef ref, RootNode rootNode) {
        rootNodes.put(ref, rootNode);
    }

    /**
     * 注册一个自定义 {@link RootNode} 并返回引用。
     */
    public static RootNodeRef rootNode(String id, int priority, boolean separateBox, int padding,
                                       RootNode.BoxRenderer boxRenderer) {
        RootNodeRef ref = new RootNodeRef(id);
        rootNodes.put(ref, new RootNode(id, priority, separateBox, padding, boxRenderer));
        return ref;
    }

    public static RootNodeRef rootNode(String id, int priority, boolean separateBox) {
        return rootNode(id, priority, separateBox, 4, RootNode.DEFAULT_BOX_RENDERER);
    }

    /** 为指定物品注册一个 tooltip 配置回调。 */
    public static void register(ItemLike itemLike, TooltipNodeCollector.TooltipConfig config) {
        pendingEntries.add(new PendingEntry(itemLike, config));
        resolvedMap = null; // invalidate cache
    }

    private static Reference2ObjectOpenHashMap<Item, List<TooltipNodeCollector.TooltipConfig>> ensureResolved() {
        if (resolvedMap != null) return resolvedMap;

        var grouped = new Reference2ObjectOpenHashMap<Item, List<TooltipNodeCollector.TooltipConfig>>();
        for (var entry : pendingEntries) {
            grouped.computeIfAbsent(entry.itemLike.asItem(), k -> new ArrayList<>()).add(entry.config);
        }
        resolvedMap = grouped;
        return grouped;
    }

    /**
     * 查询指定 {@link ItemStack} 的 tooltip 组件。
     *
     * <p>按 {@link RootNodeRef} 分组收集 {@link SubNode}，在每组内按 priority 排序，
     * 根据节点的分隔线偏好插入分隔符。
     */
    public static RegistryLibTooltipComponent resolve(ItemStack itemStack) {
        var configs = ensureResolved().get(itemStack.getItem());
        if (configs == null) return null;

        var collector = reusableCollector;
        collector.nodesByRoot.clear();
        for (var config : configs) {
            config.configure(collector, itemStack);
        }
        if (collector.nodesByRoot.isEmpty()) return null;

        var inlineSubNodes = new ArrayList<SubNode>();
        var separateRoots = new ArrayList<ResolvedRoot>();

        for (var entry : collector.nodesByRoot.entrySet()) {
            var ref = entry.getKey();
            var entries = entry.getValue();
            if (entries.isEmpty()) continue;
            var rootNode = rootNodes.get(ref);
            if (rootNode == null) continue;

            entries.sort(Comparator.comparingInt(e -> e.subNode().getPriority()));

            boolean isDefault = ref.equals(DEFAULT_ROOT_REF);
            var result = new ArrayList<SubNode>(entries.size() * 2);

            for (int i = 0; i < entries.size(); i++) {
                var nodeEntry = entries.get(i);
                boolean isFirst = i == 0;

                if (isFirst) {
                    if (isDefault && nodeEntry.separatorAbove()) {
                        result.add(SEPARATOR);
                    }
                } else {
                    var prev = entries.get(i - 1);
                    if (prev.separatorBelow() || nodeEntry.separatorAbove()) {
                        result.add(SEPARATOR);
                    }
                }
                result.add(nodeEntry.subNode());
            }

            if (rootNode.isSeparateBox()) {
                separateRoots.add(new ResolvedRoot(rootNode, result));
            } else {
                inlineSubNodes.addAll(result);
            }
        }

        if (inlineSubNodes.isEmpty() && separateRoots.isEmpty()) return null;

        separateRoots.sort(Comparator.comparingInt(r -> r.rootNode().getPriority()));
        return new RegistryLibTooltipComponent(inlineSubNodes, separateRoots);
    }

    private record PendingEntry(ItemLike itemLike, TooltipNodeCollector.TooltipConfig config) {}
}
