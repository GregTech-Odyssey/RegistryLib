package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibPageControlComponent;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

/**
 * 客户端渲染器：分页控件 "< 1 / 3 > [↑↓]"。
 *
 * <p>
 * 仅在独立框需要分页（页数 ≥ 2）时由 {@link Client#onGatherTooltipComponents} 追加到组件列表末尾，作为最后一个独立组件参与原版 tooltip
 * 计算与渲染。
 */
public class RegistryLibClientPageControl implements ClientTooltipComponent {

    private static final int TOP_MARGIN = RegistryLibClientPanelComponent.TOP_MARGIN;
    private static final int INSET = RegistryLibClientPanelComponent.INSET;
    private static final int PADDING_X = 6;
    private static final int PADDING_Y = 4;
    private static final int MIN_WIDTH = 96;
    private static final int BG_COLOR = 0xF0100010;
    private static final int BORDER_TOP = 0x70DAD2FF;
    private static final int BORDER_BOTTOM = 0x703A2F68;
    private static final int TEXT_COLOR_PAGE = 0xFFE0E0E0;
    private static final int TEXT_COLOR_HINT = 0xFFB8B1D8;

    private final int pageOffset;
    private final int pageCount;

    public RegistryLibClientPageControl(RegistryLibPageControlComponent component) {
        this.pageOffset = component.pageOffset();
        this.pageCount = component.pageCount();
    }

    private Component pageText() {
        return Component.literal("<  " + (pageOffset + 1) + " / " + pageCount + "  >");
    }

    private Component hintText() {
        return Client.tooltipPageKeyHintStatic();
    }

    @Override
    public int getHeight(Font font) {
        return TOP_MARGIN + PADDING_Y * 2 + font.lineHeight;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(MIN_WIDTH, font.width(pageText()) + font.width(hintText()) + PADDING_X * 3);
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        int contentWidth = getWidth(font);
        int boxHeight = PADDING_Y * 2 + font.lineHeight;

        int bx = x - INSET;
        int by = y + TOP_MARGIN;
        int bw = contentWidth + INSET * 2;

        graphics.fill(bx + 1, by + 1, bx + bw - 1, by + boxHeight - 1, BG_COLOR);
        graphics.fillGradient(bx, by, bx + 1, by + boxHeight, BORDER_TOP, BORDER_BOTTOM);
        graphics.fillGradient(bx + bw - 1, by, bx + bw, by + boxHeight, BORDER_TOP, BORDER_BOTTOM);
        graphics.fill(bx, by, bx + bw, by + 1, BORDER_TOP);
        graphics.fill(bx, by + boxHeight - 1, bx + bw, by + boxHeight, BORDER_BOTTOM);

        Component page = pageText();
        graphics.text(font, page, bx + PADDING_X, by + PADDING_Y, TEXT_COLOR_PAGE);

        Component hint = hintText();
        graphics.text(
                font, hint, bx + bw - PADDING_X - font.width(hint), by + PADDING_Y, TEXT_COLOR_HINT);
    }
}
