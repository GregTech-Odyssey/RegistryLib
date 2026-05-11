package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibTooltipComponent;
import com.gto.registrylib.tooltip.ResolvedRoot;
import com.gto.registrylib.tooltip.SubNode;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Client-side renderer for {@link RegistryLibTooltipComponent}.
 *
 * <p>
 * Separate boxes are still visually independent, but their dimensions are reported through the
 * vanilla tooltip component contract so vanilla can move the whole tooltip upward when it would
 * overflow.
 */
public class RegistryLibClientTooltip implements ClientTooltipComponent {

    private static final int TOOLTIP_BORDER = 3;
    private static final int BOX_GAP = 2;
    private static final int SEPARATE_BOX_GAP = BOX_GAP + 2;
    private static final int SEPARATE_BOX_X_OFFSET = -3;
    private static final int PAGE_CONTROL_GAP = 3;
    private static final int PAGE_CONTROL_PADDING_X = 6;
    private static final int PAGE_CONTROL_PADDING_Y = 4;
    private static final int MIN_PAGE_CONTROL_WIDTH = 96;
    private static final int MAX_UNPAGED_HEIGHT = 10_000;
    private static final int PANEL_BG_COLOR = 0xF0100010;
    private static final int PANEL_BORDER_TOP = 0x70DAD2FF;
    private static final int PANEL_BORDER_BOTTOM = 0x703A2F68;

    private static int availableHeight = MAX_UNPAGED_HEIGHT;
    private static int pageOffset;
    private static int pageCount = 1;
    private static int layoutRevision;
    private static boolean renderedThisFrame;
    private static boolean activeTooltip;
    private static TooltipTarget activeTarget;

    private final RegistryLibTooltipComponent component;
    private Layout cachedLayout;
    private Font cachedFont;
    private int cachedRevision = -1;

    public RegistryLibClientTooltip(RegistryLibTooltipComponent component) {
        this.component = component;
    }

    public static void prepareLayout(ItemStack stack, int screenHeight, int otherTooltipHeight) {
        TooltipTarget target = TooltipTarget.from(stack);
        if (!Objects.equals(activeTarget, target)) {
            pageOffset = 0;
            pageCount = 1;
            layoutRevision++;
            activeTarget = target;
        }
        int vanillaOverhead = 3;
        int reservedHeight = screenHeight / 3;
        availableHeight = Math.max(1, screenHeight - reservedHeight - otherTooltipHeight - vanillaOverhead);
        renderedThisFrame = true;
        activeTooltip = true;
    }

    public static void resetLayout() {
        availableHeight = MAX_UNPAGED_HEIGHT;
        activeTooltip = false;
    }

    public static void resetIfNoTooltipRendered() {
        if (!renderedThisFrame) {
            pageOffset = 0;
            pageCount = 1;
            layoutRevision++;
            activeTarget = null;
            activeTooltip = false;
        }
        renderedThisFrame = false;
    }

    public static boolean pageUp() {
        if (!activeTooltip) return false;
        if (pageCount <= 1) return false;
        if (pageOffset <= 0) return false;
        pageOffset--;
        layoutRevision++;
        return true;
    }

    public static boolean pageDown() {
        if (!activeTooltip) return false;
        if (pageCount <= 1) return false;
        if (pageOffset >= pageCount - 1) return false;
        pageOffset++;
        layoutRevision++;
        return true;
    }

    @Override
    public int getHeight(Font font) {
        return layout(font).height();
    }

    @Override
    public int getWidth(Font font) {
        return layout(font).width();
    }

    public int getInlineHeight(Font font) {
        return layout(font).inlineHeight();
    }

    public int getInlineWidth(Font font) {
        return layout(font).inlineWidth();
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        Layout layout = layout(font);

        int currentY = y;
        for (SubNode node : component.inlineSubNodes()) {
            node.extractText(graphics, font, x, currentY);
            currentY += node.getHeight(font);
        }

        for (BoxLayout box : layout.visibleBoxes()) {
            int boxX = x + SEPARATE_BOX_X_OFFSET;
            box.root().getBoxRenderer().render(graphics, boxX, y + box.y(), box.width(), box.height());

            int nodeY = y + box.y() + box.padding();
            for (SubNode node : box.nodes()) {
                node.extractText(graphics, font, boxX + box.padding(), nodeY);
                nodeY += node.getHeight(font);
            }
        }

        PageControl control = layout.pageControl();
        if (control != null) {
            int controlX = x + SEPARATE_BOX_X_OFFSET;
            renderPanel(graphics, controlX, y + control.y(), control.width(), control.height());
            graphics.text(
                    font,
                    control.pageText(),
                    controlX + PAGE_CONTROL_PADDING_X,
                    y + control.textY(),
                    0xFFE0E0E0);
            graphics.text(
                    font,
                    control.hintText(),
                    controlX + control.width() - PAGE_CONTROL_PADDING_X - font.width(control.hintText()),
                    y + control.textY(),
                    0xFFB8B1D8);
        }
    }

