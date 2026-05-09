package com.garrett.mod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EyedropperPayload(int colorId) implements CustomPacketPayload {

    public static final Type<EyedropperPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(GarrettMod.MOD_ID, "eyedropper"));

    public static final StreamCodec<FriendlyByteBuf, EyedropperPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_INT, EyedropperPayload::colorId, EyedropperPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
