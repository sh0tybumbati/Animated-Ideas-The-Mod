package com.garrett.mod.client;

import com.garrett.mod.CanvasBlock;
import com.garrett.mod.CanvasBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class CanvasBlockEntityRenderer implements BlockEntityRenderer<CanvasBlockEntity> {

    // ABGR for NativeImage, indexed by DyeColor.getId()
    private static final int[] DYE_ABGR = new int[16];

    static {
        for (DyeColor dye : DyeColor.values()) {
            int rgb = dye.getTextureDiffuseColor() & 0xFFFFFF;
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            DYE_ABGR[dye.getId()] = (0xFF << 24) | (b << 16) | (g << 8) | r;
        }
    }

    private static final Map<BlockPos, DynamicTexture> TEXTURES = new HashMap<>();
    private static final Map<BlockPos, ResourceLocation> TEX_LOCS = new HashMap<>();

    public CanvasBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CanvasBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof CanvasBlock canvas)) return;

        Direction facing = state.getValue(CanvasBlock.FACING);
        byte[] pixels = entity.getPixels();
        BlockPos pos = entity.getBlockPos().immutable();
        int bgAbgr = DYE_ABGR[canvas.color.getId()];

        // Get or create a DynamicTexture for this canvas position
        DynamicTexture tex = TEXTURES.computeIfAbsent(pos, p -> new DynamicTexture(16, 16, false));
        ResourceLocation texLoc = TEX_LOCS.computeIfAbsent(pos, p -> {
            String name = "canvas/" + (pos.getX() + "_" + pos.getY() + "_" + pos.getZ())
                    .replace('-', 'n');
            return Minecraft.getInstance().getTextureManager().register(name, tex);
        });

        // Write pixel data into the texture
        var img = tex.getPixels();
        if (img != null) {
            for (int py = 0; py < 16; py++) {
                for (int px = 0; px < 16; px++) {
                    byte id = pixels[py * 16 + px];
                    int abgr = (id >= 0 && id < 16) ? DYE_ABGR[id] : bgAbgr;
                    img.setPixelRGBA(px, py, abgr);
                }
            }
            tex.upload();
        }

        // Render a single quad covering the canvas face
        poseStack.pushPose();
        setupFacingTransform(poseStack, facing);

        VertexConsumer v = bufferSource.getBuffer(RenderType.entitySolid(texLoc));
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose lp = poseStack.last();
        float z = 14.0f / 16.0f;

        v.addVertex(pose, 0, 1, z).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(packedOverlay).setLight(packedLight).setNormal(lp,0,0,-1);
        v.addVertex(pose, 0, 0, z).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(packedOverlay).setLight(packedLight).setNormal(lp,0,0,-1);
        v.addVertex(pose, 1, 0, z).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(packedOverlay).setLight(packedLight).setNormal(lp,0,0,-1);
        v.addVertex(pose, 1, 1, z).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(packedOverlay).setLight(packedLight).setNormal(lp,0,0,-1);

        poseStack.popPose();
    }

    private void setupFacingTransform(PoseStack stack, Direction facing) {
        stack.translate(0.5, 0.5, 0.5);
        float yRot = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case EAST  -> -90;
            case WEST  -> 90;
            default    -> 0;
        };
        stack.mulPose(Axis.YP.rotationDegrees(yRot));
        stack.translate(-0.5, -0.5, -0.5);
    }
}
