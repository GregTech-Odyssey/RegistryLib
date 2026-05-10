package com.gto.registrylib.util.visual;

import com.gto.registrylib.builders.BlockBuilder;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.color.RgbColor;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BlockVisualPreset {

    void apply(BlockBuilder<?, ?> builder);

    static BlockVisualPreset constantTintedCube(
                                                @NotNull String texturePath, @NotNull RgbColor color) {
        return builder -> builder.constantTint(texturePath, color);
    }

    static BlockVisualPreset constantTintedCube(
                                                @NotNull TextureRef texture, @NotNull RgbColor color) {
        return builder -> builder.constantTint(texture, color);
    }

    static BlockVisualPreset constantTintedCube(
                                                @NotNull String texturePath, @NotNull RgbColor color, int tintIndex) {
        return builder -> builder.tintedCube(texturePath, tintIndex).constantTint(color);
    }

    static BlockVisualPreset constantTintedCube(
                                                @NotNull TextureRef texture, @NotNull RgbColor color, int tintIndex) {
        return builder -> builder.tintedCube(texture, tintIndex).constantTint(color);
    }

    static BlockVisualPreset layeredCube(
                                         @NotNull TextureRef particle, @NotNull RgbColor color, @NotNull BlockModelLayer... layers) {
        return builder -> builder.layeredCube(particle, layers).constantTint(color);
    }

    static BlockVisualPreset existingTexture(@NotNull String texturePath) {
        return builder -> builder.existingTexture(texturePath);
    }

    static BlockVisualPreset existingTexture(@NotNull TextureRef texture) {
        return builder -> builder.existingTexture(texture);
    }
}
