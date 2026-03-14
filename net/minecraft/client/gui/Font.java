package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.EmptyArea;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class Font implements net.neoforged.neoforge.client.extensions.IFontExtension {
    private static final float EFFECT_DEPTH = 0.01F;
    private static final float OVER_EFFECT_DEPTH = 0.01F;
    private static final float UNDER_EFFECT_DEPTH = -0.01F;
    public static final float SHADOW_DEPTH = 0.03F;
    public final int lineHeight = 9;
    private final RandomSource random = RandomSource.create();
    private final Font.Provider provider;
    private final StringSplitter splitter;
    /** Neo: enables linear filtering on text */
    public boolean enableTextTextureLinearFiltering = false;

    public Font(Font.Provider provider) {
        this.provider = provider;
        this.splitter = new StringSplitter((codepoint, style) -> this.getGlyphSource(style.getFont()).getGlyph(codepoint).info().getAdvance(style.isBold()));
    }

    private GlyphSource getGlyphSource(FontDescription fontLocation) {
        return this.provider.glyphs(fontLocation);
    }

    public String bidirectionalShaping(String text) {
        try {
            Bidi bidi = new Bidi(new ArabicShaping(8).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException var3) {
            return text;
        }
    }

    public void drawInBatch(
        String str,
        float x,
        float y,
        int color,
        boolean dropShadow,
        Matrix4f pose,
        MultiBufferSource bufferSource,
        Font.DisplayMode displayMode,
        int backgroundColor,
        int packedLightCoords
    ) {
        Font.PreparedText preparedText = this.prepareText(str, x, y, color, dropShadow, backgroundColor);
        preparedText.visit(Font.GlyphVisitor.forMultiBufferSource(bufferSource, pose, displayMode, packedLightCoords));
    }

    public void drawInBatch(
        Component str,
        float x,
        float y,
        int color,
        boolean dropShadow,
        Matrix4f pose,
        MultiBufferSource bufferSource,
        Font.DisplayMode displayMode,
        int backgroundColor,
        int packedLightCoords
    ) {
        Font.PreparedText preparedText = this.prepareText(str.getVisualOrderText(), x, y, color, dropShadow, false, backgroundColor);
        preparedText.visit(Font.GlyphVisitor.forMultiBufferSource(bufferSource, pose, displayMode, packedLightCoords));
    }

    public void drawInBatch(
        FormattedCharSequence str,
        float x,
        float y,
        int color,
        boolean dropShadow,
        Matrix4f pose,
        MultiBufferSource bufferSource,
        Font.DisplayMode displayMode,
        int backgroundColor,
        int packedLightCoords
    ) {
        Font.PreparedText preparedText = this.prepareText(str, x, y, color, dropShadow, false, backgroundColor);
        preparedText.visit(Font.GlyphVisitor.forMultiBufferSource(bufferSource, pose, displayMode, packedLightCoords));
    }

    public void drawInBatch8xOutline(
        FormattedCharSequence str, float x, float y, int color, int outlineColor, Matrix4f pose, MultiBufferSource bufferSource, int packedLightCoords
    ) {
        Font.PreparedTextBuilder outlineOutput = new Font.PreparedTextBuilder(0.0F, 0.0F, outlineColor, false, false);

        for (int xo = -1; xo <= 1; xo++) {
            for (int yo = -1; yo <= 1; yo++) {
                if (xo != 0 || yo != 0) {
                    float[] startX = new float[]{x};
                    int finalXo = xo;
                    int finalYo = yo;
                    str.accept((position, style, codepoint) -> {
                        boolean bold = style.isBold();
                        BakedGlyph glyph = this.getGlyph(codepoint, style);
                        outlineOutput.x = startX[0] + finalXo * glyph.info().getShadowOffset();
                        outlineOutput.y = y + finalYo * glyph.info().getShadowOffset();
                        startX[0] += glyph.info().getAdvance(bold);
                        return outlineOutput.accept(position, style.withColor(outlineColor), glyph);
                    });
                }
            }
        }

        Font.GlyphVisitor outlineGlyphVisitor = Font.GlyphVisitor.forMultiBufferSource(bufferSource, pose, Font.DisplayMode.NORMAL, packedLightCoords);

        for (TextRenderable.Styled glyphInstance : outlineOutput.glyphs) {
            outlineGlyphVisitor.acceptGlyph(glyphInstance);
        }

        Font.PreparedTextBuilder primaryOutput = new Font.PreparedTextBuilder(x, y, color, false, true);
        str.accept(primaryOutput);
        primaryOutput.visit(Font.GlyphVisitor.forMultiBufferSource(bufferSource, pose, Font.DisplayMode.POLYGON_OFFSET, packedLightCoords));
    }

    private BakedGlyph getGlyph(int codepoint, Style style) {
        GlyphSource glyphSource = this.getGlyphSource(style.getFont());
        BakedGlyph glyph = glyphSource.getGlyph(codepoint);
        if (style.isObfuscated() && codepoint != 32) {
            int targetWidth = Mth.ceil(glyph.info().getAdvance(false));
            glyph = glyphSource.getRandomGlyph(this.random, targetWidth);
        }

        return glyph;
    }

    public Font.PreparedText prepareText(String text, float x, float y, int originalColor, boolean drawShadow, int backgroundColor) {
        if (this.isBidirectional()) {
            text = this.bidirectionalShaping(text);
        }

        Font.PreparedTextBuilder output = new Font.PreparedTextBuilder(x, y, originalColor, backgroundColor, drawShadow, false);
        StringDecomposer.iterateFormatted(text, Style.EMPTY, output);
        return output;
    }

    public Font.PreparedText prepareText(
        FormattedCharSequence text, float x, float y, int originalColor, boolean drawShadow, boolean includeEmpty, int backgroundColor
    ) {
        Font.PreparedTextBuilder builder = new Font.PreparedTextBuilder(x, y, originalColor, backgroundColor, drawShadow, includeEmpty);
        text.accept(builder);
        return builder;
    }

    public int width(String str) {
        return Mth.ceil(this.splitter.stringWidth(str));
    }

    public int width(FormattedText text) {
        return Mth.ceil(this.splitter.stringWidth(text));
    }

    public int width(FormattedCharSequence text) {
        return Mth.ceil(this.splitter.stringWidth(text));
    }

    public String plainSubstrByWidth(String str, int width, boolean reverse) {
        return reverse ? this.splitter.plainTailByWidth(str, width, Style.EMPTY) : this.splitter.plainHeadByWidth(str, width, Style.EMPTY);
    }

    public String plainSubstrByWidth(String str, int width) {
        return this.splitter.plainHeadByWidth(str, width, Style.EMPTY);
    }

    public FormattedText substrByWidth(FormattedText text, int width) {
        return this.splitter.headByWidth(text, width, Style.EMPTY);
    }

    public int wordWrapHeight(FormattedText input, int textWidth) {
        return 9 * this.splitter.splitLines(input, textWidth, Style.EMPTY).size();
    }

    public List<FormattedCharSequence> split(FormattedText input, int maxWidth) {
        return Language.getInstance().getVisualOrder(this.splitter.splitLines(input, maxWidth, Style.EMPTY));
    }

    public List<FormattedText> splitIgnoringLanguage(FormattedText input, int maxWidth) {
        return this.splitter.splitLines(input, maxWidth, Style.EMPTY);
    }

    public boolean isBidirectional() {
        return Language.getInstance().isDefaultRightToLeft();
    }

    public StringSplitter getSplitter() {
        return this.splitter;
    }

    @Override public Font self() { return this; }

    @OnlyIn(Dist.CLIENT)
    public static enum DisplayMode {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;
    }

    @OnlyIn(Dist.CLIENT)
    public interface GlyphVisitor {
        static Font.GlyphVisitor forMultiBufferSource(MultiBufferSource bufferSource, Matrix4f pose, Font.DisplayMode displayMode, int lightCoords) {
            return new Font.GlyphVisitor() {
                @Override
                public void acceptGlyph(TextRenderable.Styled glyph) {
                    this.render(glyph);
                }

                @Override
                public void acceptEffect(TextRenderable effect) {
                    this.render(effect);
                }

                private void render(TextRenderable glyph) {
                    VertexConsumer buffer = bufferSource.getBuffer(glyph.renderType(displayMode));
                    glyph.render(pose, buffer, lightCoords, false);
                }
            };
        }

        default void acceptGlyph(TextRenderable.Styled glyph) {
        }

        default void acceptEffect(TextRenderable effect) {
        }

        default void acceptEmptyArea(EmptyArea empty) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface PreparedText {
        void visit(Font.GlyphVisitor visitor);

        @Nullable ScreenRectangle bounds();
    }

    @OnlyIn(Dist.CLIENT)
    private class PreparedTextBuilder implements Font.PreparedText, FormattedCharSink {
        private final boolean drawShadow;
        private final int color;
        private final int backgroundColor;
        private final boolean includeEmpty;
        private float x;
        private float y;
        private float left;
        private float top;
        private float right;
        private float bottom;
        private float backgroundLeft;
        private float backgroundTop;
        private float backgroundRight;
        private float backgroundBottom;
        private final List<TextRenderable.Styled> glyphs;
        private @Nullable List<TextRenderable> effects;
        private @Nullable List<EmptyArea> emptyAreas;

        public PreparedTextBuilder(float x, float y, int color, boolean drawShadow, boolean includeEmpty) {
            this(x, y, color, 0, drawShadow, includeEmpty);
        }

        public PreparedTextBuilder(float x, float y, int color, int backgroundColor, boolean drawShadow, boolean includeEmpty) {
            Objects.requireNonNull(Font.this);
            super();
            this.left = Float.MAX_VALUE;
            this.top = Float.MAX_VALUE;
            this.right = -Float.MAX_VALUE;
            this.bottom = -Float.MAX_VALUE;
            this.backgroundLeft = Float.MAX_VALUE;
            this.backgroundTop = Float.MAX_VALUE;
            this.backgroundRight = -Float.MAX_VALUE;
            this.backgroundBottom = -Float.MAX_VALUE;
            this.glyphs = new ArrayList<>();
            this.x = x;
            this.y = y;
            this.drawShadow = drawShadow;
            this.color = color;
            this.backgroundColor = backgroundColor;
            this.includeEmpty = includeEmpty;
            this.markBackground(x, y, 0.0F);
        }

        private void markSize(float left, float top, float right, float bottom) {
            this.left = Math.min(this.left, left);
            this.top = Math.min(this.top, top);
            this.right = Math.max(this.right, right);
            this.bottom = Math.max(this.bottom, bottom);
        }

        private void markBackground(float x, float y, float advance) {
            if (ARGB.alpha(this.backgroundColor) != 0) {
                this.backgroundLeft = Math.min(this.backgroundLeft, x - 1.0F);
                this.backgroundTop = Math.min(this.backgroundTop, y - 1.0F);
                this.backgroundRight = Math.max(this.backgroundRight, x + advance);
                this.backgroundBottom = Math.max(this.backgroundBottom, y + 9.0F);
                this.markSize(this.backgroundLeft, this.backgroundTop, this.backgroundRight, this.backgroundBottom);
            }
        }

        private void addGlyph(TextRenderable.Styled instance) {
            this.glyphs.add(instance);
            this.markSize(instance.left(), instance.top(), instance.right(), instance.bottom());
        }

        private void addEffect(TextRenderable effect) {
            if (this.effects == null) {
                this.effects = new ArrayList<>();
            }

            this.effects.add(effect);
            this.markSize(effect.left(), effect.top(), effect.right(), effect.bottom());
        }

        private void addEmptyGlyph(EmptyArea empty) {
            if (this.emptyAreas == null) {
                this.emptyAreas = new ArrayList<>();
            }

            this.emptyAreas.add(empty);
        }

        @Override
        public boolean accept(int position, Style style, int c) {
            BakedGlyph glyph = Font.this.getGlyph(c, style);
            return this.accept(position, style, glyph);
        }

        public boolean accept(int position, Style style, BakedGlyph glyph) {
            GlyphInfo glyphInfo = glyph.info();
            boolean bold = style.isBold();
            TextColor styleColor = style.getColor();
            int textColor = this.getTextColor(styleColor);
            int shadowColor = this.getShadowColor(style, textColor);
            float advance = glyphInfo.getAdvance(bold);
            float effectX0 = position == 0 ? this.x - 1.0F : this.x;
            float shadowOffset = glyphInfo.getShadowOffset();
            float boldOffset = bold ? glyphInfo.getBoldOffset() : 0.0F;
            TextRenderable.Styled instance = glyph.createGlyph(this.x, this.y, textColor, shadowColor, style, boldOffset, shadowOffset);
            if (instance != null) {
                this.addGlyph(instance);
            } else if (this.includeEmpty) {
                this.addEmptyGlyph(new EmptyArea(this.x, this.y, advance, 7.0F, 9.0F, style));
            }

            this.markBackground(this.x, this.y, advance);
            if (style.isStrikethrough()) {
                this.addEffect(
                    Font.this.provider
                        .effect()
                        .createEffect(effectX0, this.y + 4.5F - 1.0F, this.x + advance, this.y + 4.5F, 0.01F, textColor, shadowColor, shadowOffset)
                );
            }

            if (style.isUnderlined()) {
                this.addEffect(
                    Font.this.provider
                        .effect()
                        .createEffect(effectX0, this.y + 9.0F - 1.0F, this.x + advance, this.y + 9.0F, 0.01F, textColor, shadowColor, shadowOffset)
                );
            }

            this.x += advance;
            return true;
        }

        @Override
        public void visit(Font.GlyphVisitor visitor) {
            if (ARGB.alpha(this.backgroundColor) != 0) {
                visitor.acceptEffect(
                    Font.this.provider
                        .effect()
                        .createEffect(
                            this.backgroundLeft, this.backgroundTop, this.backgroundRight, this.backgroundBottom, -0.01F, this.backgroundColor, 0, 0.0F
                        )
                );
            }

            for (TextRenderable.Styled glyph : this.glyphs) {
                visitor.acceptGlyph(glyph);
            }

            if (this.effects != null) {
                for (TextRenderable effect : this.effects) {
                    visitor.acceptEffect(effect);
                }
            }

            if (this.emptyAreas != null) {
                for (EmptyArea emptyArea : this.emptyAreas) {
                    visitor.acceptEmptyArea(emptyArea);
                }
            }
        }

        private int getTextColor(@Nullable TextColor textColor) {
            if (textColor != null) {
                int alpha = ARGB.alpha(this.color);
                int rgb = textColor.getValue();
                return ARGB.color(alpha, rgb);
            } else {
                return this.color;
            }
        }

        private int getShadowColor(Style style, int textColor) {
            Integer shadow = style.getShadowColor();
            if (shadow != null) {
                float textAlpha = ARGB.alphaFloat(textColor);
                float shadowAlpha = ARGB.alphaFloat(shadow);
                return textAlpha != 1.0F ? ARGB.color(ARGB.as8BitChannel(textAlpha * shadowAlpha), shadow) : shadow;
            } else {
                return this.drawShadow ? ARGB.scaleRGB(textColor, 0.25F) : 0;
            }
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            if (!(this.left >= this.right) && !(this.top >= this.bottom)) {
                int left = Mth.floor(this.left);
                int top = Mth.floor(this.top);
                int right = Mth.ceil(this.right);
                int bottom = Mth.ceil(this.bottom);
                return new ScreenRectangle(left, top, right - left, bottom - top);
            } else {
                return null;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface Provider {
        GlyphSource glyphs(FontDescription font);

        EffectGlyph effect();
    }
}
