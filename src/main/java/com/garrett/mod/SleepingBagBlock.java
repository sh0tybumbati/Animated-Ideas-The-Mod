package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A bed-like block you can sleep in to pass the night WITHOUT resetting your spawn point
 * (the spawn-set is cancelled via EntitySleepEvents.ALLOW_SETTING_SPAWN). Rendered as a flat
 * model rather than the animated bed entity, so it needs no block-entity renderer.
 */
public class SleepingBagBlock extends BedBlock {
    public SleepingBagBlock(DyeColor color, Properties properties) {
        super(color, properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
