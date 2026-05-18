package com.gto.registrylib.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

/**
 * 内联 SubNode 的 tooltip 数据载体。
 *
 * <p>
 * 只承载属于 {@code separateBox=false} 的 RootNode 的子节点；独立框面板由 {@link RegistryLibPanelComponent}
 * 单独提供，便于让原版 tooltip 管线像处理普通组件一样统一左对齐每一块。
 *
 * @param inlineSubNodes 已按 RootNode 排序、并由 {@link TooltipRegistry} 自动插入了 {@link SeparatorNode}
 *                       的内联节点列表。
 */
public record RegistryLibTooltipComponent(List<SubNode> inlineSubNodes)
        implements TooltipComponent {}
