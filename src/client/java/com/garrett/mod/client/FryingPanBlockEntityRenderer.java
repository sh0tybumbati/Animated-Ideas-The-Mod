package com.garrett.mod.client;

import com.garrett.mod.FryingPanBlock;
import com.garrett.mod.FryingPanBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Draws the up-to-two held foods lying flat in the pan, just above its rim, oriented by the handle. */
public class FryingPanBlockEntityRenderer implements BlockEntityRenderer<FryingPanBlockEntity> {
    private final ItemRenderer itemRenderer;

    public FryingPanBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(FryingPanBlockEntity pan, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = pan.getBlockState().getValue(FryingPanBlock.FACING);
        NonNullList<ItemStack> items = pan.getItems();
        int seed = (int) pan.getBlockPos().asLong();

        // Four slots in a 2x2 grid; centers ±2.5px give a 2px gap between the 3px items.
        float d = 2.5f / 16f;
        float[][] offsets = { { -d, -d }, { d, -d }, { -d, d }, { d, d } };
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.5f, 1f / 16f, 0.5f); // resting on the pan's inner floor (1px floor)
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            pose.translate(offsets[i][0], 0f, offsets[i][1]);
            if (stack.is(Items.EGG)) {
                // Raw eggs render as a tiny sunny-side-up egg while they fry.
                CookingEggRenderer.draw(pose, buffer, packedLight, packedOverlay);
            } else {
                pose.translate(0f, 0.06f, 0f); // lift other foods so they don't clip the floor
                pose.mulPose(Axis.XP.rotationDegrees(90.0f)); // lay the item flat
                pose.scale(0.3f, 0.3f, 0.3f);
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    pose, buffer, pan.getLevel(), seed + i);
            }
            pose.popPose();
        }
    }
}
