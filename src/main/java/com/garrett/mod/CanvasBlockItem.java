package com.garrett.mod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class CanvasBlockItem extends BlockItem {

    public CanvasBlockItem(CanvasBlock block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack createFilledStack(CanvasBlock block, CanvasBlockEntity canvas) {
        ItemStack stack = new ItemStack(block);
        CompoundTag tag = new CompoundTag();
        tag.put("pixels", new IntArrayTag(canvas.getPixels()));
        // Use the vanilla helper so the block-entity "id" is written into the data.
        // Setting BLOCK_ENTITY_DATA directly without an "id" crashes on inventory save
        // ("Missing id for entity") and would not restore the painting on placement.
        BlockItem.setBlockEntityData(stack, GarrettMod.CANVAS_BLOCK_ENTITY_TYPE, tag);
        return stack;
    }
}
