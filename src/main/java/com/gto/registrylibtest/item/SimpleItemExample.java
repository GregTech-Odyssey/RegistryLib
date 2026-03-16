package com.gto.registrylibtest.item;

import com.gto.registrylib.util.entry.ItemEntry;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.world.item.Item;

/**
 * 最简单的物品注册：一个物品 + 语言。
 *
 * <p>
 * 演示 <b>Approach 2</b>：通过 {@code item(parent, name, factory)} 的两参数形式， 返回类型为 {@link
 * com.gto.registrylibtest.builder.ModItemBuilder}， 直接调用 {@code .langCn()} 设置简体中文名称，无需传递 {@code
 * ProviderType} 参数。
 */
public class SimpleItemExample {

    public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
            .item("copper_coin")
            .langCn("铜币") // Approach 2: ModItemBuilder.langCn()
            .lang("Copper Coin")
            .register();
}
