package com.gto.registrylib.util;

import com.gto.registrylib.util.entry.ItemEntry;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.function.Consumer;

public final class CreativeModeTabModifier implements CreativeModeTab.Output {

    public static final Consumer<CreativeModeTabModifier> DEFAULT = FunctionUtil.noOpConsumer();

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

    public <T extends Item> void acceptEntry(ItemEntry<T> entry) {
        event.accept(entry.readOnlyStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public <T extends Item> void acceptEntry(
                                             ItemEntry<T> entry, CreativeModeTab.TabVisibility visibility) {
        event.accept(entry.readOnlyStack(), visibility);
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        event.accept(stack, visibility);
    }
}
