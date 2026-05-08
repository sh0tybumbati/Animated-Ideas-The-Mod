package com.garrett.mod.client;

import com.garrett.mod.CanvasBlock;
import com.garrett.mod.GarrettMod;
import com.garrett.mod.GunpowderBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class GarrettModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(GarrettMod.GUNPOWDER_BLOCK, RenderType.cutout());

		var milkRenderHandler = new SimpleFluidRenderHandler(
			ResourceLocation.fromNamespaceAndPath("minecraft", "block/white_concrete"),
			ResourceLocation.fromNamespaceAndPath("minecraft", "block/white_concrete"),
			0xFFFFFF
		);
		FluidRenderHandlerRegistry.INSTANCE.register(GarrettMod.MILK_FLUID_STILL, milkRenderHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(GarrettMod.MILK_FLUID_FLOWING, milkRenderHandler);

		ColorProviderRegistry.BLOCK.register(
			(state, world, pos, tintIndex) -> state.getValue(GunpowderBlock.LIT) ? 0xFFAA00 : 0x777777,
			GarrettMod.GUNPOWDER_BLOCK
		);

		for (DyeColor color : DyeColor.values()) {
			BlockRenderLayerMap.INSTANCE.putBlock(GarrettMod.CANVAS_BLOCKS.get(color), RenderType.cutout());
		}
		BlockRenderLayerMap.INSTANCE.putBlock(GarrettMod.TRANSPARENT_CANVAS, RenderType.translucent());

		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			if (state.getBlock() instanceof CanvasBlock canvas && canvas.color != null) {
				return canvas.color.getTextureDiffuseColor() | 0xFF000000;
			}
			return 0xFFFFFFFF;
		}, GarrettMod.CANVAS_BLOCKS.values().toArray(new CanvasBlock[0]));

		BlockEntityRenderers.register(GarrettMod.CANVAS_BLOCK_ENTITY_TYPE, CanvasBlockEntityRenderer::new);
	}
}
