package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds an optional banner design applied to a sleeping bag (right-click the bag with a banner).
 * Only the head half stores/renders the design; it spans the whole bedroll. Empty by default, so a
 * plain sleeping bag costs nothing extra.
 */
public class SleepingBagBlockEntity extends BlockEntity {
    private boolean hasBanner = false;
    private DyeColor baseColor = DyeColor.WHITE;
    private BannerPatternLayers patterns = BannerPatternLayers.EMPTY;

    public SleepingBagBlockEntity(BlockPos pos, BlockState state) {
        super(GarrettMod.SLEEPING_BAG_BLOCK_ENTITY, pos, state);
    }

    public boolean hasBanner() { return hasBanner; }
    public DyeColor getBaseColor() { return baseColor; }
    public BannerPatternLayers getPatterns() { return patterns; }

    public void setBanner(DyeColor baseColor, BannerPatternLayers patterns) {
        this.hasBanner = true;
        this.baseColor = baseColor;
        this.patterns = patterns;
        markUpdated();
    }

    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!hasBanner) return;
        tag.putBoolean("HasBanner", true);
        tag.putString("BaseColor", baseColor.getName());
        BannerPatternLayers.CODEC
            .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), patterns)
            .result().ifPresent(t -> tag.put("Patterns", t));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hasBanner = tag.getBoolean("HasBanner");
        if (!hasBanner) {
            baseColor = DyeColor.WHITE;
            patterns = BannerPatternLayers.EMPTY;
            return;
        }
        baseColor = DyeColor.byName(tag.getString("BaseColor"), DyeColor.WHITE);
        Tag p = tag.get("Patterns");
        patterns = p == null ? BannerPatternLayers.EMPTY
            : BannerPatternLayers.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), p)
                .result().orElse(BannerPatternLayers.EMPTY);
    }

    // Carry the design on the dropped item (see the loot table's copy_components) and restore it on placement.
    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        BannerPatternLayers p = input.get(DataComponents.BANNER_PATTERNS);
        DyeColor base = input.get(DataComponents.BASE_COLOR);
        if (base != null || (p != null && !p.layers().isEmpty())) {
            this.baseColor = base == null ? DyeColor.WHITE : base;
            this.patterns = p == null ? BannerPatternLayers.EMPTY : p;
            this.hasBanner = true;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (hasBanner) {
            builder.set(DataComponents.BANNER_PATTERNS, patterns);
            builder.set(DataComponents.BASE_COLOR, baseColor);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("HasBanner");
        tag.remove("BaseColor");
        tag.remove("Patterns");
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
