package com.garrett.mod.client;

import com.garrett.mod.CanvasBlock;
import com.garrett.mod.DoodleBookItem;
import com.garrett.mod.GarrettMod;
import com.garrett.mod.GunpowderBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

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
			(state, world, pos, tintIndex) -> state.getValue(GunpowderBlock.LIT) ? 0xFFAA00 : 0x4A4A4A,
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

		// Thrown milk potion renders as its item; without this the entity has no
		// renderer and crashes the client when thrown. Only registered when the
		// entity type exists (it is gated by enableMilkSplashPotion).
		if (GarrettMod.THROWN_MILK_POTION_ENTITY_TYPE != null) {
			EntityRendererRegistry.register(GarrettMod.THROWN_MILK_POTION_ENTITY_TYPE, ThrownItemRenderer::new);
		}

		// Milk splash potion uses the vanilla splash-potion model with a white liquid tint.
		if (GarrettMod.CONFIG.enableMilkSplashPotion) {
			ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0xFFFFFFFF, GarrettMod.MILK_SPLASH_POTION);
		}

		if (GarrettMod.CARVABLE_PUMPKIN_BLOCK_ENTITY != null) {
			BlockEntityRenderers.register(GarrettMod.CARVABLE_PUMPKIN_BLOCK_ENTITY, CarvablePumpkinBlockEntityRenderer::new);
		}

		// Right-click a doodle book to open the drawing screen (client-side only).
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (GarrettMod.CONFIG.enableDoodleBooks && world.isClientSide() && stack.getItem() instanceof DoodleBookItem) {
				Minecraft.getInstance().setScreen(new DoodleBookScreen(hand, stack));
				return InteractionResultHolder.success(stack);
			}
			return InteractionResultHolder.pass(stack);
		});
	}
}
