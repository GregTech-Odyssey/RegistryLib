package com.gto.registrylib.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Tooltip 叶子节点——{@link RootNode} 内的渲染单元。
 *
 * <p>
 * 每个节点拥有 {@link #priority}（升序排列，值越小在所属 RootNode 中位置越靠上）。 没有子节点列表——所有 SubNode 都是叶子，层级结构通过 {@link
 * RootNode} 管理。
 *
 * <p>
 * 节点通过 {@link #getHeight} / {@link #getWidth} 提供自身尺寸， 通过 {@link #extractImage} / {@link
 * #extractText} 渲染自身内容。
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

    /** 渲染此节点的图形元素（如分隔线、色块）。默认无操作。 */
    public void extractImage(
                             Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {}

    /** 渲染此节点的文本内容。默认无操作。 */
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {}

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
        public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
            graphics.text(font, text, x, y, -1);
        }
    }
}
