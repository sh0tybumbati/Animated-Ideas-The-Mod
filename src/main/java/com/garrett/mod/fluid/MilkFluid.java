package com.garrett.mod.fluid;

import com.garrett.mod.GarrettMod;
import com.garrett.mod.GunpowderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MilkFluid extends FlowingFluid {

    public static final MilkFluid STILL_MILK = new Still(null);
    public static final MilkFluid FLOWING_MILK = new Flowing(null);
    public static final Block MILK_BLOCK = new MilkFluidBlock(STILL_MILK, Block.Properties.copy(Blocks.WATER).mapColor(MapColor.WHITE).replaceable());
    public static final Item MILK_BUCKET_ITEM = new BucketItem(STILL_MILK, new Item.Settings().craftRemainder(Items.BUCKET).maxStackSize(1));

    private MilkFluid() {}

    @Override
    public FluidState getDefaultState() {
        return STILL_MILK.getFlowing(3, true);
    }

    @Override
    public Identifier getStill() {
        return Identifier.of(GarrettMod.MOD_ID, "fluid/milk_still");
    }

    @Override
    public Identifier getFlowing() {
        return Identifier.of(GarrettMod.MOD_ID, "fluid/milk_flowing");
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return MILK_BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getBlockStateLevel(state));
    }

    @Override
    public Item getBucketItem() {
        return MILK_BUCKET_ITEM;
    }

    @Override
    protected boolean isInfinite() {
        return true; // Milk source is infinite
    }

    @Override
    protected void beforeBreakingBlock(LevelAccessor world, BlockPos pos, BlockState state) {
        // Logic for when the fluid block is broken (if needed)
    }

    @Override
    protected int getFlowSpeed() {
        return 4; // Slower flow than water
    }

    @Override
    protected int getLevelDecreaseId() {
        return 1; // How much level decreases per tick
    }

    @Override
    protected int getTickDelay() {
        return 1; // Tick delay for fluid updates
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F; // Milk shouldn't explode things
    }

    @Override
    protected BlockState getFluidState(FluidState state) {
        return MILK_BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getBlockStateLevel(state));
    }

    public static void registerFluids() {
        Registry.register(BuiltInRegistries.FLUID, Identifier.of(GarrettMod.MOD_ID, "milk"), STILL_MILK);
        Registry.register(BuiltInRegistries.FLUID, Identifier.of(GarrettMod.MOD_ID, "milk_flowing"), FLOWING_MILK);
        Registry.register(BuiltInRegistries.BLOCK, Identifier.of(GarrettMod.MOD_ID, "milk"), MILK_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, Identifier.of(GarrettMod.MOD_ID, "milk_bucket"), MILK_BUCKET_ITEM);
    }

    public static class Still extends MilkFluid {
        private final Supplier<FlowingFluid> flowing;

        public Still(Supplier<FlowingFluid> flowing) {
            this.flowing = flowing != null ? flowing : () -> FLOWING_MILK;
        }

        @Override
        protected FlowingFluid getFlowing() {
            return flowing.get();
        }

        @Override
        protected void fillState(FluidState state, Level world, BlockPos pos) {
            super.fillState(state, world, pos);
        }
    }

    public static class Flowing extends MilkFluid {
        private final Supplier<FlowingFluid> still;

        public Flowing(Supplier<FlowingFluid> still) {
            this.still = still != null ? still : () -> STILL_MILK;
        }

        @Override
        protected FlowingFluid getFlowing() {
            return this;
        }

        @Override
        protected void fillState(FluidState state, Level world, BlockPos pos) {
            super.fillState(state, world, pos);
        }
    }
}
