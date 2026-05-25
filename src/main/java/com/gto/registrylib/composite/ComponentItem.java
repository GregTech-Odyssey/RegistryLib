package com.gto.registrylib.composite;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合物品——通过 {@link ItemAttachment} 挂载多个可插拔行为组件。
 *
 * <p>
 * 短路委托、首命中委托、累加委托三种模式自动分发到所有已挂载的附件。 位掩码机制跳过未覆盖的方法，避免不必要的调用。
 */
public class ComponentItem extends Item implements IComponentItem<ComponentItem> {

    private final List<ItemAttachment<ComponentItem>> attachments = new ArrayList<>();
    private int combinedFlags = 0;

    public ComponentItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemAttachment<ComponentItem>> getAttachments() {
        return attachments;
    }

    /** 挂载附件。自动检测该 attachment 类覆盖了哪些方法，更新位掩码。 */
    public void attachAttachment(ItemAttachment<ComponentItem> attachment) {
        attachment.setOverrideFlags(ItemAttachment.detectOverrides(attachment.getClass()));
        combinedFlags |= attachment.getOverrideFlags();
        attachments.add(attachment);
        attachment.onAttached(this);
    }

    // ── 短路委托 ──

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if ((combinedFlags & ItemAttachment.USE_ON) == 0) return super.useOn(context);
        for (var att : attachments) {
            if ((att.getOverrideFlags() & ItemAttachment.USE_ON) == 0) continue;
            var result = att.useOn(this, context);
            if (result != InteractionResult.PASS) return result;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if ((combinedFlags & ItemAttachment.USE) == 0) return super.use(level, player, hand);
        for (var att : attachments) {
            if ((att.getOverrideFlags() & ItemAttachment.USE) == 0) continue;
            var result = att.use(this, level, player, hand);
            if (result != InteractionResult.PASS) return result;
        }
        return super.use(level, player, hand);
    }

    // ── 首命中委托 ──

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if ((combinedFlags & ItemAttachment.IS_BAR_VISIBLE) == 0) return super.isBarVisible(stack);
        for (var att : attachments) {
            if ((att.getOverrideFlags() & ItemAttachment.IS_BAR_VISIBLE) == 0) continue;
            Boolean result = att.isBarVisible(this, stack);
            if (result != null) return result;
        }
        return super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if ((combinedFlags & ItemAttachment.GET_BAR_WIDTH) == 0) return super.getBarWidth(stack);
        for (var att : attachments) {
            if ((att.getOverrideFlags() & ItemAttachment.GET_BAR_WIDTH) == 0) continue;
            Integer result = att.getBarWidth(this, stack);
            if (result != null) return result;
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if ((combinedFlags & ItemAttachment.GET_BAR_COLOR) == 0) return super.getBarColor(stack);
        for (var att : attachments) {
            if ((att.getOverrideFlags() & ItemAttachment.GET_BAR_COLOR) == 0) continue;
            Integer result = att.getBarColor(this, stack);
            if (result != null) return result;
        }
        return super.getBarColor(stack);
    }

    // ── 累加委托 ──

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if ((combinedFlags & ItemAttachment.INVENTORY_TICK) == 0) return;
        for (var att : attachments) {
            if ((att.getOverrideFlags() & ItemAttachment.INVENTORY_TICK) == 0) continue;
            att.inventoryTick(this, stack, level, entity, slot);
        }
    }
}
