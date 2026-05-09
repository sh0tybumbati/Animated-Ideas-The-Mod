package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class CanvasBlockEntity extends BlockEntity {

    public static final int UNPAINTED = -1;

    // Each int is 0x00RRGGBB, or UNPAINTED (-1) for an empty pixel.
    private final int[] pixels = new int[256];

    public CanvasBlockEntity(BlockPos pos, BlockState state) {
        super(GarrettMod.CANVAS_BLOCK_ENTITY_TYPE, pos, state);
        Arrays.fill(pixels, UNPAINTED);
    }

    public int[] getPixels() { return pixels; }

    public void setPixel(int index, int rgb) {
        if (index < 0 || index >= 256) return;
        pixels[index] = rgb;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("pixels", new IntArrayTag(pixels));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("pixels")) {
            int[] loaded = tag.getIntArray("pixels");
            int len = Math.min(loaded.length, 256);
            System.arraycopy(loaded, 0, pixels, 0, len);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
