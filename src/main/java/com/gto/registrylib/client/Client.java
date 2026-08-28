package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibPageControlComponent;
import com.gto.registrylib.tooltip.RegistryLibPanelComponent;
import com.gto.registrylib.tooltip.RegistryLibTooltipComponent;
import com.gto.registrylib.tooltip.ResolvedRoot;
import com.gto.registrylib.tooltip.SubNode;
import com.gto.registrylib.tooltip.TooltipPagination;
import com.gto.registrylib.tooltip.TooltipRegistry;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidType;

import lombok.experimental.UtilityClass;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@UtilityClass
public class Client {

    private final AtomicReference<ConcurrentHashMap<Supplier<BlockEntityType<?>>, BlockEntityRendererProvider>> BER = new AtomicReference<>(new ConcurrentHashMap<>());

    private final AtomicReference<ConcurrentHashMap<Supplier<FluidType>, IClientFluidTypeExtensions>> FLUID_TYPE_EXTENSIONS = new AtomicReference<>(new ConcurrentHashMap<>());

    /** 待注册的方块着色器 (1.21.1 使用 {@link BlockColor} + {@link RegisterColorHandlersEvent.Block})。 */
    private final ConcurrentHashMap<Supplier<? extends Block>, List<BlockColor>> BLOCK_TINT_SOURCES = new ConcurrentHashMap<>();

    /** 待注册的物品着色器 (1.21.1 使用 {@link ItemColor} + {@link RegisterColorHandlersEvent.Item})。 */
    private final ConcurrentHashMap<Supplier<? extends Item>, List<ItemColor>> ITEM_TINT_SOURCES = new ConcurrentHashMap<>();

    private final AtomicReference<ConcurrentHashMap<Supplier<EntityType<?>>, EntityRendererProvider>> ENTITY_RENDERERS = new AtomicReference<>(new ConcurrentHashMap<>());

    /** 1.21.1 中 KeyMapping 的类别是纯字符串（翻译键），没有 {@code KeyMapping.Category} 类。 */
    private static final String TOOLTIP_KEY_CATEGORY = "key.categories.registrylib";

    private final KeyMapping TOOLTIP_PAGE_UP = new KeyMapping(
            "key.registrylib.tooltip_page_up",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            TOOLTIP_KEY_CATEGORY);

    private final KeyMapping TOOLTIP_PAGE_DOWN = new KeyMapping(
            "key.registrylib.tooltip_page_down",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            TOOLTIP_KEY_CATEGORY);

    /** 默认 lineHeight 估算（gather 阶段没有 event font，用它作为分页高度估算的兜底）。 */
    private static final int DEFAULT_LINE_HEIGHT = 9;

    /** 屏幕底部预留的空白比例（与原版 prepareLayout 旧值保持一致），用于估算独立框可用高度。 */
    private static final int RESERVED_HEIGHT_DIVISOR = 3;

    /** 估算可用高度时扣除的安全余量（包含标题与原版 padding 的粗略折算）。 */
    private static final int LAYOUT_OVERHEAD = 24;

    /** 分页时面板之间预留的间距——面板 getHeight 已包含 TOP_MARGIN，无需额外加值。 */
    private static final int PANEL_GAP_ESTIMATE = 0;

    /** 原版 tooltip 背景颜色（TooltipRenderUtil 的私有常量，用于在 Color 事件中重画内联区域）。 */
    private static final int TOOLTIP_BACKGROUND_COLOR = 0xF0100010;

    private static final int TOOLTIP_BORDER_COLOR_TOP = 0x5055FF55;
    private static final int TOOLTIP_BORDER_COLOR_BOTTOM = 0x5028007F;

