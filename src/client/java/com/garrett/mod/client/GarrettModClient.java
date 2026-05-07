package com.garrett.mod.client;

import com.garrett.mod.GarrettMod;
import com.garrett.mod.GunpowderBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class GarrettModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ColorProviderRegistry.BLOCK.register(
			(state, world, pos, tintIndex) -> state.getValue(GunpowderBlock.LIT) ? 0xFFAA00 : 0x777777,
			GarrettMod.GUNPOWDER_BLOCK
		);
	}
}
