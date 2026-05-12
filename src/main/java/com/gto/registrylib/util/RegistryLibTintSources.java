package com.gto.registrylib.util;

import com.gto.registrylib.util.color.ArgbColor;
import com.gto.registrylib.util.color.RgbColor;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.model.ItemModelUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegistryLibTintSources {

    public ItemTintSource itemConstant(RgbColor color) {
        return ItemModelUtils.constantTint(color.opaqueArgb());
    }

    public static ItemTintSource itemConstantStatic(RgbColor color) {
        return ItemModelUtils.constantTint(color.opaqueArgb());
    }

    public BlockTintSource blockConstant(RgbColor color) {
        return blockConstant(color.opaque());
    }

    public static BlockTintSource blockConstantStatic(RgbColor color) {
        return blockConstantStatic(color.opaque());
    }

    public BlockTintSource blockConstant(ArgbColor color) {
        return BlockTintSources.constant(color.argb());
    }

    public static BlockTintSource blockConstantStatic(ArgbColor color) {
        return BlockTintSources.constant(color.argb());
    }
}
