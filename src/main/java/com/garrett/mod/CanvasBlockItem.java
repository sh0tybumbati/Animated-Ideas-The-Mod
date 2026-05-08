package com.garrett.mod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class CanvasBlockItem extends BlockItem {

    public CanvasBlockItem(CanvasBlock block, Properties properties) {
        super(block, properties);
    }

    // BlockItem.updateCustomBlockEntityTag already reads DataComponents.BLOCK_ENTITY_DATA
    // and applies it to the placed block entity — no override needed.

    public static ItemStack createFilledStack(CanvasBlock block, CanvasBlockEntity canvas) {
        ItemStack stack = new ItemStack(block);
        CompoundTag tag = new CompoundTag();
        tag.put("pixels", new ByteArrayTag(canvas.getPixels()));
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        return stack;
    }
}
