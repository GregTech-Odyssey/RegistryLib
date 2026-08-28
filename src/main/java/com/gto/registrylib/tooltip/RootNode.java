package com.gto.registrylib.tooltip;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import org.joml.Matrix4f;

import java.util.function.Supplier;

/**
 * Tooltip 根节点——框容器。
 *
 * <p>
 * 每个 RootNode 代表一个独立的 tooltip 框区域。 {@link #separateBox} 为 {@code true} 时在原版 tooltip 下方渲染独立框， 为
 * {@code false} 时紧跟在原版 tooltip 内容之后（无独立框）。
 *
 * <p>
 * RootNode 不包含渲染内容——内容由注入的 {@link SubNode} 列表提供。 框的绘制逻辑通过构造参数 {@link #boxRenderer} 自定义。
 */
public class RootNode {

    /** 默认背景色 (ARGB)。 */
    private static final int BG_COLOR = 0xF0100010;

    /** 边框亮边——顶部高光 (ARGB)。 */
    private static final int BORDER_HIGHLIGHT = 0x90E8E8E8;

    /** 边框暗边——底部阴影 (ARGB)。 */
    private static final int BORDER_SHADOW = 0x90686870;

    /**
     * 默认框渲染器——深色背景 + 白色亮度渐变边框。
     *
     * <p>
     * 用 {@link Supplier} 包一层：引用了客户端类（{@link MultiBufferSource} 等）的内层 lambda 只有在 supplier 被调用时
     * （即客户端渲染 tooltip 时）才会被链接。如果直接把它写成 {@code BoxRenderer} 静态字段，那么在 {@code RootNode.<clinit>}
     * 初始化该字段时就会解析客户端类 —— 而 {@code TooltipRegistry.<clinit>} 会在普通（双端）物品注册过程中构造一个默认 {@code RootNode}，
     * 这会让专用服务器直接 {@code NoClassDefFoundError} 崩溃。
     */
    public static final Supplier<BoxRenderer> DEFAULT_BOX_RENDERER = () -> (matrix, bufferSource, x, y, width, height) -> {
        int r = x + width;
        int b = y + height;
        // 背景（十字形，留出 4 角像素形成圆角效果）
        fill(matrix, bufferSource, x + 1, y + 1, r - 1, b - 1, BG_COLOR);
        // 1px 亮度渐变边框
        fillGradient(matrix, bufferSource, x, y, x + 1, b, BORDER_HIGHLIGHT, BORDER_SHADOW);
        fillGradient(matrix, bufferSource, r - 1, y, r, b, BORDER_HIGHLIGHT, BORDER_SHADOW);
        fill(matrix, bufferSource, x, y, r, y + 1, BORDER_HIGHLIGHT);
        fill(matrix, bufferSource, x, b - 1, r, b, BORDER_SHADOW);
    };

    private final String id;
    private final int priority;
    private final boolean separateBox;
    private final int padding;
    private final Supplier<BoxRenderer> boxRenderer;

    public RootNode(
                    String id,
                    int priority,
                    boolean separateBox,
                    int padding,
                    Supplier<BoxRenderer> boxRenderer) {
        this.id = id;
        this.priority = priority;
        this.separateBox = separateBox;
        this.padding = padding;
        this.boxRenderer = boxRenderer;
    }

    public RootNode(String id, int priority, boolean separateBox) {
        this(id, priority, separateBox, 4, DEFAULT_BOX_RENDERER);
    }

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSeparateBox() {
        return separateBox;
    }

    public int getPadding() {
        return padding;
    }

    /** 解析框渲染器。仅可在客户端调用（如 tooltip 渲染时）。 */
    public BoxRenderer getBoxRenderer() {
        return boxRenderer.get();
    }

    /** 在 text pass 中绘制一个实心矩形（用于压在文本下方的背景）。 */
    private static void fill(
                             Matrix4f matrix,
                             MultiBufferSource.BufferSource bufferSource,
                             float x0,
                             float y0,
                             float x1,
                             float y1,
                             int color) {
        if (x0 < x1 && y0 < y1) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
            buffer.addVertex(matrix, x0, y0, 0.0F).setColor(color);
            buffer.addVertex(matrix, x0, y1, 0.0F).setColor(color);
            buffer.addVertex(matrix, x1, y1, 0.0F).setColor(color);
            buffer.addVertex(matrix, x1, y0, 0.0F).setColor(color);
        }
    }

    /** 在 text pass 中绘制一个垂直渐变矩形（用于压在文本下方的边框/背景）。 */
    private static void fillGradient(
                                     Matrix4f matrix,
                                     MultiBufferSource.BufferSource bufferSource,
                                     float x0,
                                     float y0,
                                     float x1,
                                     float y1,
                                     int colorTop,
                                     int colorBottom) {
        if (x0 < x1 && y0 < y1) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
            buffer.addVertex(matrix, x0, y0, 0.0F).setColor(colorTop);
            buffer.addVertex(matrix, x0, y1, 0.0F).setColor(colorBottom);
            buffer.addVertex(matrix, x1, y1, 0.0F).setColor(colorBottom);
            buffer.addVertex(matrix, x1, y0, 0.0F).setColor(colorTop);
        }
    }

    @FunctionalInterface
    public interface BoxRenderer {

        void render(
                    Matrix4f matrix,
                    MultiBufferSource.BufferSource bufferSource,
                    int x,
                    int y,
                    int width,
                    int height);
    }
}
