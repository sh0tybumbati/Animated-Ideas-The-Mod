package com.garrett.mod.client.mixin;

import com.garrett.mod.CanvasBlock;
import com.garrett.mod.CanvasBlockEntity;
import com.garrett.mod.EyedropperPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class PickBlockMixin {

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private void garrettmod_eyedropper(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (!(mc.hitResult instanceof BlockHitResult blockHit)) return;

        ClientLevel level = mc.level;
        if (level == null) return;

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CanvasBlock canvas)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CanvasBlockEntity canvasEntity)) return;

        int pixel = canvas.hitToPixel(state.getValue(CanvasBlock.FACING), blockHit.getLocation(), pos);
        if (pixel < 0) return;

        int rgb = canvasEntity.getPixels()[pixel];
        if (rgb == CanvasBlockEntity.UNPAINTED) return;

        // Snap to nearest DyeColor only here so the player gets a holdable dye item
        DyeColor nearest = CanvasBlock.nearestDyeColor(rgb);
        ClientPlayNetworking.send(new EyedropperPayload(nearest.getId()));
        ci.cancel();
    }
}
