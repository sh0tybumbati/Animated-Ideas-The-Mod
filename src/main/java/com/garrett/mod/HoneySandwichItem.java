package com.garrett.mod;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A honey sandwich clears poison when eaten, mirroring the honey bottle. */
public class HoneySandwichItem extends Item {
    public HoneySandwichItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.removeEffect(MobEffects.POISON);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
