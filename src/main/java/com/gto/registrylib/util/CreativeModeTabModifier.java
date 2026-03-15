package com.gto.registrylib.util;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

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
}
