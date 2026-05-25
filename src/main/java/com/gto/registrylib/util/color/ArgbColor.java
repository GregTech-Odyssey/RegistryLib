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

    /**
     * Produces a human-readable description of an array of ARGB colors, useful for debug-tint logging
     * in builders.
     */
    public static String describeColors(@org.jetbrains.annotations.Nullable ArgbColor[] colors) {
        if (colors == null) return "unknown";
        if (colors.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < colors.length; i++) {
            if (i > 0) sb.append(", ");
            ArgbColor color = colors[i];
            sb.append("0x")
                    .append(String.format("%08X", color.argb()))
                    .append(color.isOpaque() ? " opaque" : " alpha=" + color.alpha());
        }
        return sb.append(']').toString();
    }
}
