package com.garrett.mod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent C2S when the player saves a doodle. Contains the 16x16 pixel grid
 * encoded as 256 bytes (one byte per pixel, index into DyeColor values, 0xFF = empty).
 */
public record DoodleBookSavePayload(byte[] pixels) implements CustomPacketPayload {
    public static final Type<DoodleBookSavePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(GarrettMod.MOD_ID, "doodle_save"));

    public static final StreamCodec<FriendlyByteBuf, DoodleBookSavePayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.pixels().length);
                buf.writeBytes(payload.pixels());
            },
            buf -> {
                int len = buf.readVarInt();
                byte[] pixels = new byte[len];
                buf.readBytes(pixels);
                return new DoodleBookSavePayload(pixels);
            }
        );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
