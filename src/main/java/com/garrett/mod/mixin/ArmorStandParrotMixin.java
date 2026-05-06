package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandParrotMixin extends LivingEntity {
    protected ArmorStandParrotMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (this.level().isClientSide()) return;
        if (!GarrettMod.CONFIG.enableParrotArmorStands) return;

        ItemStack stack = player.getItemInHand(hand);
        
        if (hand == Hand.MAIN_HAND && stack.isEmpty() && player.isShiftKeyDown()) {
            CompoundTag leftShoulder = player.getShoulderEntityLeft();
            CompoundTag rightShoulder = player.getShoulderEntityRight();
            CompoundTag parrotNbt = null;

            if (!rightShoulder.isEmpty()) {
                parrotNbt = rightShoulder;
                player.setShoulderEntityRight(new CompoundTag());
            } else if (!leftShoulder.isEmpty()) {
                parrotNbt = leftShoulder;
                player.setShoulderEntityLeft(new CompoundTag());
            }

            if (parrotNbt != null) {
                EntityType.create(parrotNbt, this.level()).ifPresent(entity -> {
                    entity.setPos(this.getX(), this.getY() + 1.5, this.getZ());
                    this.level().addFreshEntity(entity);
                    entity.startRiding((ArmorStandEntity)(Object)this);
                });
                
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }
}
