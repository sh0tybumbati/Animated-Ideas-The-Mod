package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A bed-like block you can sleep in to pass the night WITHOUT resetting your spawn point
 * (the spawn-set is cancelled via EntitySleepEvents.ALLOW_SETTING_SPAWN). Rendered as a flat
 * model; right-clicking it with a banner stamps that banner's design onto the bedroll (stored
 * on the head half in {@link SleepingBagBlockEntity} and drawn by its renderer).
 */
public class SleepingBagBlock extends BedBlock {
    // Flat 3px-tall slab matching the model, instead of the tall vanilla bed shape.
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 3, 16);

    public SleepingBagBlock(DyeColor color, Properties properties) {
        super(color, properties);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SleepingBagBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof BannerItem banner) {
            // The design lives on the head half and spans the whole bedroll.
            BlockPos headPos = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(BedBlock.getConnectedDirection(state));
            if (!level.isClientSide() && level.getBlockEntity(headPos) instanceof SleepingBagBlockEntity bag) {
                BannerPatternLayers layers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
                bag.setBanner(banner.getColor(), layers);
                if (!player.getAbilities().instabuild) stack.consume(1, player);
                level.playSound(null, headPos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
