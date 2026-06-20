package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cooks food only (uses {@link RecipeType#SMOKING} recipes). Cook speed is sped up
 * to 50 ticks by {@code AirFryerCookSpeedMixin}; here we make it burn fuel twice as
 * fast as a furnace. Reuses the vanilla smoker menu/screen.
 */
public class AirFryerBlockEntity extends AbstractFurnaceBlockEntity {
    public AirFryerBlockEntity(BlockPos pos, BlockState state) {
        super(GarrettMod.AIR_FRYER_BLOCK_ENTITY, pos, state, RecipeType.SMOKING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.gtcai.air_fryer");
    }

    @Override
    protected int getBurnDuration(ItemStack fuel) {
        // Fuel burns per tick, and the air fryer cooks at 50 ticks/item (4x a furnace),
        // so throughput per tick is 4x. Quartering the furnace's per-item fuel budget
        // (1600/8 = 200 ticks for coal) yields 4 items per coal: half a furnace's 8,
        // i.e. fuel-hungry as intended.
        return Math.max(1, super.getBurnDuration(fuel) / 8);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SmokerMenu(id, inventory, this, this.dataAccess);
    }
}
