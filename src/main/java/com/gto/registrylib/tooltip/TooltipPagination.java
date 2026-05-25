package com.gto.registrylib.tooltip;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * tooltip 分页的进程级状态。
 *
 * <p>
 * 之前这些字段散落在 {@code RegistryLibClientTooltip} 内的 static 上，但分页其实是「对当前悬停物品的浏览状态」， 与具体某一帧的
 * ClientTooltipComponent 实例无关，所以把它们集中放到 tooltip 包里更直接、更便于在 gather 和按键处理之间共享。
 */
public final class TooltipPagination {

    private TooltipPagination() {}

    // Pagination state is intentionally global: only one tooltip is visible at a time,
    // and all access is on the Minecraft client render thread.
    private static int pageOffset;
    private static int pageCount = 1;
    private static boolean activeTooltip;
    private static boolean renderedThisFrame;
    private static TooltipTarget activeTarget;

    /** 切换到新的悬停目标时重置 offset；同一个物品继续翻页则保留 offset。 */
    public static void prepareForTarget(ItemStack stack) {
        TooltipTarget target = TooltipTarget.from(stack);
        if (!Objects.equals(activeTarget, target)) {
            pageOffset = 0;
            pageCount = 1;
            activeTarget = target;
        }
        renderedThisFrame = true;
        activeTooltip = true;
    }

    /** 标记本帧没有渲染 tooltip——客户端 tick post 会调用，用于在悬停结束后清空 offset。 */
    public static void resetIfIdle() {
        if (!renderedThisFrame) {
            pageOffset = 0;
            pageCount = 1;
            activeTooltip = false;
            activeTarget = null;
        }
        renderedThisFrame = false;
    }

    public static int pageOffset() {
        return pageOffset;
    }

    /** 由 gather 阶段算出真实页数后回写，便于翻页按键判断边界。 */
    public static void setPageCount(int newCount) {
        pageCount = Math.max(1, newCount);
        if (pageOffset >= pageCount) {
            pageOffset = pageCount - 1;
        }
        if (pageOffset < 0) {
            pageOffset = 0;
        }
    }

    public static int pageCount() {
        return pageCount;
    }

    public static boolean pageUp() {
        if (!activeTooltip || pageCount <= 1 || pageOffset <= 0) return false;
        pageOffset--;
        return true;
    }

    public static boolean pageDown() {
        if (!activeTooltip || pageCount <= 1 || pageOffset >= pageCount - 1) return false;
        pageOffset++;
        return true;
    }

    /**
     * 用 (itemId, sorted components) 作为目标指纹：同一物品但 NBT/组件不同时也会被识别为新目标， 这是分页 offset
     * 是否保留的判定依据。
     */
    private record TooltipTarget(String itemId, List<ComponentValue> components) {

        private static TooltipTarget from(ItemStack stack) {
            DataComponentMap components = stack.getComponents();
            List<ComponentValue> values = new ArrayList<>(components.size());
            for (TypedDataComponent<?> component : components) {
                values.add(
                        new ComponentValue(
                                Objects.toString(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type())),
                                Objects.toString(component.value())));
            }
            values.sort(Comparator.comparing(ComponentValue::typeId));
            return new TooltipTarget(
                    Objects.toString(BuiltInRegistries.ITEM.getKey(stack.getItem())), List.copyOf(values));
        }
    }

    private record ComponentValue(String typeId, String value) {}
}
