package com.gto.registrylibtest.block;

import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * 最简单的方块注册：一个方块 + 对应物品 + 语言。
 *
 * <p>
 * 演示 <b>Approach 2</b>：通过 {@code block(parent, name, factory)} 的两参数形式， 返回类型为 {@link
 * com.gto.registrylibtest.builder.ModBlockBuilder}， 直接调用 {@code .langCn()} 设置简体中文名称。 注意：{@code
 * .langCn()} 必须在任何 {@code BlockBuilder} 方法之前调用。
 */
public class SimpleBlockExample {

    public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
            .block(RegistryLibTest.REGISTRYLIB, "decorative_stone", Block::new)
            .langCn("装饰石") // Approach 2: ModBlockBuilder.langCn()，须最先调用
            .initialProperties(Blocks.STONE)
            .lang("Decorative Stone")
            .simpleItem()
            .register();
}
