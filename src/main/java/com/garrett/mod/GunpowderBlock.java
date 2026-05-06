package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GunpowderBlock extends Block {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public GunpowderBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.is(state.getBlock())) return;
        this.checkIgnition(world, pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.checkIgnition(world, pos, state);
    }

    private void checkIgnition(Level world, BlockPos pos, BlockState state) {
        if (!state.getValue(LIT) && world.hasNeighborSignal(pos)) {
            world.setBlock(pos, state.setValue(LIT, true), 3);
            world.scheduleTick(pos, this, 2);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            // Spread to neighbors
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = world.getBlockState(neighborPos);
                
                if (neighborState.is(this) && !neighborState.getValue(LIT)) {
                    world.setBlock(neighborPos, neighborState.setValue(LIT, true), 3);
                    world.scheduleTick(neighborPos, this, 2);
                } else if (neighborState.is(Blocks.TNT)) {
                    world.explode(null, (double)neighborPos.getX() + 0.5, (double)neighborPos.getY() + 0.5, (double)neighborPos.getZ() + 0.5, 4.0F, Level.ExplosionSourceType.TNT);
                    world.removeBlock(neighborPos, false);
                }
            }
            // Disappear after burning
            world.removeBlock(pos, false);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, PlayerEntity player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.FLINT_AND_STEEL) || itemStack.is(Items.FIRE_CHARGE)) {
            if (!state.getValue(LIT)) {
                world.setBlock(pos, state.setValue(LIT, true), 3);
                world.scheduleTick(pos, this, 2);
                if (!player.isCreative()) {
                    if (itemStack.is(Items.FLINT_AND_STEEL)) {
                        itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                    } else {
                        itemStack.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide());
            }
        }
        return super.use(state, world, pos, player, hand, hit);
    }
}
