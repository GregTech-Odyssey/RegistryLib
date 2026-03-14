package com.gto.registrylib.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.Collections;
import java.util.List;

/**
 * RegistryLib Tooltip 数据容器。
 *
 * <p>
 * 实现 {@link TooltipComponent} 标记接口，在 {@link
 * net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents} 阶段注入 MC tooltip 管线。
 *
 * @param inlineSubNodes 内联子节点列表（属于 {@code separateBox=false} 的 RootNode）， 直接渲染在原版 tooltip 框内，含自动插入的
 *                       {@link SeparatorNode}。
 * @param separateRoots  独立框根节点列表（{@code separateBox=true}）， 每个 {@link ResolvedRoot} 在原版 tooltip
 *                       下方渲染为独立带框区域。
 */
public record RegistryLibTooltipComponent(
                                          List<SubNode> inlineSubNodes, List<ResolvedRoot> separateRoots)
        implements TooltipComponent {

    public RegistryLibTooltipComponent(List<SubNode> inlineSubNodes) {
        this(inlineSubNodes, Collections.emptyList());
    }
}
