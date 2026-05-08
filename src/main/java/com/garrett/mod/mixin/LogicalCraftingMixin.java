package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class LogicalCraftingMixin {

    @Inject(method = "onTake", at = @At("RETURN"))
    private void onCraft(Player player, ItemStack stack, CallbackInfo ci) {
        if (!(stack.getItem() instanceof BlockItem bi)) return;

        int bonus = 0;
        if (GarrettMod.CONFIG.enableLogicalStairs && bi.getBlock() instanceof StairBlock) {
            bonus = 4; // vanilla gives 4, logical gives 8
        } else if (GarrettMod.CONFIG.enableLogicalTrapdoors && bi.getBlock() instanceof TrapDoorBlock) {
            bonus = 10; // vanilla gives 2, logical gives 12
        }

        if (bonus > 0) {
            ItemStack bonusStack = new ItemStack(stack.getItem(), bonus);
            if (!player.getInventory().add(bonusStack)) {
                player.drop(bonusStack, false);
            }
        }
    }
}
