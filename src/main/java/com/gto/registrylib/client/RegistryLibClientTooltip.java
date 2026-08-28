package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibTooltipComponent;
import com.gto.registrylib.tooltip.SubNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;

import org.joml.Matrix4f;

import java.util.List;

/**
 * 客户端渲染器：内联 SubNode 列表。
 *
 * <p>
 * 只负责绘制原版 tooltip 背景内、紧跟标题之后的若干行内联文字（包括 {@link com.gto.registrylib.tooltip.SeparatorNode}
 * 这种全宽分隔线）。独立框由 {@link RegistryLibClientPanelComponent} 处理；分页控件由 {@link
 * RegistryLibClientPageControl} 处理；都是同一份原版组件列表里的独立条目。
 *
 * <p>
 * 注意 {@link #renderImage} 用本组件自己的 {@link #getWidth(Font)} 而不是原版传入的全局 max width 来绘制——
 * 因为分隔线应该跟内联背景一样宽，而不是延伸到独立框那么宽。
 */
public class RegistryLibClientTooltip implements ClientTooltipComponent {

    private final List<SubNode> inlineSubNodes;

    public RegistryLibClientTooltip(RegistryLibTooltipComponent component) {
        this.inlineSubNodes = component.inlineSubNodes();
    }

    @Override
    public int getHeight() {
        Font font = Minecraft.getInstance().font;
        int h = 0;
        for (SubNode node : inlineSubNodes) {
            h += node.getHeight(font);
        }
        return h;
    }

    @Override
    public int getWidth(Font font) {
        int w = 0;
        for (SubNode node : inlineSubNodes) {
            w = Math.max(w, node.getWidth(font));
        }
        return w;
    }

    @Override
    public void renderText(
                           Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        int currentY = y;
        for (SubNode node : inlineSubNodes) {
            node.renderText(font, x, currentY, matrix, bufferSource);
            currentY += node.getHeight(font);
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int inlineWidth = getWidth(font);
        int currentY = y;
        for (SubNode node : inlineSubNodes) {
            int nodeHeight = node.getHeight(font);
            node.renderImage(font, x, currentY, inlineWidth, nodeHeight, graphics);
            currentY += nodeHeight;
        }
    }
}
