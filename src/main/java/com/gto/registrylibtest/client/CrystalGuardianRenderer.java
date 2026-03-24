package com.gto.registrylibtest.client;

import com.gto.registrylibtest.entity.CrystalGuardian;
import com.gto.registrylibtest.entity.SimpleEntityExample;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * 将晶体矿守卫者渲染为缩小的晶体矿方块模型（1/3 大小）。
 */
@OnlyIn(Dist.CLIENT)
public class CrystalGuardianRenderer extends EntityRenderer<CrystalGuardian, EntityRenderState> {

    private static final float SCALE = 0.333F;

    private List<BlockStateModelPart> cachedParts;
    private final RandomSource random = RandomSource.create();

    public CrystalGuardianRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(
                       EntityRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);

        if (cachedParts == null) {
            cachedParts = collectBlockModelParts();
        }
        if (cachedParts.isEmpty()) return;

        poseStack.pushPose();
        // 居中并缩放
        poseStack.translate(0.0F, SCALE * 0.5F, 0.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        collector.submitBlockModel(
                poseStack,
                RenderTypes.solidMovingBlock(),
                cachedParts,
                new int[] {-1},
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor);

        poseStack.popPose();
    }

    private List<BlockStateModelPart> collectBlockModelParts() {
        BlockState blockState =
                SimpleEntityExample.CRYSTAL_ORE.getDefaultState();
        BlockStateModel model = Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .get(blockState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);
        return parts;
    }
}
