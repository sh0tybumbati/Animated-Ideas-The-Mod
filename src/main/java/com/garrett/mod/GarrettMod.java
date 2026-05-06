package com.garrett.mod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GarrettMod implements ModInitializer {
	public static final String MOD_ID = "garrettmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static GarrettModConfig CONFIG;

	public static final Block GUNPOWDER_BLOCK = new GunpowderBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE).noOcclusion().noCollision());
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class GarrettMod implements ModInitializer {
// ... existing code ...
	@Override
	public void onInitialize() {
		AutoConfig.register(GarrettModConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(GarrettModConfig.class).getConfig();

		Registry.register(BuiltInRegistries.BLOCK, Identifier.of(MOD_ID, "gunpowder_block"), GUNPOWDER_BLOCK);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!CONFIG.enableRepairableAnvils) return InteractionResult.PASS;

			BlockState state = world.getBlockState(hitResult.getBlockPos());
			if (state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
				ItemStack stack = player.getItemInHand(hand);
				if (stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_BLOCK)) {
					Block repairTarget = null;
					if (state.is(Blocks.DAMAGED_ANVIL)) repairTarget = Blocks.CHIPPED_ANVIL;
					else if (state.is(Blocks.CHIPPED_ANVIL)) repairTarget = Blocks.ANVIL;
					else if (state.is(Blocks.ANVIL) && stack.is(Items.IRON_BLOCK)) return InteractionResult.PASS; // Already full

					if (repairTarget != null) {
						if (!world.isClientSide()) {
							world.setBlock(hitResult.getBlockPos(), repairTarget.defaultBlockState(), 3);
							if (!player.isCreative()) stack.shrink(1);
							world.levelEvent(1031, hitResult.getBlockPos(), 0); // Anvil sound
						}
						return InteractionResult.sidedSuccess(world.isClientSide());
					} else if (stack.is(Items.IRON_BLOCK)) { // Full repair
						if (!world.isClientSide()) {
							world.setBlock(hitResult.getBlockPos(), Blocks.ANVIL.defaultBlockState(), 3);
							if (!player.isCreative()) stack.shrink(1);
							world.levelEvent(1031, hitResult.getBlockPos(), 0);
						}
						return InteractionResult.sidedSuccess(world.isClientSide());
					}
				}
			}
			return InteractionResult.PASS;
		});
// ... existing gunpowder code ...

			if (!CONFIG.enablePlaceableGunpowder) return InteractionResult.PASS;
			
			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(Items.GUNPOWDER)) {
				BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());
				if (world.getBlockState(pos).isAir() && GUNPOWDER_BLOCK.canSurvive(GUNPOWDER_BLOCK.defaultBlockState(), world, pos)) {
					if (!world.isClientSide()) {
						world.setBlock(pos, GUNPOWDER_BLOCK.defaultBlockState(), 3);
						if (!player.isCreative()) {
							stack.shrink(1);
						}
					}
					return InteractionResult.sidedSuccess(world.isClientSide());
				}
			}
			return InteractionResult.PASS;
		});

		LOGGER.info("GarrettTheCarrotMod initialized!");
	}
}
