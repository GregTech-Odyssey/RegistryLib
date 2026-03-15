package com.gto.registrylibtest.item;

import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.world.item.Item;

/** 最简单的物品注册：一个物品 + 语言。 */
public class SimpleItemExample {

    public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB.item(
            "copper_coin",
            Item::new,
            item -> {
                item.lang("Copper Coin");
            });
}
