package com.gto.registrylib.util.visual;

import com.gto.registrylib.builders.ItemBuilder;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.color.RgbColor;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ItemVisualPreset {

    void apply(ItemBuilder<?, ?> builder);

    static ItemVisualPreset tintedTemplate(@NotNull String texturePath, @NotNull RgbColor color) {
        return builder -> builder.constantTint(texturePath, color);
    }

    static ItemVisualPreset tintedTemplate(@NotNull TextureRef texture, @NotNull RgbColor color) {
        return builder -> builder.constantTint(texture, color);
    }

    static ItemVisualPreset existingTexture(@NotNull String texturePath) {
        return builder -> builder.existingTexture(texturePath);
    }

    static ItemVisualPreset existingTexture(@NotNull TextureRef texture) {
        return builder -> builder.existingTexture(texture);
    }
}
