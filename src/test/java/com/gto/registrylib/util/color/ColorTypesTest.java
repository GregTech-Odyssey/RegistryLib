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

    // -----------------------------------------------------------------------
    // Additional RGB boundary tests
    // -----------------------------------------------------------------------

    @Test
    void rgbColorMinimumValidValue() {
        RgbColor color = RgbColor.of(0x000000);

        assertEquals(0x000000, color.rgb());
        assertEquals(0xFF000000, color.opaqueArgb());
    }

    @Test
    void rgbColorMaximumValidValue() {
        RgbColor color = RgbColor.of(0xFFFFFF);

        assertEquals(0xFFFFFF, color.rgb());
        assertEquals(0xFFFFFFFF, color.opaqueArgb());
    }

    // -----------------------------------------------------------------------
    // Additional ARGB tests
    // -----------------------------------------------------------------------

    @Test
    void argbColorPartialAlpha() {
        ArgbColor color = ArgbColor.of(0x80336699);

        assertEquals(128, color.alpha());
        assertFalse(color.isTransparent(), "alpha=128 should not be transparent");
        assertFalse(color.isOpaque(), "alpha=128 should not be opaque");
    }

    // -----------------------------------------------------------------------
    // describeColors tests
    // -----------------------------------------------------------------------

    @Test
    void describeColorsNullReturnsUnknown() {
        assertEquals("unknown", ArgbColor.describeColors(null));
    }

    @Test
    void describeColorsEmptyArrayReturnsBrackets() {
        assertEquals("[]", ArgbColor.describeColors(new ArgbColor[0]));
    }

    @Test
    void describeColorsSingleOpaqueColor() {
        ArgbColor color = ArgbColor.of(0xFF336699);
        String result = ArgbColor.describeColors(new ArgbColor[] { color });

        assertTrue(result.startsWith("["), "should start with '['");
        assertTrue(result.endsWith("]"), "should end with ']'");
        assertTrue(result.contains("0xFF336699"), "should contain hex representation");
        assertTrue(result.contains("opaque"), "opaque color should be labelled 'opaque'");
    }

    @Test
    void describeColorsSingleTransparentColor() {
        ArgbColor color = ArgbColor.of(0x80336699);
        String result = ArgbColor.describeColors(new ArgbColor[] { color });

        assertTrue(result.contains("0x80336699"), "should contain hex representation");
        assertTrue(result.contains("alpha=128"), "partial-alpha color should show alpha value");
        assertFalse(result.contains("opaque"), "partial-alpha color should not be labelled opaque");
    }

    @Test
    void describeColorsMultipleColors() {
        ArgbColor opaque = ArgbColor.of(0xFFFF0000);
        ArgbColor partial = ArgbColor.of(0x4000FF00);
        String result = ArgbColor.describeColors(new ArgbColor[] { opaque, partial });

        assertTrue(result.startsWith("["), "should start with '['");
        assertTrue(result.endsWith("]"), "should end with ']'");
        assertTrue(result.contains("0xFFFF0000"), "should contain first color");
        assertTrue(result.contains("0x4000FF00"), "should contain second color");
        assertTrue(result.contains(", "), "multiple colors should be comma-separated");
    }
}