    private void renderPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BG_COLOR);
        graphics.fillGradient(x, y, x + 1, y + height, PANEL_BORDER_TOP, PANEL_BORDER_BOTTOM);
        graphics.fillGradient(
                x + width - 1, y, x + width, y + height, PANEL_BORDER_TOP, PANEL_BORDER_BOTTOM);
        graphics.fill(x, y, x + width, y + 1, PANEL_BORDER_TOP);
        graphics.fill(x, y + height - 1, x + width, y + height, PANEL_BORDER_BOTTOM);
    }

    @Override
    public void extractImage(
                             Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        Layout layout = layout(font);

        int currentY = y;
        for (SubNode node : component.inlineSubNodes()) {
            node.extractImage(font, x, currentY, layout.inlineWidth(), layout.inlineHeight(), graphics);
            currentY += node.getHeight(font);
        }

        for (BoxLayout box : layout.visibleBoxes()) {
            int boxX = x + SEPARATE_BOX_X_OFFSET;
            int nodeY = y + box.y() + box.padding();
            for (SubNode node : box.nodes()) {
                node.extractImage(
                        font, boxX + box.padding(), nodeY, box.contentWidth(), box.contentHeight(), graphics);
                nodeY += node.getHeight(font);
            }
        }
    }

    private Layout layout(Font font) {
        if (cachedLayout == null || cachedFont != font || cachedRevision != layoutRevision) {
            cachedFont = font;
            cachedRevision = layoutRevision;
            cachedLayout = createLayout(font);
        }
        return cachedLayout;
    }

    private Layout createLayout(Font font) {
        int inlineHeight = 0;
        int inlineWidth = 0;
        for (SubNode node : component.inlineSubNodes()) {
            inlineHeight += node.getHeight(font);
            inlineWidth = Math.max(inlineWidth, node.getWidth(font));
        }

        List<BoxLayout> allBoxes = measureBoxes(font, inlineHeight);
        int controlHeight = pageControlHeight(font);
        boolean needsPaging = totalHeight(inlineHeight, allBoxes, false, controlHeight) > availableHeight;

        if (!needsPaging || allBoxes.isEmpty()) {
            int height = totalHeight(inlineHeight, allBoxes, false, controlHeight);
            int width = Math.max(inlineWidth, maxBoxWidth(allBoxes));
            clampPageOffset(1);
            pageCount = 1;
            return new Layout(width, height, inlineWidth, inlineHeight, allBoxes, null);
        }

        List<List<BoxLayout>> pages = paginate(inlineHeight, allBoxes, controlHeight);
        clampPageOffset(pages.size());
        pageCount = pages.size();
        List<BoxLayout> visibleBoxes = pages.get(pageOffset);
        PageControl pageControl = pageControl(font, inlineHeight, visibleBoxes, pages.size());
        int height = totalHeight(inlineHeight, visibleBoxes, true, controlHeight);
        int width = Math.max(Math.max(inlineWidth, maxBoxWidth(visibleBoxes)), pageControl.width());
        return new Layout(width, height, inlineWidth, inlineHeight, visibleBoxes, pageControl);
    }

    private List<BoxLayout> measureBoxes(Font font, int inlineHeight) {
        if (component.separateRoots().isEmpty()) return Collections.emptyList();

        List<BoxLayout> boxes = new ArrayList<>();
        int y = inlineHeight + TOOLTIP_BORDER + BOX_GAP;
        for (ResolvedRoot resolved : component.separateRoots()) {
            if (resolved.subNodes().isEmpty()) continue;

            int contentWidth = 0;
            int contentHeight = 0;
            for (SubNode node : resolved.subNodes()) {
                contentWidth = Math.max(contentWidth, node.getWidth(font));
                contentHeight += node.getHeight(font);
            }

            int padding = resolved.rootNode().getPadding();
            int height = contentHeight + padding * 2;
            int width = contentWidth + padding * 2;
            boxes.add(
                    new BoxLayout(
                            resolved.rootNode(),
                            resolved.subNodes(),
                            y,
                            width,
                            height,
                            contentWidth,
                            contentHeight,
                            padding));
            y += height + SEPARATE_BOX_GAP;
        }
        return boxes;
    }

    private List<List<BoxLayout>> paginate(
                                           int inlineHeight, List<BoxLayout> boxes, int controlHeight) {
        int firstBoxY = inlineHeight + TOOLTIP_BORDER + BOX_GAP;
        int maxPageHeight = Math.max(1, availableHeight - inlineHeight - PAGE_CONTROL_GAP - controlHeight);
        List<List<BoxLayout>> pages = new ArrayList<>();
        List<BoxLayout> page = new ArrayList<>();
        int pageContentHeight = 0;

        for (BoxLayout box : boxes) {
            int boxContribution = box.height() + (page.isEmpty() ? 0 : SEPARATE_BOX_GAP);
            if (!page.isEmpty() && pageContentHeight + boxContribution > maxPageHeight) {
                pages.add(reposition(page, firstBoxY));
                page = new ArrayList<>();
                pageContentHeight = 0;
                boxContribution = box.height();
            }
            page.add(box);
            pageContentHeight += boxContribution;
        }

        if (!page.isEmpty()) {
            pages.add(reposition(page, firstBoxY));
        }
        if (pages.isEmpty()) {
            pages.add(Collections.emptyList());
        }
        return pages;
    }

    private List<BoxLayout> reposition(List<BoxLayout> boxes, int firstY) {
        List<BoxLayout> result = new ArrayList<>(boxes.size());
        int y = firstY;
        for (BoxLayout box : boxes) {
            result.add(box.withY(y));
            y += box.height() + SEPARATE_BOX_GAP;
        }
        return result;
    }

    private PageControl pageControl(
                                    Font font, int inlineHeight, List<BoxLayout> boxes, int pageCount) {
        int y;
        if (boxes.isEmpty()) {
            y = inlineHeight + TOOLTIP_BORDER + BOX_GAP;
        } else {
            BoxLayout last = boxes.get(boxes.size() - 1);
            y = last.y() + last.height() + PAGE_CONTROL_GAP;
        }
        Component pageText = Component.literal("<  " + (pageOffset + 1) + " / " + pageCount + "  >");
        Component hintText = Client.tooltipPageKeyHint();
        int width = Math.max(
                MIN_PAGE_CONTROL_WIDTH,
                font.width(pageText) + font.width(hintText) + PAGE_CONTROL_PADDING_X * 3);
        int height = pageControlHeight(font);
        int textY = y + PAGE_CONTROL_PADDING_Y;
        return new PageControl(y, width, height, textY, pageText, hintText);
    }

    private int pageControlHeight(Font font) {
        return PAGE_CONTROL_PADDING_Y * 2 + font.lineHeight;
    }

    private int totalHeight(
                            int inlineHeight, List<BoxLayout> boxes, boolean includePageControl, int controlHeight) {
        int height = inlineHeight;
        if (!boxes.isEmpty()) {
            BoxLayout last = boxes.get(boxes.size() - 1);
            height = Math.max(height, last.y() + last.height());
        }
        if (includePageControl) {
            if (boxes.isEmpty()) {
                height += TOOLTIP_BORDER + BOX_GAP + controlHeight;
            } else {
                height += PAGE_CONTROL_GAP + controlHeight;
            }
        }
        return height;
    }

    private int maxBoxWidth(List<BoxLayout> boxes) {
        int width = 0;
        for (BoxLayout box : boxes) {
            width = Math.max(width, box.width());
        }
        return width;
    }

    private void clampPageOffset(int pageCount) {
        if (pageCount <= 0) {
            pageOffset = 0;
        } else if (pageOffset < 0) {
            pageOffset = 0;
        } else if (pageOffset >= pageCount) {
            pageOffset = pageCount - 1;
        }
    }

    private record Layout(
                          int width,
                          int height,
                          int inlineWidth,
                          int inlineHeight,
                          List<BoxLayout> visibleBoxes,
                          PageControl pageControl) {}

    private record PageControl(
                               int y, int width, int height, int textY, Component pageText, Component hintText) {}

    private record TooltipTarget(String itemId, List<ComponentValue> components) {

        private static TooltipTarget from(ItemStack stack) {
            DataComponentMap components = stack.getComponents();
            List<ComponentValue> values = new ArrayList<>(components.size());
            for (TypedDataComponent<?> component : components) {
                values.add(
                        new ComponentValue(
                                Objects.toString(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type())),
                                Objects.toString(component.value())));
            }
            values.sort(Comparator.comparing(ComponentValue::typeId));
            return new TooltipTarget(
                    Objects.toString(BuiltInRegistries.ITEM.getKey(stack.getItem())), List.copyOf(values));
        }
    }

    private record ComponentValue(String typeId, String value) {}

    private record BoxLayout(
                             com.gto.registrylib.tooltip.RootNode root,
                             List<SubNode> nodes,
                             int y,
                             int width,
                             int height,
                             int contentWidth,
                             int contentHeight,
                             int padding) {

        private BoxLayout withY(int y) {
            return new BoxLayout(root, nodes, y, width, height, contentWidth, contentHeight, padding);
        }
    }
}
