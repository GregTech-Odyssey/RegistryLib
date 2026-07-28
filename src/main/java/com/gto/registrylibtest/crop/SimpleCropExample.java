package com.gto.registrylibtest.crop;

import com.gto.registrylib.crop.RegistryLibCropBlock;
import com.gto.registrylib.util.TextureRef;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.state.SimpleStateExample;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

public final class SimpleCropExample {

    public static final BlockEntry<RegistryLibCropBlock> ESSENCE_CARROT = RegistryLibTest.REGISTRYLIB
            .crop("essence_carrot")
            .properties(
                    properties -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollision()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.CROP))
            .seedItem(seed -> seed.existingTexture(TextureRef.mc("item/carrot")))
            .produce(() -> Items.CARROT)
            .growthRoll(
                    (state, level, pos, random) -> {
                        int ambientEssence = SimpleStateExample.AMBIENT_ESSENCE.getOrCreate(
                                level, level.getChunk(pos).getPos());
                        return random.nextInt(8) < Math.clamp(ambientEssence, 1, 7);
                    })
            .onHarvest(
                    (_state, level, pos, _player) -> {
                        if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            SimpleStateExample.ESSENCE_EPOCH.set(
                                    serverLevel, SimpleStateExample.ESSENCE_EPOCH.getOrCreate(serverLevel) + 1);
                        }
                    })
            .stageTextures(
                    TextureRef.mc("block/carrots_stage0"),
                    TextureRef.mc("block/carrots_stage1"),
                    TextureRef.mc("block/carrots_stage2"),
                    TextureRef.mc("block/carrots_stage3"))
            .register();

    public static final BlockEntry<RegistryLibCropBlock> ESSENCE_POTATO = RegistryLibTest.REGISTRYLIB
            .crop("essence_potato")
            .properties(
                    properties -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollision()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.CROP))
            .seedItem(seed -> seed.existingTexture(TextureRef.mc("item/potato")))
            .loot(
                    (tables, block) -> tables.add(
                            block,
                            tables.createCropDrops(
                                    block,
                                    Items.POTATO,
                                    Items.POTATO,
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(
                                                    StatePropertiesPredicate.Builder.properties()
                                                            .hasProperty(CropBlock.AGE, block.getMaxAge())))))
            .stageTextures(
                    TextureRef.mc("block/potatoes_stage0"),
                    TextureRef.mc("block/potatoes_stage1"),
                    TextureRef.mc("block/potatoes_stage2"),
                    TextureRef.mc("block/potatoes_stage3"))
            .register();

    private SimpleCropExample() {}
}
