package com.garrett.mod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

    /** Returns the 256-byte pixel array from the item's custom data, or a blank array if not present. */
    public static byte[] getPixels(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(NBT_PIXELS)) {
                return tag.getByteArray(NBT_PIXELS);
            }
        }
        return new byte[GRID_SIZE * GRID_SIZE];
    }

    /** Saves the 256-byte pixel array to the item's custom data. */
    public static void setPixels(ItemStack stack, byte[] pixels) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putByteArray(NBT_PIXELS, pixels));
    }
}
