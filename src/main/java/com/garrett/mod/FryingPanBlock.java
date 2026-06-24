package com.garrett.mod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

/**
 * A frying pan: a flat, two-slot cooker that floats on top of a campfire. It is only placeable on a
 * campfire and cooks (at 2x campfire speed) only while that campfire is lit. Right-click a campfire-cookable
 * food onto it to start; finished food pops off above. Cooking logic lives in {@link FryingPanBlockEntity}.
 */
public class FryingPanBlock extends BaseEntityBlock {
    public static final MapCodec<FryingPanBlock> CODEC = simpleCodec(FryingPanBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Thin 10x10 body sitting low in the block. The visible handle has no collision box of its own.
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 2, 13);

    public FryingPanBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<FryingPanBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    /** Only valid floating directly on top of a campfire. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() instanceof CampfireBlock;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
            : super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FryingPanBlockEntity pan) {
            ItemStack held = player.getItemInHand(hand);
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe = pan.getCookableRecipe(held);
            if (recipe.isPresent()) {
                if (!level.isClientSide()) {
                    // Halve the recipe's cook time so the pan finishes in roughly half the campfire time.
                    int cookTime = Math.max(1, recipe.get().value().getCookingTime() / 2);
                    if (pan.placeFood(player, held, cookTime)) {
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FryingPanBlockEntity pan) {
                Containers.dropContents(level, pos, pan.getItems());
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FryingPanBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, GarrettMod.FRYING_PAN_BLOCK_ENTITY, FryingPanBlockEntity::cookTick);
    }
}
