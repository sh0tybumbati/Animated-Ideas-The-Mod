package com.garrett.mod.mixin;

import com.garrett.mod.AirFryerBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// getTotalCookTime is private static and reads the recipe's cook time (100 for
// smoking). Halve it for the air fryer so it cooks in 50 ticks.
@Mixin(AbstractFurnaceBlockEntity.class)
public class AirFryerCookSpeedMixin {
    @Inject(method = "getTotalCookTime", at = @At("RETURN"), cancellable = true)
    private static void gtcai$airFryerCookSpeed(Level level, AbstractFurnaceBlockEntity blockEntity,
                                                CallbackInfoReturnable<Integer> cir) {
        if (blockEntity instanceof AirFryerBlockEntity) {
            cir.setReturnValue(Math.max(1, cir.getReturnValueI() / 2));
        }
    }
}
