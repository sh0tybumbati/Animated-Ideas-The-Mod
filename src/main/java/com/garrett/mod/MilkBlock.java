package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MilkBlock extends Block {

    public MilkBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide() && entity instanceof Player player && !player.getActiveEffects().isEmpty()) {
            player.removeAllEffects();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.BUCKET)) {
            if (!world.isClientSide()) {
                world.removeBlock(pos, false);
                if (!player.isCreative()) {
                    stack.shrink(1);
                    ItemStack milkBucket = new ItemStack(Items.MILK_BUCKET);
                    if (stack.isEmpty()) {
                        player.setItemInHand(hand, milkBucket);
                    } else if (!player.getInventory().add(milkBucket)) {
                        player.drop(milkBucket, false);
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }
}
