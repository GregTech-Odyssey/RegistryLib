package com.gto.registrylib.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 分隔线节点——在同一 {@link RootNode} 内的 {@link SubNode} 之间绘制全宽高光细线。
 *
 * <p>由 {@link TooltipRegistry} 在组装 {@link RegistryLibTooltipComponent} 时自动插入，无需手动创建。
 *
 * <p>全宽 1px 半透明白色高光线，在深色 tooltip 背景上呈现柔和分隔。
 * 总高度 7px（3px 上间距 + 1px 线条 + 3px 下间距）。
 */
public class SeparatorNode extends SubNode {

    private static final int TOTAL_HEIGHT = 7;
    private static final int TOP_PADDING = 3;
    private static final int LINE_COLOR = 0x40FFFFFF;

    public SeparatorNode() {
        super(Integer.MIN_VALUE);
    }

    @Override
    public int getHeight(Font font) {
        return TOTAL_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        if (width <= 0) return;
        graphics.fill(x, y + TOP_PADDING, x + width, y + TOP_PADDING + 1, LINE_COLOR);
    }
}
