package com.garrett.mod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** A pumpkin the player carves pixel-by-pixel with a sword; flint &amp; steel lights it. */
public class CarvablePumpkinBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final MapCodec<CarvablePumpkinBlock> CODEC = simpleCodec(CarvablePumpkinBlock::new);

    public CarvablePumpkinBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<CarvablePumpkinBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CarvablePumpkinBlockEntity(pos, state);
    }

    /** A dropped/placeable item that preserves every face's carving and the lit state. */
    public static ItemStack createCarvedStack(BlockState state, CarvablePumpkinBlockEntity be) {
        ItemStack stack = new ItemStack(GarrettMod.CARVABLE_PUMPKIN_BLOCK);
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("carved", be.getFaces());
        tag.putBoolean("lit", state.getValue(LIT));
        // setBlockEntityData writes the block-entity id so the data serializes and re-applies on place.
        BlockItem.setBlockEntityData(stack, GarrettMod.CARVABLE_PUMPKIN_BLOCK_ENTITY, tag);
        return stack;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()
                && level.getBlockEntity(pos) instanceof CarvablePumpkinBlockEntity be) {
            Block.popResource(level, pos, createCarvedStack(state, be));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // The carve grid re-applies to the block entity automatically; restore the lit state here.
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null && data.copyTag().getBoolean("lit") && !state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    // Flint & steel toggles the jack-o'-lantern glow on/off.
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LIT, !state.getValue(LIT)), 3);
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player,
                        hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Maps a hit on the carved face to an 8x8 pixel index (y*8 + x), or -1 if off-face. */
    public static int hitToPixel(Direction facing, Vec3 hitPos, BlockPos blockPos) {
        double lx = hitPos.x - blockPos.getX();
        double ly = hitPos.y - blockPos.getY();
        double lz = hitPos.z - blockPos.getZ();
        double u, v;
        switch (facing) {
            case NORTH -> { u = 1.0 - lx; v = 1.0 - ly; }
            case SOUTH -> { u = lx;       v = 1.0 - ly; }
            case EAST  -> { u = 1.0 - lz; v = 1.0 - ly; }
            case WEST  -> { u = lz;       v = 1.0 - ly; }
            default    -> { return -1; }
        }
        int n = CarvablePumpkinBlockEntity.SIZE;
        int px = (int) Math.min(n - 1, Math.max(0, u * n));
        int py = (int) Math.min(n - 1, Math.max(0, v * n));
        return py * n + px;
    }
}
