package com.gto.registrylib.providers.generators;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.providers.RegistryLibProvider;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;

public class RegistryLibModelProvider extends ModelProvider implements RegistryLibProvider {

    private final RegistryCore parent;

    public RegistryLibModelProvider(RegistryCore parent, PackOutput packOutput) {
        super(packOutput, parent.getModid());
        this.parent = parent;
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
