package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stores one {@value #SIZE}x{@value #SIZE} carve grid per face. Each face needs 256 bits, held as
 * {@value #LONGS_PER_FACE} longs; the flat array is laid out per {@code face.get3DDataValue()}.
 * Only the four horizontal faces are carved.
 */
public class CarvablePumpkinBlockEntity extends BlockEntity {
    public static final int SIZE = 16;
    public static final int BITS = SIZE * SIZE;            // 256
    public static final int LONGS_PER_FACE = BITS / 64;    // 4

    private final long[] faces = new long[6 * LONGS_PER_FACE];

    public CarvablePumpkinBlockEntity(BlockPos pos, BlockState state) {
        super(GarrettMod.CARVABLE_PUMPKIN_BLOCK_ENTITY, pos, state);
    }

    public boolean isCarved(Direction face, int pixel) {
        if (pixel < 0 || pixel >= BITS) return false;
        int i = face.get3DDataValue() * LONGS_PER_FACE + (pixel >> 6);
        return (faces[i] & (1L << (pixel & 63))) != 0;
    }

    public boolean faceEmpty(Direction face) {
        int base = face.get3DDataValue() * LONGS_PER_FACE;
        for (int k = 0; k < LONGS_PER_FACE; k++) if (faces[base + k] != 0) return false;
        return true;
    }

    public long[] getFaces() {
        return faces;
    }

    /** Carving is one-way: a carved pixel stays carved. */
    public void carve(Direction face, int pixel) {
        if (pixel < 0 || pixel >= BITS) return;
        int i = face.get3DDataValue() * LONGS_PER_FACE + (pixel >> 6);
        long mask = 1L << (pixel & 63);
        if ((faces[i] & mask) != 0) return;
        faces[i] |= mask;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLongArray("carved", faces);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        long[] loaded = tag.getLongArray("carved");
        if (loaded.length == faces.length) {
            System.arraycopy(loaded, 0, faces, 0, faces.length);
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
