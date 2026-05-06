package com.garrett.mod.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import java.util.function.Supplier;

public class MilkFluidBlock extends LiquidBlock {
    public MilkFluidBlock(Supplier<? extends FlowableFluid> fluid, Properties properties) {
        super(fluid.get(), properties);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.removeAllEffects();
        }
        super.entityInside(state, world, pos, entity);
    }
}
