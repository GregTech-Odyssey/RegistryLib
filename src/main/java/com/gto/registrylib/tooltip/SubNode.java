package com.gto.registrylib.tooltip;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

import org.joml.Matrix4f;

/**
 * Tooltip 叶子节点——{@link RootNode} 内的渲染单元。
 *
 * <p>
 * 每个节点拥有 {@link #priority}（升序排列，值越小在所属 RootNode 中位置越靠上）。 没有子节点列表——所有 SubNode 都是叶子，层级结构通过 {@link
 * RootNode} 管理。
 *
 * <p>
 * 节点通过 {@link #getHeight} / {@link #getWidth} 提供自身尺寸， 通过 {@link #renderImage} / {@link
 * #renderText} 渲染自身内容。
 */
public abstract class SubNode {

    private final int priority;

    protected SubNode(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    /** 此节点自身的高度，单位：像素。 */
    public abstract int getHeight(Font font);

    /** 此节点自身的宽度，单位：像素。 */
    public abstract int getWidth(Font font);

    /**
     * 渲染此节点的图形元素（如分隔线、色块）。在 1.21.1 的 tooltip 管线中，image pass 在 text pass 之后执行，
     * 因此图形元素会绘制在文本之上；需要压在文本下面的背景请使用 {@link #renderText} 中的 quad。 默认无操作。
     */
    public void renderImage(
                            Font font,
                            int x,
                            int y,
                            int width,
                            int height,
                            net.minecraft.client.gui.GuiGraphics graphics) {}

    /** 渲染此节点的文本内容（text pass）。默认无操作。 */
    public void renderText(
                           Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {}

    /** 在 text pass 中绘制一个实心矩形（用于压在文本下方的背景）。 */
    protected static void fill(
                               Matrix4f matrix,
                               MultiBufferSource.BufferSource bufferSource,
                               float x0,
                               float y0,
                               float x1,
                               float y1,
                               int color) {
        if (x0 < x1 && y0 < y1) {
            VertexConsumer buffer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.gui());
            buffer.addVertex(matrix, x0, y0, 0.0F).setColor(color);
            buffer.addVertex(matrix, x0, y1, 0.0F).setColor(color);
            buffer.addVertex(matrix, x1, y1, 0.0F).setColor(color);
            buffer.addVertex(matrix, x1, y0, 0.0F).setColor(color);
        }
    }

    /** 在 text pass 中绘制一个垂直渐变矩形（用于压在文本下方的边框/背景）。 */
    protected static void fillGradient(
                                       Matrix4f matrix,
                                       MultiBufferSource.BufferSource bufferSource,
                                       float x0,
                                       float y0,
                                       float x1,
                                       float y1,
                                       int colorTop,
                                       int colorBottom) {
        if (x0 < x1 && y0 < y1) {
            VertexConsumer buffer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.gui());
            buffer.addVertex(matrix, x0, y0, 0.0F).setColor(colorTop);
            buffer.addVertex(matrix, x0, y1, 0.0F).setColor(colorBottom);
            buffer.addVertex(matrix, x1, y1, 0.0F).setColor(colorBottom);
            buffer.addVertex(matrix, x1, y0, 0.0F).setColor(colorTop);
        }
    }

    /** 自定义文本节点——包装任意 {@link Component}。默认 priority = 0。 */
    public static class Basic extends SubNode {

        private final Component text;

        public Basic(Component text) {
            this(text, 0);
        }

        public Basic(Component text, int priority) {
            super(priority);
            this.text = text;
        }

        public Component getText() {
            return text;
        }

        @Override
        public int getHeight(Font font) {
            return font.lineHeight;
        }

        @Override
        public int getWidth(Font font) {
            return font.width(text);
        }

        @Override
        public void renderText(
                               Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
            font.drawInBatch(
                    text,
                    (float) x,
                    (float) y,
                    -1,
                    true,
                    matrix,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    15728880);
        }
    }
}
