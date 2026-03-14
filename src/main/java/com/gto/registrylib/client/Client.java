package com.gto.registrylib.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@UtilityClass
public class Client {

    public void init(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(Client::onClientSetup);
        modEventBus.addListener(Client::onRegisterClientExtensions);
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
}
