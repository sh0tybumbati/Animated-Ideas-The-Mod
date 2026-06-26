package com.garrett.mod.client;

import com.garrett.mod.SleepingBagBlock;
import com.garrett.mod.SleepingBagBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * Draws an applied banner design flat across the sleeping bag's bedroll. The design is stored on the
 * head half and rendered once from there, spanning both the head and foot blocks, reusing vanilla's
 * banner-pattern compositing on a flattened, scaled flag model.
 */
public class SleepingBagBlockEntityRenderer implements BlockEntityRenderer<SleepingBagBlockEntity> {
    private final ModelPart flag;

    public SleepingBagBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        // A flag cropped to its middle 32px (4px off the top and bottom of the normal 40px flag), so the
        // bedroll keeps a strip of bed showing at the head (the pillow) without losing much of the design.
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("flag",
            CubeListBuilder.create().texOffs(0, 4).addBox(-10.0f, 0.0f, -2.0f, 20.0f, 32.0f, 1.0f),
            PartPose.ZERO);
        this.flag = LayerDefinition.create(mesh, 64, 64).bakeRoot().getChild("flag");
    }

    @Override
    public void render(SleepingBagBlockEntity bag, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!bag.hasBanner()) return;
        BlockState state = bag.getBlockState();
        if (!(state.getBlock() instanceof SleepingBagBlock)) return;
        if (state.getValue(BedBlock.PART) != BedPart.HEAD) return; // render once, spanning both halves

        Direction footDir = BedBlock.getConnectedDirection(state); // points from head toward foot

        pose.pushPose();
        pose.translate(0.5, 0.103, 0.5);                             // head block centre, just above the bedroll top
        pose.mulPose(Axis.YP.rotationDegrees(-footDir.toYRot()));    // length axis -> foot direction
        pose.translate(0.0, 0.0, -0.1);                              // anchored to the foot end; leaves the head pillow showing
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));                // lay the flag flat
        pose.scale(0.8f, 0.8f, 0.8f);                                // 20px wide -> 1 block; cropped 32px -> 1.6 blocks toward the foot

        flag.xRot = 0.0f;
        flag.visible = true;
        BannerRenderer.renderPatterns(pose, buffer, packedLight, packedOverlay, flag,
            Sheets.BANNER_BASE, true, bag.getBaseColor(), bag.getPatterns());
        pose.popPose();
    }
}
