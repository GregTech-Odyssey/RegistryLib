package com.gto.registrylib.client;

import com.gto.registrylib.RegistryLib;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = RegistryLib.MOD_ID, dist = Dist.CLIENT)
public final class Client {

  private static Map<BlockEntityType<?>, BlockEntityRendererProvider> BER =
      new ConcurrentHashMap<>();

  public static void registerBER(BlockEntityType<?> type, BlockEntityRendererProvider provider) {
    BER.put(type, provider);
  }

  @SubscribeEvent
  public void onClientSetup(FMLClientSetupEvent event) {
    BER.forEach(BlockEntityRenderers::register);
    BER = null;
  }
}
