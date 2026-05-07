package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceGunpowderMixin {

    @Shadow
    protected int litTime;

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void onServerTick(Level world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (!GarrettMod.CONFIG.enableGunpowderExplosions) return;
        if (((FurnaceGunpowderMixin) (Object) blockEntity).litTime <= 0) return;

        ItemStack inputStack = blockEntity.getItem(0);
        if (inputStack.is(Items.GUNPOWDER)) {
            world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0f, Level.ExplosionInteraction.BLOCK);
            world.destroyBlock(pos, false);
        }
    }
}
