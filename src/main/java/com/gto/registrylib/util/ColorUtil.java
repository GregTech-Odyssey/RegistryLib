package com.gto.registrylib.util;

import com.gto.registrylib.RegistryLib;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class ColorUtil {

    public Color generateRandomVibrantColor() {
        float hue = RegistryLib.RANDOM.nextFloat();
        float saturation = 0.8f + RegistryLib.RANDOM.nextFloat() * 0.2f;
        float lightness = 0.5f + RegistryLib.RANDOM.nextFloat() * 0.3f;

        return Color.getHSBColor(hue, saturation, lightness);
    }

    public Color generateRandomMutedColor() {
        float hue = RegistryLib.RANDOM.nextFloat();
        float saturation = 0.4f + RegistryLib.RANDOM.nextFloat() * 0.3f;
        float lightness = 0.4f + RegistryLib.RANDOM.nextFloat() * 0.4f;

        return Color.getHSBColor(hue, saturation, lightness);
    }

    public Color generateRandomColor() {
        return new Color(
                RegistryLib.RANDOM.nextInt(256),
                RegistryLib.RANDOM.nextInt(256),
                RegistryLib.RANDOM.nextInt(256));
    }
}
