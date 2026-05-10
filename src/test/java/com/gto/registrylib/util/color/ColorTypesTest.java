package com.gto.registrylib.util.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ColorTypesTest {

    @Test
    void rgbColorRejectsAlphaBits() {
        assertThrows(IllegalArgumentException.class, () -> RgbColor.of(0xFF336699));
    }

    @Test
    void rgbColorConvertsToOpaqueArgb() {
        RgbColor color = RgbColor.of(0x336699);

        assertEquals(0x336699, color.rgb());
        assertEquals(0xFF336699, color.opaqueArgb());
        assertEquals(ArgbColor.of(0xFF336699), color.opaque());
    }

    @Test
    void argbColorExposesAlphaAndOpacity() {
        ArgbColor transparent = ArgbColor.of(0x00336699);
        ArgbColor opaque = ArgbColor.of(0xFF336699);

        assertEquals(0, transparent.alpha());
        assertTrue(transparent.isTransparent());
        assertFalse(transparent.isOpaque());
        assertEquals(255, opaque.alpha());
        assertTrue(opaque.isOpaque());
    }
}
