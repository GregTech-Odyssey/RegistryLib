package com.gto.registrylib.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * 分页控制条的 tooltip 数据载体。
 *
 * <p>
 * 仅在独立框总高度超过可用屏幕高度、需要分页显示时才注入到原版 tooltip 组件列表的末尾。
 */
public record RegistryLibPageControlComponent(int pageOffset, int pageCount) implements TooltipComponent {}
