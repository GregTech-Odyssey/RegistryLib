package com.gto.registrylib.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * 独立框面板的 tooltip 数据载体。
 *
 * <p>
 * 每一个 {@code separateBox=true} 的 {@link ResolvedRoot} 会包装成一个独立的 {@link TooltipComponent}， 让原版
 * {@code GuiGraphicsExtractor.tooltip()} 像处理普通组件那样把它们与标题/内联文本一起在同一个左对齐基线 上垂直堆叠——避免把多块拼到同一个
 * ClientTooltipComponent 里再手算 X 偏移导致的对齐错乱。
 */
public record RegistryLibPanelComponent(ResolvedRoot resolved) implements TooltipComponent {}
