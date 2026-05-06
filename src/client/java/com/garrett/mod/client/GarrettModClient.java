package com.garrett.mod.client;

import com.garrett.mod.GarrettMod;
import com.garrett.mod.GunpowderBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.world.level.block.state.BlockState;

public class GarrettModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            return state.getValue(GunpowderBlock.LIT) ? 0xFFFFAA00 : 0xFF777777; // Orange if lit, Gray if not
        }, GarrettMod.GUNPOWDER_BLOCK);
    }
}
