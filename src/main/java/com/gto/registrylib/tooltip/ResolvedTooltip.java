package com.gto.registrylib.tooltip;

import java.util.List;

/**
 * {@link TooltipRegistry#resolve} 的返回值——尚未拆分成 ClientTooltipComponent 的中间结构。
 *
 * <p>
 * 把内联与独立框分开返回，便于 gather 阶段把它们映射为不同的 ClientTooltipComponent：
 *
 * <ul>
 * <li>{@link #inlineSubNodes} → 一个 {@link RegistryLibTooltipComponent}
 * <li>{@link #separateRoots} → 每个一个 {@link RegistryLibPanelComponent}
 * </ul>
 */
public record ResolvedTooltip(List<SubNode> inlineSubNodes, List<ResolvedRoot> separateRoots) {

    public boolean isEmpty() {
        return inlineSubNodes.isEmpty() && separateRoots.isEmpty();
    }
}