    public void init(IEventBus modEventBus) {
        modEventBus.addListener(Client::onClientSetup);
        modEventBus.addListener(Client::onRegisterClientExtensions);
        modEventBus.addListener(Client::onRegisterBlockTintSources);
        modEventBus.addListener(Client::onRegisterItemTintSources);
        modEventBus.addListener(Client::onRegisterTooltipFactories);
        modEventBus.addListener(Client::onRegisterKeyMappings);
        modEventBus.addListener(Client::onRegisterEntityRenderers);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, Client::onGatherTooltipComponents);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, Client::onRenderTooltipColor);
        NeoForge.EVENT_BUS.addListener(Client::onTooltipKeyPressed);
        NeoForge.EVENT_BUS.addListener(Client::onClientTickPost);
    }

    public static void initStatic(IEventBus modEventBus) {
        init(modEventBus);
    }

    public void registerBER(Supplier<BlockEntityType<?>> type, BlockEntityRendererProvider provider) {
        var map = BER.get();
        if (map != null) map.put(type, provider);
    }

    public static void registerBERStatic(
                                         Supplier<BlockEntityType<?>> type, BlockEntityRendererProvider provider) {
        registerBER(type, provider);
    }

    public void registerFluidTypeExtensions(
                                            Supplier<FluidType> type, IClientFluidTypeExtensions extensions) {
        var map = FLUID_TYPE_EXTENSIONS.get();
        if (map != null) map.put(type, extensions);
    }

    public static void registerFluidTypeExtensionsStatic(
                                                         Supplier<FluidType> type, IClientFluidTypeExtensions extensions) {
        registerFluidTypeExtensions(type, extensions);
    }

    public void registerBlockTintSources(Supplier<? extends Block> block, BlockColor... tintSources) {
        BLOCK_TINT_SOURCES.put(block, List.of(tintSources.clone()));
    }

    public static void registerBlockTintSourcesStatic(
                                                      Supplier<? extends Block> block, BlockColor... tintSources) {
        registerBlockTintSources(block, tintSources);
    }

    public void registerItemTintSources(Supplier<? extends Item> item, ItemColor... tintSources) {
        ITEM_TINT_SOURCES.put(item, List.of(tintSources.clone()));
    }

    public static void registerItemTintSourcesStatic(
                                                     Supplier<? extends Item> item, ItemColor... tintSources) {
        registerItemTintSources(item, tintSources);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerEntityRenderer(
                                       Supplier<EntityType<?>> type, EntityRendererProvider renderer) {
        var map = ENTITY_RENDERERS.get();
        if (map != null) map.put(type, renderer);
    }

    public static void registerEntityRendererStatic(
                                                    Supplier<EntityType<?>> type, EntityRendererProvider renderer) {
        registerEntityRenderer(type, renderer);
    }

    @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
    private void onClientSetup(FMLClientSetupEvent ignoredEvent) {
        var map = BER.getAndSet(null);
        if (map != null)
            map.forEach((type, provider) -> BlockEntityRenderers.register(type.get(), provider));
    }

    private void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        var map = FLUID_TYPE_EXTENSIONS.getAndSet(null);
        if (map != null)
            map.forEach((type, extensions) -> event.registerFluidType(extensions, type.get()));
    }

    private void onRegisterBlockTintSources(RegisterColorHandlersEvent.Block event) {
        BLOCK_TINT_SOURCES.forEach(
                (block, tintSources) -> {
                    for (BlockColor color : tintSources) {
                        event.register(color, block.get());
                    }
                });
        BLOCK_TINT_SOURCES.clear();
    }

    private void onRegisterItemTintSources(RegisterColorHandlersEvent.Item event) {
        ITEM_TINT_SOURCES.forEach(
                (item, tintSources) -> {
                    for (ItemColor color : tintSources) {
                        event.register(color, item.get());
                    }
                });
        ITEM_TINT_SOURCES.clear();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        var map = ENTITY_RENDERERS.getAndSet(null);
        if (map != null)
            map.forEach(
                    (type, renderer) -> event.registerEntityRenderer((EntityType) type.get(), renderer));
    }

    private void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(RegistryLibTooltipComponent.class, RegistryLibClientTooltip::new);
        event.register(RegistryLibPanelComponent.class, RegistryLibClientPanelComponent::new);
        event.register(RegistryLibPageControlComponent.class, RegistryLibClientPageControl::new);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOOLTIP_PAGE_UP);
        event.register(TOOLTIP_PAGE_DOWN);
    }

    /**
     * 把 {@link TooltipRegistry#resolve} 的结果拆分成多个独立组件追加到原版组件列表里：
     *
     * <ul>
     * <li>非空内联节点 → 一个 {@link RegistryLibTooltipComponent}
     * <li>每一个独立框 → 一个 {@link RegistryLibPanelComponent}
     * <li>分页时还会追加一个 {@link RegistryLibPageControlComponent}
     * </ul>
     *
     * <p>
     * 这样原版的 tooltip 管线在计算 tooltip 总宽高、定位与渲染时就能像处理任何普通组件一样 把它们左对齐到同一个已定位的 x，避免「整块塞进一个
     * ClientTooltipComponent 然后内部手算偏移」导致的对齐问题。
     */
    private void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().isEmpty()) return;
        var resolved = TooltipRegistry.resolve(event.getItemStack());
        if (resolved == null || resolved.isEmpty()) return;

        TooltipPagination.prepareForTarget(event.getItemStack());

        var elements = event.getTooltipElements();

        if (!resolved.inlineSubNodes().isEmpty()) {
            elements.add(Either.right(new RegistryLibTooltipComponent(resolved.inlineSubNodes())));
        }

        var panels = resolved.separateRoots();
        if (panels.isEmpty()) {
            TooltipPagination.setPageCount(1);
            return;
        }

        int availableForPanels = estimateAvailablePanelHeight(event.getScreenHeight());
        var pages = paginatePanels(panels, availableForPanels);
        TooltipPagination.setPageCount(pages.size());

        int pageOffset = TooltipPagination.pageOffset();
        for (var panel : pages.get(pageOffset)) {
            elements.add(Either.right(new RegistryLibPanelComponent(panel)));
        }
        if (pages.size() > 1) {
            elements.add(Either.right(new RegistryLibPageControlComponent(pageOffset, pages.size())));
        }
    }

    /** 估算允许独立框占据的最大高度——和老版本 prepareLayout 的算法保持一致（屏幕高度的 2/3 减去若干余量）。 */
    private static int estimateAvailablePanelHeight(int screenHeight) {
        int reserved = screenHeight / RESERVED_HEIGHT_DIVISOR;
        return Math.max(1, screenHeight - reserved - LAYOUT_OVERHEAD);
    }

    /**
     * 按 {@link #estimateAvailablePanelHeight} 把独立框切成若干页。
     *
     * <p>
     * gather 阶段没有 event font，因此用 {@link Minecraft#font} 做高度估算；面板间距使用 {@link
     * #PANEL_GAP_ESTIMATE}（原版 tooltip 组件之间会被加 2px 间隙， 加上独立框自身想要的视觉留白后大致与这个常量一致）。
     */
    private static List<List<ResolvedRoot>> paginatePanels(
                                                           List<ResolvedRoot> panels, int availableHeight) {
        Font font = Minecraft.getInstance().font;
        List<List<ResolvedRoot>> pages = new ArrayList<>();
        List<ResolvedRoot> current = new ArrayList<>();
        int currentHeight = 0;

        for (ResolvedRoot panel : panels) {
            int panelHeight = estimatePanelHeight(panel, font);
            int contribution = panelHeight + (current.isEmpty() ? 0 : PANEL_GAP_ESTIMATE);
            if (!current.isEmpty() && currentHeight + contribution > availableHeight) {
                pages.add(current);
                current = new ArrayList<>();
                currentHeight = 0;
                contribution = panelHeight;
            }
            current.add(panel);
            currentHeight += contribution;
        }
        if (!current.isEmpty()) pages.add(current);
        if (pages.isEmpty()) pages.add(Collections.emptyList());
        return pages;
    }

    private static int estimatePanelHeight(ResolvedRoot panel, Font font) {
        int contentHeight = 0;
        for (SubNode node : panel.subNodes()) {
            int nh = node.getHeight(font);
            if (nh <= 0) nh = DEFAULT_LINE_HEIGHT;
            contentHeight += nh;
        }
        return RegistryLibClientPanelComponent.TOP_MARGIN + RegistryLibClientPanelComponent.INSET * 2 + contentHeight;
    }

    /**
     * 1.21.1 没有 {@code RenderTooltipEvent.Texture}；原版整块背景由 {@link TooltipRenderUtil} 以 {@link
     * RenderTooltipEvent.Color} 事件提供的颜色绘制。这里把整块背景改成全透明，再手动给「标题 + 内联」这一段 画一份原版风格的背景。独立框面板各自在自己的
     * renderText 里画背景。
     */
    private void onRenderTooltipColor(RenderTooltipEvent.Color event) {
        if (!containsRegistryLibComponent(event.getComponents())) return;

        // 1) 原版整块背景透明化。
        event.setBackgroundStart(0);
        event.setBackgroundEnd(0);
        event.setBorderStart(0);
        event.setBorderEnd(0);

        // 2) 重画内联区域（标题 + 内联节点）的原版风格背景。
        InlineAreaDims inline = measureInlineArea(event.getComponents(), event.getFont());
        if (inline.width > 0 && inline.height > 0) {
            TooltipRenderUtil.renderTooltipBackground(
                    event.getGraphics(),
                    event.getX(),
                    event.getY(),
                    inline.width,
                    inline.height,
                    400,
                    TOOLTIP_BACKGROUND_COLOR,
                    TOOLTIP_BACKGROUND_COLOR,
                    TOOLTIP_BORDER_COLOR_TOP,
                    TOOLTIP_BORDER_COLOR_BOTTOM);
        }
    }

    private static boolean containsRegistryLibComponent(
                                                        List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> components) {
        for (var c : components) {
            if (c instanceof RegistryLibClientTooltip || c instanceof RegistryLibClientPanelComponent || c instanceof RegistryLibClientPageControl) {
                return true;
            }
        }
        return false;
    }

    /**
     * 「内联区域」 = 原版 tooltip 顺序里、第一个独立框/分页控件出现之前的所有组件 （通常就是标题文本 + 可选的 {@link RegistryLibClientTooltip}）。
     */
    private static InlineAreaDims measureInlineArea(
                                                    List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> components,
                                                    Font font) {
        int width = 0;
        int height = 0;
        for (var c : components) {
            if (c instanceof RegistryLibClientPanelComponent || c instanceof RegistryLibClientPageControl) {
                break;
            }
            width = Math.max(width, c.getWidth(font));
            height += c.getHeight();
        }
        return new InlineAreaDims(width, height);
    }

    private record InlineAreaDims(int width, int height) {}

    private void onClientTickPost(ClientTickEvent.Post event) {
        TooltipPagination.resetIfIdle();
    }

    private void onTooltipKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        var key = event.getKeyCode() == -1 ? InputConstants.Type.SCANCODE.getOrCreate(event.getScanCode()) : InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode());
        if (TOOLTIP_PAGE_UP.isActiveAndMatches(key)) {
            if (TooltipPagination.pageUp()) {
                event.setCanceled(true);
            }
        } else if (TOOLTIP_PAGE_DOWN.isActiveAndMatches(key)) {
            if (TooltipPagination.pageDown()) {
                event.setCanceled(true);
            }
        }
    }

    public Component tooltipPageKeyHint() {
        return Component.translatable(
                "tooltip.registrylib.page_keys",
                compactKeyName(TOOLTIP_PAGE_UP),
                compactKeyName(TOOLTIP_PAGE_DOWN));
    }

    public static Component tooltipPageKeyHintStatic() {
        return tooltipPageKeyHint();
    }

    private Component compactKeyName(KeyMapping mapping) {
        return switch (mapping.saveString()) {
            case "key.keyboard.up" -> Component.literal("↑");
            case "key.keyboard.down" -> Component.literal("↓");
            case "key.keyboard.left" -> Component.literal("←");
            case "key.keyboard.right" -> Component.literal("→");
            case "key.keyboard.page.up" -> Component.literal("PgUp");
            case "key.keyboard.page.down" -> Component.literal("PgDn");
            default -> mapping.getTranslatedKeyMessage();
        };
    }
}
