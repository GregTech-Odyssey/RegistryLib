package com.gto.registrylib.tooltip;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Tooltip 节点收集器——按 {@link RootNodeRef} 分组收集 {@link SubNode}。 */
public class TooltipNodeCollector {

    /** 节点条目——包装 {@link SubNode} 及其分隔线偏好。 */
    public record NodeEntry(SubNode subNode, boolean separatorAbove, boolean separatorBelow) {}

    final Map<RootNodeRef, List<NodeEntry>> nodesByRoot = new HashMap<>();

    /** 向指定 {@link RootNodeRef} 添加一个 {@link SubNode}。 */
    public void node(
                     RootNodeRef rootRef, SubNode subNode, boolean separatorAbove, boolean separatorBelow) {
        nodesByRoot
                .computeIfAbsent(rootRef, k -> new ArrayList<>())
                .add(new NodeEntry(subNode, separatorAbove, separatorBelow));
    }

    public void node(RootNodeRef rootRef, SubNode subNode) {
        node(rootRef, subNode, false, false);
    }

    /** 向内建默认根节点添加一个 {@link SubNode}（内嵌于原版提示框）。 */
    public void node(SubNode subNode, boolean separatorAbove, boolean separatorBelow) {
        node(TooltipRegistry.defaultRootRef(), subNode, separatorAbove, separatorBelow);
    }

    public void node(SubNode subNode) {
        node(subNode, false, false);
    }

    /** Tooltip 配置回调接口。 */
    @FunctionalInterface
    public interface TooltipConfig {

        void configure(TooltipNodeCollector collector, ItemStack stack);
    }
}
