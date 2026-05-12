package com.gto.registrylibtest.crop;

import com.gto.registrylib.crop.RegistryLibCropBlock;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.state.SimpleStateExample;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class SimpleCropExample {

    public static final BlockEntry<RegistryLibCropBlock> ESSENCE_CARROT = RegistryLibTest.REGISTRYLIB
            .crop("essence_carrot")
            .properties(properties -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP))
            .produce(() -> Items.CARROT)
            .growthRoll((state, level, pos, random) -> {
                int ambientEssence = SimpleStateExample.AMBIENT_ESSENCE.getOrCreate(level, level.getChunk(pos).getPos());
                return random.nextInt(8) < Math.max(1, Math.min(7, ambientEssence));
            })
            .onHarvest((_state, level, pos, _player) -> {
                if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    SimpleStateExample.ESSENCE_EPOCH.set(
                            serverLevel,
                            SimpleStateExample.ESSENCE_EPOCH.getOrCreate(serverLevel) + 1);
                }
            })
            .stageTextures(
                    TextureRef.mc("block/carrots_stage0"),
                    TextureRef.mc("block/carrots_stage1"),
                    TextureRef.mc("block/carrots_stage2"),
                    TextureRef.mc("block/carrots_stage3"))
            .register();

    private SimpleCropExample() {}
}
