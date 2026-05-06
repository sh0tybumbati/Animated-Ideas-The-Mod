package com.garrett.mod.item;

import net.minecraft.item.FoodProperties;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

public class SandwichItem extends Item {
    private final Item baseBread;
    private final Item filling;

    public SandwichItem(Item baseBread, Item filling, Properties properties) {
        super(properties);
        this.baseBread = baseBread;
        this.filling = filling;
    }

    @Override
    public FoodProperties getFoodProperties() {
        FoodProperties breadProps = baseBread.getFoodProperties();
        FoodProperties fillingProps = filling.getFoodProperties();

        int hunger = 6; // Default hunger for bread
        float saturation = 0.8f; // Default saturation for bread

        if (breadProps != null) {
            hunger = breadProps.getNutrition();
            saturation = breadProps.getSaturationModifier();
        }

        if (fillingProps != null) {
            hunger += fillingProps.getNutrition();
            saturation += fillingProps.getSaturationModifier();
        }

        // Ensure hunger/saturation don't exceed max values if desired
        // hunger = Math.min(hunger, 20);
        // saturation = Math.min(saturation, 5.0f);

        FoodProperties.Builder builder = new FoodProperties.Builder()
            .hunger(hunger)
            .saturationModifier(saturation);

        // Add effects if applicable (simplified for now, can be made more complex)
        if (filling == Items.ROTTEN_FLESH) {
            builder.effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.8f);
        }
        if (filling == Items.POISONOUS_POTATO) {
            builder.effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 0.6f);
        }
        // Add more filling-specific effects here

        return builder.build();
    }
}
