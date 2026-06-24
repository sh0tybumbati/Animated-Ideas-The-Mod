package com.garrett.mod.client.mixin;

import com.garrett.mod.GarrettMod;
import com.garrett.mod.client.CookingEggRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders raw eggs cooking on a campfire as the same little 3D sunny-side-up egg the frying pan uses.
 * Only takes over when at least one slot holds an egg; otherwise vanilla rendering runs untouched. Other
 * foods are drawn with the exact vanilla transforms so they look identical.
 */
@Mixin(CampfireRenderer.class)
public class CampfireEggRenderMixin {
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gtcai$fryEggs(CampfireBlockEntity be, float partialTick, PoseStack pose,
                               MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        if (!GarrettMod.CONFIG.enableCookingEggs) return;
        NonNullList<ItemStack> items = be.getItems();
        boolean anyEgg = false;
        for (ItemStack s : items) {
            if (s.is(Items.EGG)) { anyEgg = true; break; }
        }
        if (!anyEgg) return; // let vanilla render normally

        Direction facing = be.getBlockState().getValue(CampfireBlock.FACING);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        long posSeed = be.getBlockPos().asLong();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            Direction slot = Direction.from2DDataValue((i + facing.get2DDataValue()) % 4);
            pose.pushPose();
            if (stack.is(Items.EGG)) {
                // Flat 3D egg, nudged toward its slot direction on the grate.
                pose.translate(0.5 + slot.getStepX() * 0.2, 0.44921875, 0.5 + slot.getStepZ() * 0.2);
                CookingEggRenderer.draw(pose, buffer, light, overlay);
            } else {
                // Vanilla campfire item transform.
                pose.translate(0.5, 0.44921875, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(-slot.toYRot()));
                pose.mulPose(Axis.XP.rotationDegrees(90.0f));
                pose.translate(-0.3125, -0.3125, 0.0);
                pose.scale(0.375f, 0.375f, 0.375f);
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay,
                    pose, buffer, be.getLevel(), i + (int) posSeed);
            }
            pose.popPose();
        }
        ci.cancel();
    }
}
