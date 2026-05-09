package com.garrett.mod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class CanvasBlockItem extends BlockItem {

    public CanvasBlockItem(CanvasBlock block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack createFilledStack(CanvasBlock block, CanvasBlockEntity canvas) {
        ItemStack stack = new ItemStack(block);
        CompoundTag tag = new CompoundTag();
        tag.put("pixels", new IntArrayTag(canvas.getPixels()));
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        return stack;
    }
}
