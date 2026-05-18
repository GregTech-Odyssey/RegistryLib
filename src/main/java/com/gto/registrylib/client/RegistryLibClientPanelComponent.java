package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibPanelComponent;
import com.gto.registrylib.tooltip.ResolvedRoot;
import com.gto.registrylib.tooltip.SubNode;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/**
 * 客户端渲染器：一个独立框面板。
 *
 * <p>
 * 与 {@link RegistryLibClientTooltip}（内联节点渲染器）独立。原版
 * {@code GuiGraphicsExtractor.tooltip()} 会用 {@link #getWidth} / {@link #getHeight} 把它当作一个普通组件参与计算
 * tooltip 总尺寸与定位，再依次调用 {@link #extractText}/{@link #extractImage}——这样所有面板的 X 自然落到同一个
 * 已定位的 {@code l}，左对齐由原版保证，无需手算偏移。
 *
 * <p>
 * 面板背景在 {@code extractText} 内、子节点文字之前绘制；这是因为原版 tooltip 管线里 text pass 排在 image pass
 * 之前，背景写在 extractText 才能落到所有子节点文字之下而不是覆盖它们。
 */
public class RegistryLibClientPanelComponent implements ClientTooltipComponent {

    /** 面板上方留白——在面板之间以及面板与内联区域之间产生间距。 */
    static final int TOP_MARGIN = 5;

    /** 背景向内容区左右各延伸的像素数——比原版 PADDING (3) 少 1，视觉上与原版边框对齐。 */
    static final int INSET = 4;

    private final ResolvedRoot resolved;

    public RegistryLibClientPanelComponent(RegistryLibPanelComponent component) {
        this.resolved = component.resolved();
    }

    private int contentWidth(Font font) {
        int w = 0;
        for (SubNode node : resolved.subNodes()) {
            w = Math.max(w, node.getWidth(font));
        }
        return w;
    }

    private int contentHeight(Font font) {
        int h = 0;
        for (SubNode node : resolved.subNodes()) {
            h += node.getHeight(font);
        }
        return h;
    }

    @Override
    public int getWidth(Font font) {
        return contentWidth(font);
    }

    @Override
    public int getHeight(Font font) {
        return TOP_MARGIN + INSET * 2 + contentHeight(font);
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        int cw = contentWidth(font);
        int ch = contentHeight(font);
        resolved.rootNode().getBoxRenderer().render(
                graphics, x - INSET, y + TOP_MARGIN, cw + INSET * 2, ch + INSET * 2);

        int contentY = y + TOP_MARGIN + INSET;
        for (SubNode node : resolved.subNodes()) {
            node.extractText(graphics, font, x, contentY);
            contentY += node.getHeight(font);
        }
    }

    @Override
    public void extractImage(
                             Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        int cw = contentWidth(font);
        int contentY = y + TOP_MARGIN + INSET;
        for (SubNode node : resolved.subNodes()) {
            int nodeHeight = node.getHeight(font);
            node.extractImage(font, x, contentY, cw, nodeHeight, graphics);
            contentY += nodeHeight;
        }
    }
}
