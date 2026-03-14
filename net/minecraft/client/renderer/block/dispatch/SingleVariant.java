package net.minecraft.client.renderer.block.dispatch;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SingleVariant implements BlockStateModel {
    private final BlockStateModelPart model;

    public SingleVariant(BlockStateModelPart model) {
        this.model = model;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        output.add(this.model);
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.model.particleMaterial();
    }

    @Override
    public boolean hasTranslucency() {
        return this.model.hasTranslucency();
    }

    @Override
    public Object createGeometryKey(net.minecraft.client.renderer.block.BlockAndTintGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, RandomSource random) {
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public record Unbaked(Variant variant) implements BlockStateModel.Unbaked {
        public static final com.mojang.serialization.MapCodec<SingleVariant.Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(SingleVariant.Unbaked::new, SingleVariant.Unbaked::variant);
        public static final Codec<SingleVariant.Unbaked> CODEC = MAP_CODEC.codec();

        @Override
        public BlockStateModel bake(ModelBaker modelBakery) {
            return new SingleVariant(this.variant.bake(modelBakery));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.variant.resolveDependencies(resolver);
        }
    }
}
