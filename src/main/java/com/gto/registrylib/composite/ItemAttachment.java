package com.gto.registrylib.composite;

import com.gto.registrylib.tooltip.TooltipNodeCollector;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可附加到 {@link IComponentItem} 上的行为组件。
 *
 * <p>
 * 泛型 {@code T} 为宿主 Item 的类型，所有回调方法的 item 参数均为 {@code T}， 编写时可直接访问自定义 Item 子类的成员。
 *
 * <p>
 * 每个方法提供"不干预"的默认实现：
 *
 * <ul>
 * <li><b>短路类</b>（交互）：返回 {@link InteractionResult#PASS}
 * <li><b>首命中类</b>（显示）：返回 {@code null}
 * <li><b>累加类</b>（tooltip / tick）：空方法体
 * </ul>
 */
public class ItemAttachment<T extends IComponentItem<T>> {

    // ── 位掩码标志 ──

    public static final int USE_ON = 1;
    public static final int USE = 1 << 1;
    public static final int IS_BAR_VISIBLE = 1 << 2;
    public static final int GET_BAR_WIDTH = 1 << 3;
    public static final int GET_BAR_COLOR = 1 << 4;
    public static final int INVENTORY_TICK = 1 << 5;
    public static final int COLLECT_TOOLTIP = 1 << 6;

    private static final Class<?> BASE_CLASS = ItemAttachment.class;

    private static final Map<String, Integer> METHOD_FLAGS = Map.of(
            "useOn", USE_ON,
            "use", USE,
            "isBarVisible", IS_BAR_VISIBLE,
            "getBarWidth", GET_BAR_WIDTH,
            "getBarColor", GET_BAR_COLOR,
            "inventoryTick", INVENTORY_TICK,
            "collectTooltipNodes", COLLECT_TOOLTIP);

    private static final Map<Class<?>, Integer> cache = new ConcurrentHashMap<>();

    /** 检测子类实际覆盖了哪些方法，返回覆盖方法的位掩码并集。结果缓存。 */
    public static int detectOverrides(Class<?> clazz) {
        Integer cached = cache.get(clazz);
        if (cached != null) return cached;

        int flags = 0;
        for (Method method : clazz.getMethods()) {
            Integer bit = METHOD_FLAGS.get(method.getName());
            if (bit != null && method.getDeclaringClass() != BASE_CLASS) {
                flags |= bit;
            }
        }
        cache.put(clazz, flags);
        return flags;
    }

    /** 由注册系统在 attach 时设置。 */
    private int overrideFlags = 0;

    /** Returns the bitmask of overridden callback methods. */
    public int getOverrideFlags() {
        return overrideFlags;
    }

    /** Sets the override flags. Called by the registration system during attach. */
    void setOverrideFlags(int flags) {
        this.overrideFlags = flags;
    }

    @SuppressWarnings("unchecked")
    public final <A> A self() {
        return (A) this;
    }

    public void onAttached(T item) {}

    // ── 短路类：首个非 PASS 即返回 ──

    public InteractionResult useOn(T item, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult use(T item, Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    // ── 首命中类：首个非 null 即返回 ──

    public Boolean isBarVisible(T item, ItemStack stack) {
        return null;
    }

    public Integer getBarWidth(T item, ItemStack stack) {
        return null;
    }

    public Integer getBarColor(T item, ItemStack stack) {
        return null;
    }

    // ── 累加类：所有 attachment 均调用 ──

    /** 为 tooltip 贡献节点。 */
    public void collectTooltipNodes(T item, ItemStack stack, TooltipNodeCollector collector) {}

    public void inventoryTick(
                              T item, ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {}
}
