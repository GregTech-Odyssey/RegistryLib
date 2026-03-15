package com.gto.registrylibtest.block;

import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** 最简单的方块注册：一个方块 + 对应物品 + 语言。 */
public class SimpleBlockExample {

    public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
            .block("decorative_stone", Block::new)
            .initialProperties(() -> Blocks.STONE)
            .lang("Decorative Stone")
            .lang(RegistryLibTest.LANG_ZH_CN, "装饰石")
            .simpleItem()
            .register();
}
