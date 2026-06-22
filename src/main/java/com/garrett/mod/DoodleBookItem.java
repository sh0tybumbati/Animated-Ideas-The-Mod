package com.garrett.mod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

/**
 * A multi-page sketchbook. Each page is a {@value #GRID}x{@value #GRID} grid stored as one byte per
 * pixel (0 = empty, 1-16 = DyeColor id + 1). Pages live in the item's custom data as a list of byte
 * arrays. The drawing UI is {@code DoodleBookScreen}; edits are sent back via {@code DoodleBookSavePayload}.
 */
public class DoodleBookItem extends Item {
    public static final int GRID = 64;
    public static final int PAGE_BYTES = GRID * GRID; // 4096
    public static final int MAX_PAGES = 16;

    private static final String KEY = "doodle_pages";

    public DoodleBookItem(Properties properties) {
        super(properties);
    }

    /** All pages; always at least one (a blank page) so the book is never empty. */
    public static List<byte[]> getPages(ItemStack stack) {
        List<byte[]> pages = new ArrayList<>();
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(KEY, Tag.TAG_LIST)) {
                ListTag list = tag.getList(KEY, Tag.TAG_BYTE_ARRAY);
                for (int i = 0; i < list.size() && pages.size() < MAX_PAGES; i++) {
                    byte[] raw = ((ByteArrayTag) list.get(i)).getAsByteArray();
                    byte[] page = new byte[PAGE_BYTES];
                    System.arraycopy(raw, 0, page, 0, Math.min(raw.length, PAGE_BYTES));
                    pages.add(page);
                }
            }
        }
        if (pages.isEmpty()) pages.add(new byte[PAGE_BYTES]);
        return pages;
    }

    /** Writes one page; appends a new page when index == current count (capped at {@link #MAX_PAGES}). */
    public static void setPage(ItemStack stack, int index, byte[] page) {
        if (index < 0 || index >= MAX_PAGES || page.length != PAGE_BYTES) return;
        List<byte[]> pages = getPages(stack);
        if (index < pages.size()) {
            pages.set(index, page);
        } else if (index == pages.size()) {
            pages.add(page);
        } else {
            return;
        }
        ListTag list = new ListTag();
        for (byte[] p : pages) list.add(new ByteArrayTag(p));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(KEY, list));
    }
}
