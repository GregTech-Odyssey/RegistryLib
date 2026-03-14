package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibTooltipComponent;
import com.gto.registrylib.tooltip.ResolvedRoot;
import com.gto.registrylib.tooltip.SubNode;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/**
 * {@link RegistryLibTooltipComponent} 的客户端渲染实现。
 *
 * <ul>
 *   <li>内联 SubNode（来自 {@code separateBox=false} 的 RootNode）直接嵌入原版提示框
 *   <li>独立框 SubNode（来自 {@code separateBox=true} 的 RootNode）
 *       在原版提示框下方绘制独立背景框并渲染
 * </ul>
 */
public class RegistryLibClientTooltip implements ClientTooltipComponent {

    /** 原版提示框底部边框 padding。 */
    private static final int TOOLTIP_BORDER = 3;

    /** 独立框之间 / 提示框与独立框之间的间距。 */
    private static final int BOX_GAP = 2;

    private final RegistryLibTooltipComponent component;

    public RegistryLibClientTooltip(RegistryLibTooltipComponent component) {
        this.component = component;
    }

    @Override
    public int getHeight(Font font) {
        int h = 0;
        for (SubNode node : component.inlineSubNodes()) {
            h += node.getHeight(font);
        }
        return h;
    }

    @Override
    public int getWidth(Font font) {
        int w = 0;
        for (SubNode node : component.inlineSubNodes()) {
            w = Math.max(w, node.getWidth(font));
        }
        return w;
    }

    /**
     * 渲染文字层——原版先调用此方法。
     * 独立框的背景也在此处绘制，保证背景在文字之下。
     */
    @Override
    public void renderText(GuiGraphics graphics, Font font, int x, int y) {
        // 1) 内联 SubNodes
        int currentY = y;
        for (SubNode node : component.inlineSubNodes()) {
            node.renderText(graphics, font, x, currentY);
            currentY += node.getHeight(font);
        }

        // 2) 独立框——先提交背景，再提交文字
        if (component.separateRoots().isEmpty()) return;

        int boxY = currentY + TOOLTIP_BORDER + BOX_GAP;

        for (ResolvedRoot resolved : component.separateRoots()) {
            var rootNode = resolved.rootNode();
            var subNodes = resolved.subNodes();
            if (subNodes.isEmpty()) continue;

            int contentWidth = 0;
            int contentHeight = 0;
            for (SubNode node : subNodes) {
                contentWidth = Math.max(contentWidth, node.getWidth(font));
                contentHeight += node.getHeight(font);
            }
            int padding = rootNode.getPadding();
            int boxX = x - padding;
            int boxW = contentWidth + padding * 2;
            int boxH = contentHeight + padding * 2;

            // 先提交框背景
            rootNode.getBoxRenderer().render(graphics, boxX, boxY, boxW, boxH);

            // 再提交文字
            int nodeY = boxY + padding;
            for (SubNode node : subNodes) {
                node.renderText(graphics, font, x, nodeY);
                nodeY += node.getHeight(font);
            }

            boxY += boxH + BOX_GAP;
        }
    }

    /**
     * 渲染图像层——原版在 {@link #renderText} 之后调用此方法。
     */
    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        // 1) 内联 SubNodes
        int currentY = y;
        for (SubNode node : component.inlineSubNodes()) {
            node.renderImage(font, x, currentY, width, height, graphics);
            currentY += node.getHeight(font);
        }

        // 2) 独立框——仅图像
        if (component.separateRoots().isEmpty()) return;

        int boxY = currentY + TOOLTIP_BORDER + BOX_GAP;

        for (ResolvedRoot resolved : component.separateRoots()) {
            var subNodes = resolved.subNodes();
            if (subNodes.isEmpty()) continue;

            int contentWidth = 0;
            int contentHeight = 0;
            for (SubNode node : subNodes) {
                contentWidth = Math.max(contentWidth, node.getWidth(font));
                contentHeight += node.getHeight(font);
            }
            int padding = resolved.rootNode().getPadding();
            int boxH = contentHeight + padding * 2;

            int nodeY = boxY + padding;
            for (SubNode node : subNodes) {
                node.renderImage(font, x, nodeY, contentWidth, contentHeight, graphics);
                nodeY += node.getHeight(font);
            }

            boxY += boxH + BOX_GAP;
        }
    }
}
