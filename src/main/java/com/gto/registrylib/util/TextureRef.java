package com.gto.registrylib.util;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public record TextureRef(ResourceLocation id) {

    public static TextureRef of(@NotNull ResourceLocation id) {
        return new TextureRef(id);
    }

    public static TextureRef mod(@NotNull String modid, @NotNull String path) {
        return new TextureRef(ResourceLocation.fromNamespaceAndPath(modid, normalize(path)));
    }

    public static TextureRef mc(@NotNull String path) {
        return new TextureRef(ResourceLocation.withDefaultNamespace(normalize(path)));
    }

    public TextureRef withPrefix(@NotNull String prefix) {
        return new TextureRef(id.withPrefix(prefix));
    }

    public TextureRef withSuffix(@NotNull String suffix) {
        return new TextureRef(id.withSuffix(suffix));
    }

    private static String normalize(String path) {
        String normalized = path;
        if (normalized.startsWith("assets/")) {
            int textures = normalized.indexOf("/textures/");
            if (textures >= 0) {
                normalized = normalized.substring(textures + "/textures/".length());
            }
        }
        if (normalized.startsWith("textures/")) {
            normalized = normalized.substring("textures/".length());
        }
        if (normalized.endsWith(".png")) {
            normalized = normalized.substring(0, normalized.length() - ".png".length());
        }
        return normalized;
    }
}
