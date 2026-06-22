package com.garrett.mod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent C2S when the player saves a doodle page. Carries the target page index plus the
 * {@value #SIZE}-byte 64x64 grid (one byte per pixel: 0 = empty, 1-16 = DyeColor id + 1).
 */
public record DoodleBookSavePayload(int page, byte[] pixels) implements CustomPacketPayload {
    public static final Type<DoodleBookSavePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(GarrettMod.MOD_ID, "doodle_save"));

    public static final int SIZE = DoodleBookItem.PAGE_BYTES; // 64x64 = 4096

    // Fixed-size pixel payload so a client can't request a huge allocation; page index is clamped.
    public static final StreamCodec<FriendlyByteBuf, DoodleBookSavePayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.page());
                byte[] p = payload.pixels();
                for (int i = 0; i < SIZE; i++) buf.writeByte(i < p.length ? p[i] : 0);
            },
            buf -> {
                int page = buf.readVarInt();
                byte[] pixels = new byte[SIZE];
                buf.readBytes(pixels);
                return new DoodleBookSavePayload(page, pixels);
            }
        );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
