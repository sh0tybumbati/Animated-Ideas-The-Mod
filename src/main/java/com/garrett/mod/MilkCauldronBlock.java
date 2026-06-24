package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** A cauldron filled with milk; after a short curdle it sets into a cheese block. */
public class MilkCauldronBlock extends Block {
    private static final int CURDLE_TICKS = 200; // ~10 seconds

    public MilkCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, CURDLE_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Milk has set: empty the cauldron and yield a cheese block.
        level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        Block.popResource(level, pos.above(), new ItemStack(GarrettMod.CHEESE_BLOCK));
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8f, 0.8f);
    }
}
