package com.gto.registrylibtest.client;

import com.gto.registrylibtest.TimerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TimerBlockEntityRenderer
    implements BlockEntityRenderer<TimerBlockEntity, TimerBlockEntityRenderState> {

  public TimerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public TimerBlockEntityRenderState createRenderState() {
    return new TimerBlockEntityRenderState();
  }

  @Override
  public void extractRenderState(
      TimerBlockEntity blockEntity,
      TimerBlockEntityRenderState state,
      float partialTicks,
      Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(
        blockEntity, state, partialTicks, cameraPosition, breakProgress);
    state.tier = blockEntity.getTier();
    state.count = blockEntity.getCount();
  }

  @Override
  public void submit(
      TimerBlockEntityRenderState state,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState camera) {
    int tier = state.tier;
    int light = state.lightCoords;

    // Render "Tier N" on top face
    renderTopText(poseStack, collector, "Tier " + tier, light);

    // Render the elapsed seconds on all 4 side faces
    String number = String.valueOf(state.count);
    renderSideFaceText(poseStack, collector, number, light, 0);   // South (Z+)
    renderSideFaceText(poseStack, collector, number, light, 90);  // East  (X+)
    renderSideFaceText(poseStack, collector, number, light, 180); // North (Z-)
    renderSideFaceText(poseStack, collector, number, light, 270); // West  (X-)
  }

  private void renderTopText(
      PoseStack poseStack, SubmitNodeCollector collector, String text, int light) {
    poseStack.pushPose();

    // Move to top center of the block
    poseStack.translate(0.5f, 1.001f, 0.5f);
    // Rotate to face up: Rx(-90) maps local +Z -> world +Y, so text is visible from above
    poseStack.mulPose(Axis.XP.rotationDegrees(-90));
    // Scale down - text is rendered at ~8px per character
    float scale = 1.0f / 96.0f;
    poseStack.scale(scale, -scale, scale);

    var formattedText = Component.literal(text).getVisualOrderText();
    // Center: assume ~6px per char
    float textWidth = text.length() * 6.0f;
    float x = -textWidth / 2.0f;
    float y = -4.0f;

    collector.submitText(
        poseStack,
        x,
        y,
        formattedText,
        false, // no drop shadow
        Font.DisplayMode.SEE_THROUGH,
        light,
        0xFFFFFFFF, // white
        0, // no background
        0 // no outline
        );

    poseStack.popPose();
  }

  private void renderSideFaceText(
      PoseStack poseStack, SubmitNodeCollector collector, String text, int light, float yRotation) {
    poseStack.pushPose();

    // Move to center of block
    poseStack.translate(0.5f, 0.5f, 0.5f);
    // Rotate so that local +Z faces the target side
    poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
    // Move to just outside the face surface
    poseStack.translate(0.0f, 0.0f, 0.501f);
    // No extra flip needed: text in local XY is already visible from the local +Z side (outward)
    // Scale down
    float scale = 1.0f / 48.0f;
    poseStack.scale(scale, -scale, scale);

    var formattedText = Component.literal(text).getVisualOrderText();
    float textWidth = text.length() * 6.0f;
    float x = -textWidth / 2.0f;
    float y = -4.0f;

    collector.submitText(
        poseStack,
        x,
        y,
        formattedText,
        false,
        Font.DisplayMode.SEE_THROUGH,
        light,
        0xFFFFFF00, // yellow
        0,
        0);

    poseStack.popPose();
  }
}
