package com.garrett.mod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DoodleBookItem extends Item {
    public static final int GRID_SIZE = 16;
    public static final String NBT_PIXELS = "gtcai.doodle_pixels";

    public DoodleBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // The actual screen opening is handled on the client side via a Mixin or UseItemCallback
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    /** Returns the 256-byte pixel array from the item NBT, or a blank array if not present. */
    public static byte[] getPixels(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains(NBT_PIXELS)) {
            return tag.getByteArray(NBT_PIXELS);
        }
        return new byte[GRID_SIZE * GRID_SIZE];
    }

    /** Saves the 256-byte pixel array to the item NBT. */
    public static void setPixels(ItemStack stack, byte[] pixels) {
        stack.getOrCreateTag().putByteArray(NBT_PIXELS, pixels);
    }
}
