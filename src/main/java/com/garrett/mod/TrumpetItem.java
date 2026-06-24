package com.garrett.mod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A brass trumpet: right-click plays a note chosen by where you're looking (down/level/up). */
public class TrumpetItem extends Item {
    public TrumpetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        float pitch = player.getXRot();
        SoundEvent note = pitch > 25.0f ? GarrettMod.TRUMPET_LOW
                        : pitch < -25.0f ? GarrettMod.TRUMPET_HIGH
                        : GarrettMod.TRUMPET_MID;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            note, SoundSource.RECORDS, 2.0f, 1.0f);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
