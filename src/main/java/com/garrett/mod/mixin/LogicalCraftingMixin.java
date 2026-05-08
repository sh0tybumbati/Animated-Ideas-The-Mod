package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class LogicalCraftingMixin {

    @Shadow private CraftingContainer craftSlots;

    private ItemStack pendingStairsRefund;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void captureIngredients(Player player, ItemStack stack, CallbackInfo ci) {
        pendingStairsRefund = null;
        if (!GarrettMod.CONFIG.enableLogicalStairs) return;
        if (!(stack.getItem() instanceof BlockItem bi)) return;
        if (!bi.getBlock().defaultBlockState().is(BlockTags.STAIRS)) return;

        // Capture the ingredient type before the crafting grid is cleared
        for (int i = 0; i < craftSlots.getContainerSize(); i++) {
            ItemStack ingredient = craftSlots.getItem(i);
            if (!ingredient.isEmpty()) {
                pendingStairsRefund = new ItemStack(ingredient.getItem(), 3);
                break;
            }
        }
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void onCraft(Player player, ItemStack stack, CallbackInfo ci) {
        // Refund 3 planks so stairs cost 3 planks net (works for all modded stairs via tag)
        if (pendingStairsRefund != null) {
            if (!player.getInventory().add(pendingStairsRefund)) {
                player.drop(pendingStairsRefund, false);
            }
            pendingStairsRefund = null;
        }

        // Trapdoors: boost to 12 total (works for all modded trapdoors via tag)
        if (!GarrettMod.CONFIG.enableLogicalTrapdoors) return;
        if (!(stack.getItem() instanceof BlockItem bi)) return;
        if (!bi.getBlock().defaultBlockState().is(BlockTags.TRAPDOORS)) return;

        ItemStack bonus = new ItemStack(stack.getItem(), 10);
        if (!player.getInventory().add(bonus)) player.drop(bonus, false);
    }
}
