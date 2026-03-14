package com.gto.registrylib.client;

import com.gto.registrylib.tooltip.RegistryLibTooltipComponent;
import com.gto.registrylib.tooltip.TooltipRegistry;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidType;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@UtilityClass
public class Client {

    public void init(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(Client::onClientSetup);
        modEventBus.addListener(Client::onRegisterClientExtensions);
        modEventBus.addListener(Client::onRegisterTooltipFactories);
        NeoForge.EVENT_BUS.addListener(Client::onGatherTooltipComponents);
    }

    private ConcurrentHashMap<Supplier<BlockEntityType<?>>, BlockEntityRendererProvider> BER = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Supplier<FluidType>, IClientFluidTypeExtensions> FLUID_TYPE_EXTENSIONS = new ConcurrentHashMap<>();

    public void registerBER(Supplier<BlockEntityType<?>> type, BlockEntityRendererProvider provider) {
        BER.put(type, provider);
    }

    public void registerFluidTypeExtensions(
                                            Supplier<FluidType> type, IClientFluidTypeExtensions extensions) {
        FLUID_TYPE_EXTENSIONS.put(type, extensions);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        BER.forEach(((type, provider) -> BlockEntityRenderers.register(type.get(), provider)));
        BER = null;
    }

    private void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        FLUID_TYPE_EXTENSIONS.forEach(
                (type, extensions) -> event.registerFluidType(extensions, type.get()));
        FLUID_TYPE_EXTENSIONS = null;
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
