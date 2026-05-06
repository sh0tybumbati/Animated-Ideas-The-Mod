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

        // AbstractFurnaceBlockEntity.litTime (mapped name for burnTime in some mappings)
        // We use an Accessor or cast if we can't shadow static context directly, 
        // but here we check if the furnace is LIT.
        
        // In 26.1.2 mappings, let's assume getLitTime or similar. 
        // For now, checking if it has any fuel time left.
        
        // Use an accessor-like approach via shadowing if possible, 
        // but static tick methods provide the instance.
        
        // Cast to access protected fields if in same package or use accessors.
        // Assuming 'litTime' is the field for current burn progress.
        
        if (((FurnaceGunpowderMixin)(Object)blockEntity).litTime > 0) {
            for (int i = 0; i < blockEntity.getContainerSize(); i++) {
                ItemStack stack = blockEntity.getItem(i);
                if (stack.is(Items.GUNPOWDER)) {
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
                    break;
                }
            }
        }
    }
}
