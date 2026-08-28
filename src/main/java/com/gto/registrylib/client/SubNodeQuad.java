package com.gto.registrylib.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import org.joml.Matrix4f;

/**
 * 1.21.1 tooltip text-pass 矩形绘制工具：在 {@link MultiBufferSource.BufferSource} 中写入 {@link
 * RenderType#gui()} quad，用于绘制必须位于文本下方的背景/边框。
 */
final class SubNodeQuad {

    private SubNodeQuad() {}

    static void fill(
                     Matrix4f matrix,
                     MultiBufferSource.BufferSource bufferSource,
                     float x0,
                     float y0,
                     float x1,
                     float y1,
                     int color) {
        if (x0 < x1 && y0 < y1) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
            buffer.addVertex(matrix, x0, y0, 0.0F).setColor(color);
            buffer.addVertex(matrix, x0, y1, 0.0F).setColor(color);
            buffer.addVertex(matrix, x1, y1, 0.0F).setColor(color);
            buffer.addVertex(matrix, x1, y0, 0.0F).setColor(color);
        }
    }

    static void fillGradient(
                             Matrix4f matrix,
                             MultiBufferSource.BufferSource bufferSource,
                             float x0,
                             float y0,
                             float x1,
                             float y1,
                             int colorTop,
                             int colorBottom) {
        if (x0 < x1 && y0 < y1) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
            buffer.addVertex(matrix, x0, y0, 0.0F).setColor(colorTop);
            buffer.addVertex(matrix, x0, y1, 0.0F).setColor(colorBottom);
            buffer.addVertex(matrix, x1, y1, 0.0F).setColor(colorBottom);
            buffer.addVertex(matrix, x1, y0, 0.0F).setColor(colorTop);
        }
    }
}
