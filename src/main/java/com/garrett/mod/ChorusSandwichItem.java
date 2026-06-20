package com.garrett.mod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A chorus fruit sandwich randomly teleports the eater, mirroring the chorus fruit. */
public class ChorusSandwichItem extends Item {
    public ChorusSandwichItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            double originX = entity.getX();
            double originY = entity.getY();
            double originZ = entity.getZ();

            for (int attempt = 0; attempt < 16; attempt++) {
                double tx = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                double ty = entity.getY() + (entity.getRandom().nextInt(16) - 8);
                double tz = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                if (entity.isPassenger()) {
                    entity.stopRiding();
                }
                if (entity.randomTeleport(tx, ty, tz, true)) {
                    level.playSound(null, originX, originY, originZ,
                        SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    entity.resetFallDistance();
                    break;
                }
            }

            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(this, 20);
            }
        }
        return result;
    }
}
