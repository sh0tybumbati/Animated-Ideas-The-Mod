package com.garrett.mod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** A food-only furnace variant. Behaviour lives in {@link AirFryerBlockEntity}. */
public class AirFryerBlock extends AbstractFurnaceBlock {
    public static final MapCodec<AirFryerBlock> CODEC = simpleCodec(AirFryerBlock::new);

    public AirFryerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<AirFryerBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AirFryerBlockEntity(pos, state);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof AirFryerBlockEntity fryer) {
            player.openMenu(fryer);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, GarrettMod.AIR_FRYER_BLOCK_ENTITY);
    }
}
