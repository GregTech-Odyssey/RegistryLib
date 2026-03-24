package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibTooltipComponent;
import com.gto.registrylib.tooltip.TooltipRegistry;

import com.mojang.datafixers.util.Either;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidType;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@UtilityClass
public class Client {

    private final AtomicReference<ConcurrentHashMap<Supplier<BlockEntityType<?>>, BlockEntityRendererProvider>> BER = new AtomicReference<>(new ConcurrentHashMap<>());

    private final AtomicReference<ConcurrentHashMap<Supplier<FluidType>, IClientFluidTypeExtensions>> FLUID_TYPE_EXTENSIONS = new AtomicReference<>(new ConcurrentHashMap<>());

    private final AtomicReference<ConcurrentHashMap<Supplier<EntityType<?>>, EntityRendererProvider>> ENTITY_RENDERERS = new AtomicReference<>(new ConcurrentHashMap<>());

    public void init(IEventBus modEventBus) {
        modEventBus.addListener(Client::onClientSetup);
        modEventBus.addListener(Client::onRegisterClientExtensions);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerEntityRenderer(Supplier<EntityType<?>> type, EntityRendererProvider renderer) {
        var map = ENTITY_RENDERERS.get();
        if (map != null) map.put(type, renderer);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        var map = BER.getAndSet(null);
        if (map != null)
            map.forEach((type, provider) -> BlockEntityRenderers.register(type.get(), provider));
    }

    private void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        var map = FLUID_TYPE_EXTENSIONS.getAndSet(null);
        if (map != null)
            map.forEach((type, extensions) -> event.registerFluidType(extensions, type.get()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        var map = ENTITY_RENDERERS.getAndSet(null);
        if (map != null)
            map.forEach((type, renderer) -> event.registerEntityRenderer((EntityType) type.get(), renderer));
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
