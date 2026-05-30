package com.gto.registrylib.tooltip;

import net.minecraft.client.gui.GuiGraphicsExtractor;

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
     * 用 {@link Supplier} 包一层：引用了客户端类 {@link GuiGraphicsExtractor} 的内层 lambda 只有在 supplier 被调用时
     * （即客户端渲染 tooltip 时）才会被链接。如果直接把它写成 {@code BoxRenderer} 静态字段，那么在
     * {@code RootNode.<clinit>} 初始化该字段时就会解析 {@code GuiGraphicsExtractor} —— 而
     * {@code TooltipRegistry.<clinit>} 会在普通（双端）物品注册过程中构造一个默认 {@code RootNode}，
     * 这会让专用服务器直接 {@code NoClassDefFoundError} 崩溃。
     */
    public static final Supplier<BoxRenderer> DEFAULT_BOX_RENDERER = () -> (graphics, x, y, width, height) -> {
        int r = x + width;
        int b = y + height;
        // 背景（十字形，留出 4 角像素形成圆角效果）
        graphics.fill(x + 1, y + 1, r - 1, b - 1, BG_COLOR);
        // 1px 亮度渐变边框
        graphics.fillGradient(x, y, x + 1, b, BORDER_HIGHLIGHT, BORDER_SHADOW);
        graphics.fillGradient(r - 1, y, r, b, BORDER_HIGHLIGHT, BORDER_SHADOW);
        graphics.fill(x, y, r, y + 1, BORDER_HIGHLIGHT);
        graphics.fill(x, b - 1, r, b, BORDER_SHADOW);
    };

    private final String id;
    private final int priority;
    private final boolean separateBox;
    private final int padding;
    private final Supplier<BoxRenderer> boxRenderer;

    public RootNode(
                    String id, int priority, boolean separateBox, int padding, Supplier<BoxRenderer> boxRenderer) {
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

    @FunctionalInterface
    public interface BoxRenderer {

        void render(GuiGraphicsExtractor graphics, int x, int y, int width, int height);
    }
}
