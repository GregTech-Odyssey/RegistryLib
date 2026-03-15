package com.gto.registrylib.util;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.function.Supplier;

public final class CreativeModeTabModifier implements CreativeModeTab.Output {

    private final BuildCreativeModeTabContentsEvent event;

    public CreativeModeTabModifier(BuildCreativeModeTabContentsEvent event) {
        this.event = event;
    }

    public FeatureFlagSet getFlags() {
        return event.getFlags();
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return event.getParameters();
    }

    public boolean hasPermissions() {
        return event.hasPermissions();
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        event.accept(stack, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
    }

    public void accept(Supplier<Item> supplier) {
        event.accept(supplier.get(), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
    }
}
