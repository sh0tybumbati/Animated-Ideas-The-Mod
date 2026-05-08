package com.garrett.mod.client;

import com.garrett.mod.GarrettMod;
import com.garrett.mod.GunpowderBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.RenderType;

public class GarrettModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(GarrettMod.GUNPOWDER_BLOCK, RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(GarrettMod.MILK_BLOCK, RenderType.translucent());
		ColorProviderRegistry.BLOCK.register(
			(state, world, pos, tintIndex) -> state.getValue(GunpowderBlock.LIT) ? 0xFFAA00 : 0x777777,
			GarrettMod.GUNPOWDER_BLOCK
		);
	}
}
