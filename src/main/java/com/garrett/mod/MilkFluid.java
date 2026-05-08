package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class MilkFluid extends FlowingFluid {

    @Override
    public Fluid getFlowing() { return GarrettMod.MILK_FLUID_FLOWING; }

    @Override
    public Fluid getSource() { return GarrettMod.MILK_FLUID_STILL; }

    @Override
    public Item getBucket() { return Items.MILK_BUCKET; }

    @Override
    protected boolean canConvertToSource(Level level) { return false; }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {}

    @Override
    public int getSlopeFindDistance(LevelReader level) { return 4; }

    @Override
    public int getDropOff(LevelReader level) { return 1; }

    @Override
    public int getTickDelay(LevelReader level) { return 5; }

    @Override
    protected float getExplosionResistance() { return 100.0f; }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return GarrettMod.MILK_BLOCK.defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == GarrettMod.MILK_FLUID_STILL || fluid == GarrettMod.MILK_FLUID_FLOWING;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    public static class Source extends MilkFluid {
        @Override
        public int getAmount(FluidState state) { return 8; }

        @Override
        public boolean isSource(FluidState state) { return true; }
    }

    public static class Flowing extends MilkFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) { return state.getValue(LEVEL); }

        @Override
        public boolean isSource(FluidState state) { return false; }
    }
}
