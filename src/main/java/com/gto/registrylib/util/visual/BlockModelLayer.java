package com.gto.registrylib.util.visual;

import com.gto.registrylib.util.TextureRef;

import org.jetbrains.annotations.NotNull;

public record BlockModelLayer(TextureRef texture, int tintIndex, boolean forceTranslucent) {

    public static final int NO_TINT = -1;

    public static BlockModelLayer untinted(@NotNull TextureRef texture) {
        return new BlockModelLayer(texture, NO_TINT, false);
    }

    public static BlockModelLayer untinted(@NotNull TextureRef texture, boolean forceTranslucent) {
        return new BlockModelLayer(texture, NO_TINT, forceTranslucent);
    }

    public static BlockModelLayer tinted(@NotNull TextureRef texture, int tintIndex) {
        return new BlockModelLayer(texture, tintIndex, false);
    }

    public static BlockModelLayer tinted(
                                         @NotNull TextureRef texture, int tintIndex, boolean forceTranslucent) {
        return new BlockModelLayer(texture, tintIndex, forceTranslucent);
    }

    public boolean hasTint() {
        return tintIndex >= 0;
    }
}
