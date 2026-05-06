package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceGunpowderMixin {

    @Shadow
    protected int litTime;

    @Inject(method = "tick", at = @At("TAIL"))
    private static void onTick(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClient) return;
        if (!GarrettMod.CONFIG.enableGunpowderExplosions) return;

        // In AbstractFurnaceBlockEntity:
        // Slot 0: Input
        // Slot 1: Fuel
        // Slot 2: Output
        
        // We check if the furnace is lit (litTime > 0) AND has gunpowder in the input slot
        if (((FurnaceGunpowderMixin)(Object)blockEntity).litTime > 0) {
            ItemStack inputStack = blockEntity.getItem(0);
            if (inputStack.is(Items.GUNPOWDER)) {
                world.explode(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    3.0f,
                    true,
                    World.ExplosionSourceType.BLOCK
                );
                world.destroyBlock(pos, false);
            }
        }
    }
}
