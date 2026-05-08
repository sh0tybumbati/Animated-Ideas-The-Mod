package com.garrett.mod.client;

import com.garrett.mod.CanvasBlock;
import com.garrett.mod.CanvasBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class CanvasBlockEntityRenderer implements BlockEntityRenderer<CanvasBlockEntity> {

    // Packed ARGB colors for each DyeColor id
    private static final int[] DYE_COLORS = new int[16];

    static {
        for (DyeColor dye : DyeColor.values()) {
            float[] c = dye.getTextureDiffuseColors();
            int r = (int)(c[0] * 255);
            int g = (int)(c[1] * 255);
            int b = (int)(c[2] * 255);
            DYE_COLORS[dye.getId()] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }

    public CanvasBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CanvasBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof CanvasBlock canvas)) return;

        Direction facing = state.getValue(CanvasBlock.FACING);
        byte[] pixels = entity.getPixels();

        // Background color from canvas block's DyeColor
        int bgColor = DYE_COLORS[canvas.color.getId()];

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        poseStack.pushPose();
        setupFacingTransform(poseStack, facing);

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float depth = 14.0f / 16.0f; // front face sits at Z=14/16 in NORTH orientation

        for (int py = 0; py < 16; py++) {
            for (int px = 0; px < 16; px++) {
                int idx = py * 16 + px;
                byte colorId = pixels[idx];
                int color = (colorId >= 0 && colorId < 16) ? DYE_COLORS[colorId] : bgColor;

                float x0 = px / 16.0f;
                float x1 = (px + 1) / 16.0f;
                float y0 = 1.0f - (py + 1) / 16.0f;
                float y1 = 1.0f - py / 16.0f;

                float r = ((color >> 16) & 0xFF) / 255.0f;
                float g = ((color >> 8) & 0xFF) / 255.0f;
                float b = (color & 0xFF) / 255.0f;

                vertex(consumer, pose, normal, x0, y1, depth, r, g, b, packedLight, packedOverlay);
                vertex(consumer, pose, normal, x0, y0, depth, r, g, b, packedLight, packedOverlay);
                vertex(consumer, pose, normal, x1, y0, depth, r, g, b, packedLight, packedOverlay);
                vertex(consumer, pose, normal, x1, y1, depth, r, g, b, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private void vertex(VertexConsumer v, Matrix4f pose, Matrix3f normal,
                        float x, float y, float z, float r, float g, float b,
                        int light, int overlay) {
        v.addVertex(pose, x, y, z)
         .setColor(r, g, b, 1.0f)
         .setUv(0, 0)
         .setOverlay(overlay)
         .setLight(light)
         .setNormal(normal, 0, 0, -1);
    }

    private void setupFacingTransform(PoseStack stack, Direction facing) {
        // Translate to block center, rotate to face, translate back
        stack.translate(0.5f, 0.5f, 0.5f);
        float yRot = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case EAST  -> -90;
            case WEST  -> 90;
            default    -> 0;
        };
        stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        stack.translate(-0.5f, -0.5f, -0.5f);
    }
}
