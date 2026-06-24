package com.garrett.mod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * The frying pan as a melee weapon: a heavy, slow bonk. Its attack damage/speed live on the item's
 * attribute modifiers (set at registration); this adds a metallic clang and an extra dollop of
 * knockback on every hit.
 */
public class FryingPanItem extends BlockItem {
    public FryingPanItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.7f, 1.6f);
        target.knockback(0.6, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
    }
}
