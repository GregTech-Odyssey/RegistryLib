package com.gto.registrylib.util.color;

public record RgbColor(int rgb) {

    private static final int RGB_MASK = 0x00FFFFFF;

    public static RgbColor of(int rgb) {
        if ((rgb & ~RGB_MASK) != 0) {
            throw new IllegalArgumentException("RGB color must be 0xRRGGBB: " + Integer.toHexString(rgb));
        }
        return new RgbColor(rgb);
    }

    public int opaqueArgb() {
        return 0xFF000000 | rgb;
    }

    public ArgbColor opaque() {
        return ArgbColor.of(opaqueArgb());
    }
}
