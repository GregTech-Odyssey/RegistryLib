package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibTooltipComponent;
import com.gto.registrylib.tooltip.TooltipRegistry;

import com.mojang.datafixers.util.Either;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidType;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@UtilityClass
public class Client {

    private final AtomicReference<ConcurrentHashMap<Supplier<BlockEntityType<?>>, BlockEntityRendererProvider>> BER = new AtomicReference<>(new ConcurrentHashMap<>());

    private final AtomicReference<ConcurrentHashMap<Supplier<FluidType>, IClientFluidTypeExtensions>> FLUID_TYPE_EXTENSIONS = new AtomicReference<>(new ConcurrentHashMap<>());

    private final ConcurrentHashMap<Supplier<? extends Block>, List<BlockTintSource>> BLOCK_TINT_SOURCES = new ConcurrentHashMap<>();

    /**
     * 待注册的流体模型 (NeoForge 26.1+)。
     *
     * <p>
     * key = 单个 {@code FluidBuilder} 的稳定注册键，value = 该 builder 当前最终的流体模型配置。 同一个 builder 多次调用 {@code
     * clientExtension(...)} 时，后一次必须覆盖前一次，而不是在事件中重复注册。
     *
     * <p>
     * 与 {@link RegisterClientExtensionsEvent} 不同，{@link RegisterFluidModelsEvent} 会在模型重新加载时再次触发，
     * 因此这里不能在首次触发后清空注册表。
     */
    private final ConcurrentHashMap<Object, FluidModelRegistration> FLUID_MODELS = new ConcurrentHashMap<>();

    public record FluidModelRegistration(
                                         Supplier<? extends Fluid> still,
                                         Supplier<? extends Fluid> flowing,
                                         FluidModel.Unbaked model) {}

    private final AtomicReference<ConcurrentHashMap<Supplier<EntityType<?>>, EntityRendererProvider>> ENTITY_RENDERERS = new AtomicReference<>(new ConcurrentHashMap<>());

    public void init(IEventBus modEventBus) {
        modEventBus.addListener(Client::onClientSetup);
        modEventBus.addListener(Client::onRegisterClientExtensions);
        modEventBus.addListener(Client::onRegisterFluidModels);
        modEventBus.addListener(Client::onRegisterBlockTintSources);
        modEventBus.addListener(Client::onRegisterTooltipFactories);
        modEventBus.addListener(Client::onRegisterEntityRenderers);
        NeoForge.EVENT_BUS.addListener(Client::onGatherTooltipComponents);
    }

    public void registerBER(Supplier<BlockEntityType<?>> type, BlockEntityRendererProvider provider) {
        var map = BER.get();
        if (map != null) map.put(type, provider);
    }

    public void registerFluidTypeExtensions(
                                            Supplier<FluidType> type, IClientFluidTypeExtensions extensions) {
        var map = FLUID_TYPE_EXTENSIONS.get();
        if (map != null) map.put(type, extensions);
    }

    /**
     * 注册一个 {@link FluidModel.Unbaked}，会在 {@link RegisterFluidModelsEvent} 中绑定到给定的源/流动流体上。
     *
     * <p>
     * 替代 NeoForge 26.1 之前的 {@link IClientFluidTypeExtensions#getStillTexture()} / {@code
     * getFlowingTexture()} / {@code getTintColor()}。
     */
    public void registerFluidModel(
                                   Object registrationKey,
                                   Supplier<? extends Fluid> still,
                                   Supplier<? extends Fluid> flowing,
                                   FluidModel.Unbaked model) {
        FLUID_MODELS.put(registrationKey, new FluidModelRegistration(still, flowing, model));
    }

    public void registerBlockTintSources(
                                         Supplier<? extends Block> block, BlockTintSource... tintSources) {
        BLOCK_TINT_SOURCES.put(block, List.of(tintSources.clone()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerEntityRenderer(
                                       Supplier<EntityType<?>> type, EntityRendererProvider renderer) {
        var map = ENTITY_RENDERERS.get();
        if (map != null) map.put(type, renderer);
    }

    @SuppressWarnings("unused")
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

    private void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        FLUID_MODELS.forEach(
                (registrationKey, registration) -> event.register(
                        registration.model(), registration.still().get(), registration.flowing().get()));
    }

    private void onRegisterBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        BLOCK_TINT_SOURCES.forEach((block, tintSources) -> event.register(tintSources, block.get()));
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
    }

    private void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().isEmpty()) return;
        var component = TooltipRegistry.resolve(event.getItemStack());
        if (component == null) return;
        event.getTooltipElements().add(Either.right(component));
    }
}
