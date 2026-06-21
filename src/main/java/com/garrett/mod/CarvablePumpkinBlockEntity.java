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
 * Stores one 8x8 carve grid per face: {@code faces[dir.get3DDataValue()]} is a 64-bit mask where
 * bit (y*8 + x) set means that pixel is carved out. Only the four horizontal faces are carved.
 */
public class CarvablePumpkinBlockEntity extends BlockEntity {
    public static final int SIZE = 8;

    private final long[] faces = new long[6];

    public CarvablePumpkinBlockEntity(BlockPos pos, BlockState state) {
        super(GarrettMod.CARVABLE_PUMPKIN_BLOCK_ENTITY, pos, state);
    }

    public long getCarved(Direction face) {
        return faces[face.get3DDataValue()];
    }

    public long[] getFaces() {
        return faces;
    }

    public void setFaces(long[] src) {
        if (src.length == faces.length) System.arraycopy(src, 0, faces, 0, faces.length);
    }

    /** Carving is one-way: a carved pixel stays carved. */
    public void carve(Direction face, int index) {
        if (index < 0 || index >= SIZE * SIZE) return;
        int i = face.get3DDataValue();
        long updated = faces[i] | (1L << index);
        if (updated == faces[i]) return;
        faces[i] = updated;
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
