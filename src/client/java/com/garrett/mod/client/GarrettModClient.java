package com.garrett.mod.client;

import com.garrett.mod.GarrettMod;
import com.garrett.mod.GunpowderBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GarrettModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(GarrettMod.GUNPOWDER_BLOCK, RenderType.cutout());

		var milkRenderHandler = new SimpleFluidRenderHandler(
			ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still"),
			ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow"),
			0xFFFFFF
		);
		FluidRenderHandlerRegistry.INSTANCE.register(GarrettMod.MILK_FLUID_STILL, milkRenderHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(GarrettMod.MILK_FLUID_FLOWING, milkRenderHandler);

		ColorProviderRegistry.BLOCK.register(
			(state, world, pos, tintIndex) -> state.getValue(GunpowderBlock.LIT) ? 0xFFAA00 : 0x777777,
			GarrettMod.GUNPOWDER_BLOCK
		);
	}
}
