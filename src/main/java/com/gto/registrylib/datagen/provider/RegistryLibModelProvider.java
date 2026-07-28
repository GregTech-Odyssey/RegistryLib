package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.generator.RegistryLibBlockModelGenerator;
import com.gto.registrylib.datagen.generator.RegistryLibItemModelGenerator;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;

import java.util.stream.Stream;

public class RegistryLibModelProvider extends ModelProvider implements RegistryLibProvider {

    private final RegistryCore parent;

    public RegistryLibModelProvider(RegistryCore parent, PackOutput packOutput) {
        super(packOutput, parent.getModid());
        this.parent = parent;
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks()
                .filter(
                        holder -> holder
                                .unwrapKey()
                                .map(k -> !parent.isBlockExcludedFromModelValidation(k.identifier().getPath()))
                                .orElse(true));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        new RegistryLibBlockModelGenerator(
                parent,
                blockModels.blockStateOutput,
                blockModels.itemModelOutput,
                blockModels.modelOutput)
                .run();
        new RegistryLibItemModelGenerator(parent, itemModels.itemModelOutput, itemModels.modelOutput)
                .run();
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }
}
