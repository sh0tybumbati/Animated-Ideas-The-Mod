package com.garrett.mod.client;

import com.garrett.mod.CarvablePumpkinBlock;
import com.garrett.mod.CarvablePumpkinBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/** Draws each horizontal face's 8x8 carve grid: dark holes, glowing yellow-white when lit. */
public class CarvablePumpkinBlockEntityRenderer implements BlockEntityRenderer<CarvablePumpkinBlockEntity> {

    // Stored as ABGR (NativeImage order).
    private static final int CARVED_DARK = 0xFF000000;   // opaque black hole
    private static final int CARVED_GLOW = 0xFFAAEEFF;   // warm yellow-white (rgb 0xFFEEAA) when lit
    private static final int CLEAR = 0x00000000;         // uncarved -> pumpkin shows through

    private static final Direction[] HORIZONTALS = {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private static final Map<String, DynamicTexture> TEXTURES = new HashMap<>();
    private static final Map<String, ResourceLocation> TEX_LOCS = new HashMap<>();

    public CarvablePumpkinBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CarvablePumpkinBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof CarvablePumpkinBlock)) return;
        boolean lit = state.getValue(CarvablePumpkinBlock.LIT);
        BlockPos pos = entity.getBlockPos().immutable();
        int onColor = lit ? CARVED_GLOW : CARVED_DARK;
        int light = lit ? LightTexture.FULL_BRIGHT : packedLight;

        int n = CarvablePumpkinBlockEntity.SIZE;
        for (Direction face : HORIZONTALS) {
            if (entity.faceEmpty(face)) continue; // nothing carved on this face

            String key = pos.getX() + "_" + pos.getY() + "_" + pos.getZ() + "_" + face.getName();
            DynamicTexture tex = TEXTURES.computeIfAbsent(key, k -> new DynamicTexture(n, n, false));
            ResourceLocation texLoc = TEX_LOCS.computeIfAbsent(key, k ->
                Minecraft.getInstance().getTextureManager().register("carvable_pumpkin/" + k.replace('-', 'n'), tex));

            var img = tex.getPixels();
            if (img != null) {
                for (int y = 0; y < n; y++) {
                    for (int x = 0; x < n; x++) {
                        img.setPixelRGBA(x, y, entity.isCarved(face, y * n + x) ? onColor : CLEAR);
                    }
                }
                tex.upload();
            }

            poseStack.pushPose();
            setupFacingTransform(poseStack, face);
            VertexConsumer v = bufferSource.getBuffer(RenderType.entityTranslucentCull(texLoc));
            Matrix4f pose = poseStack.last().pose();
            PoseStack.Pose lp = poseStack.last();
            float z = -0.002f; // just outside the face in the rotated frame
            v.addVertex(pose, 0, 1, z).setColor(1f,1f,1f,1f).setUv(1,0).setOverlay(packedOverlay).setLight(light).setNormal(lp,0,0,-1);
            v.addVertex(pose, 1, 1, z).setColor(1f,1f,1f,1f).setUv(0,0).setOverlay(packedOverlay).setLight(light).setNormal(lp,0,0,-1);
            v.addVertex(pose, 1, 0, z).setColor(1f,1f,1f,1f).setUv(0,1).setOverlay(packedOverlay).setLight(light).setNormal(lp,0,0,-1);
            v.addVertex(pose, 0, 0, z).setColor(1f,1f,1f,1f).setUv(1,1).setOverlay(packedOverlay).setLight(light).setNormal(lp,0,0,-1);
            poseStack.popPose();
        }
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
