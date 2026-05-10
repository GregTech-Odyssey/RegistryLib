package com.gto.registrylib.util.color;

public record ArgbColor(int argb) {

    public static ArgbColor of(int argb) {
        return new ArgbColor(argb);
    }

    public int alpha() {
        return argb >>> 24;
    }

    public boolean isOpaque() {
        return alpha() == 0xFF;
    }

    public boolean isTransparent() {
        return alpha() == 0;
    }
}
