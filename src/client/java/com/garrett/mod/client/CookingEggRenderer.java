package com.garrett.mod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Draws a tiny sunny-side-up egg sitting on the XZ plane at the current pose origin: a 3x3-pixel white
 * slab with a raised 1x1-pixel yellow yolk cube on top. Pixels are pan-scale (1px = 1/16 block). Used in
 * place of the egg item while it cooks in a pan (and can be reused for any campfire-style cooker).
 */
public final class CookingEggRenderer {
    private static final ResourceLocation WHITE =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png");

    private static final float PX = 1f / 16f;

    private CookingEggRenderer() {}

    public static void draw(PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        VertexConsumer v = buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));
        // white: 3x3 footprint, half a pixel thick, sitting on the surface.
        box(v, stack, 1.5f * PX, 0f, 0.5f * PX, 1f, 1f, 1f, packedLight, packedOverlay);
        // yolk: 1x1 footprint, half a pixel tall, raised on top of the white.
        box(v, stack, 0.5f * PX, 0.5f * PX, 1.0f * PX, 0.96f, 0.82f, 0.30f, packedLight, packedOverlay);
    }

    /** A square box of half-extent {@code h} (X/Z), from height {@code y0} to {@code y1}, tinted (r,g,b). */
    private static void box(VertexConsumer v, PoseStack stack, float h, float y0, float y1,
                            float r, float g, float b, int light, int overlay) {
        Matrix4f m = stack.last().pose();
        PoseStack.Pose lp = stack.last();
        // top
        quad(v, m, lp, -h, y1, -h,  h, y1, -h,  h, y1, h, -h, y1, h, r, g, b, light, overlay, 0, 1, 0);
        // bottom
        quad(v, m, lp, -h, y0, h,  h, y0, h,  h, y0, -h, -h, y0, -h, r, g, b, light, overlay, 0, -1, 0);
        // north (-z)
        quad(v, m, lp, -h, y1, -h, -h, y0, -h,  h, y0, -h,  h, y1, -h, r, g, b, light, overlay, 0, 0, -1);
        // south (+z)
        quad(v, m, lp,  h, y1, h,  h, y0, h, -h, y0, h, -h, y1, h, r, g, b, light, overlay, 0, 0, 1);
        // west (-x)
        quad(v, m, lp, -h, y1, h, -h, y0, h, -h, y0, -h, -h, y1, -h, r, g, b, light, overlay, -1, 0, 0);
        // east (+x)
        quad(v, m, lp,  h, y1, -h,  h, y0, -h,  h, y0, h,  h, y1, h, r, g, b, light, overlay, 1, 0, 0);
    }

    private static void quad(VertexConsumer v, Matrix4f m, PoseStack.Pose lp,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float r, float g, float b, int light, int overlay,
                             float nx, float ny, float nz) {
        v.addVertex(m, x0, y0, z0).setColor(r, g, b, 1f).setUv(0.5f, 0.5f).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        v.addVertex(m, x1, y1, z1).setColor(r, g, b, 1f).setUv(0.5f, 0.5f).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        v.addVertex(m, x2, y2, z2).setColor(r, g, b, 1f).setUv(0.5f, 0.5f).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        v.addVertex(m, x3, y3, z3).setColor(r, g, b, 1f).setUv(0.5f, 0.5f).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
    }
}
