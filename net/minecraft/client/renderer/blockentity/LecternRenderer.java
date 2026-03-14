package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class LecternRenderer implements BlockEntityRenderer<LecternBlockEntity, LecternRenderState> {
    private final SpriteGetter sprites;
    private final BookModel bookModel;
    private static final BookModel.State BOOK_STATE = BookModel.State.forAnimation(0.0F, 0.1F, 0.9F, 1.2F);

    public LecternRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    public LecternRenderState createRenderState() {
        return new LecternRenderState();
    }

    public void extractRenderState(
        LecternBlockEntity blockEntity,
        LecternRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.hasBook = blockEntity.getBlockState().getValue(LecternBlock.HAS_BOOK);
        state.yRot = blockEntity.getBlockState().getValue(LecternBlock.FACING).getClockWise().toYRot();
    }

    public void submit(LecternRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.hasBook) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.0625F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
            poseStack.translate(0.0F, -0.125F, 0.0F);
            submitNodeCollector.submitModel(
                this.bookModel,
                BOOK_STATE,
                poseStack,
                EnchantTableRenderer.BOOK_TEXTURE.renderType(RenderTypes::entitySolid),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.sprites.get(EnchantTableRenderer.BOOK_TEXTURE),
                0,
                state.breakProgress
            );
            poseStack.popPose();
        }
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(LecternBlockEntity blockEntity) {
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        return new net.minecraft.world.phys.AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.5, pos.getZ() + 1.0);
    }
}
