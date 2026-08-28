package com.gto.registrylib.util;

import com.gto.registrylib.util.color.ArgbColor;
import com.gto.registrylib.util.color.RgbColor;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegistryLibTintSources {

    /** 1.21.1 常量物品着色器——任何 tintindex 都返回该颜色。 */
    public ItemColor itemConstant(RgbColor color) {
        return (stack, tintIndex) -> color.opaqueArgb();
    }

    public ItemColor itemConstant(ArgbColor color) {
        return (stack, tintIndex) -> color.argb();
    }

    public BlockColor blockConstant(RgbColor color) {
        return blockConstant(color.opaque());
    }

    public BlockColor blockConstant(ArgbColor color) {
        return (state, level, pos, tintIndex) -> color.argb();
    }
}
