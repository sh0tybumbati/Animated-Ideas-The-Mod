package com.garrett.mod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GarrettMod implements ModInitializer {
	public static final String MOD_ID = "garrettmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static GarrettModConfig CONFIG;
	public static final Map<UUID, BlockPos> playerMiningPos = new HashMap<>();

	public static final Block GUNPOWDER_BLOCK = new GunpowderBlock(
		BlockBehaviour.Properties.of().noOcclusion().instabreak()
	);

	public static final Item SANDWICH = new Item(new Item.Properties()
		.food(new FoodProperties.Builder().nutrition(10).saturationModifier(0.8f).build())
	);

	public static final FlowingFluid MILK_FLUID_STILL = new MilkFluid.Source();
	public static final FlowingFluid MILK_FLUID_FLOWING = new MilkFluid.Flowing();
	public static final Block MILK_BLOCK = new MilkLiquidBlock(
		MILK_FLUID_STILL,
		BlockBehaviour.Properties.of().noCollision().strength(100.0f).liquid()
			.pushReaction(PushReaction.DESTROY).replaceable().noLootTable()
	);

	public static final Map<DyeColor, CanvasBlock> CANVAS_BLOCKS = new EnumMap<>(DyeColor.class);
	public static BlockEntityType<CanvasBlockEntity> CANVAS_BLOCK_ENTITY_TYPE;

	static {
		for (DyeColor color : DyeColor.values()) {
			CANVAS_BLOCKS.put(color, new CanvasBlock(color,
				BlockBehaviour.Properties.of().noOcclusion().strength(0.5f).noLootTable()
			));
		}
	}

	@Override
	public void onInitialize() {
		AutoConfig.register(GarrettModConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(GarrettModConfig.class).getConfig();

		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "gunpowder_block"), GUNPOWDER_BLOCK);
		Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk"), MILK_FLUID_STILL);
		Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "flowing_milk"), MILK_FLUID_FLOWING);
		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk_block"), MILK_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "sandwich"), SANDWICH);

		// Register all 16 canvas blocks and items
		CanvasBlock[] canvasBlockArray = new CanvasBlock[16];
		for (DyeColor color : DyeColor.values()) {
			CanvasBlock block = CANVAS_BLOCKS.get(color);
			String name = color.getName() + "_canvas";
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, name), block);
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, name),
				new CanvasBlockItem(block, new Item.Properties()));
			canvasBlockArray[color.getId()] = block;
		}

		CANVAS_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "canvas"),
			BlockEntityType.Builder.of(CanvasBlockEntity::new, canvasBlockArray).build(null)
		);

		// Placeable Gunpowder
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!CONFIG.enablePlaceableGunpowder) return InteractionResult.PASS;

			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(Items.GUNPOWDER)) {
				BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());
				if (world.getBlockState(pos).isAir() && ((GunpowderBlock) GUNPOWDER_BLOCK).canPlaceAt(world, pos)) {
					if (!world.isClientSide()) {
						world.setBlock(pos, GUNPOWDER_BLOCK.defaultBlockState(), 3);
						if (!player.isCreative()) stack.shrink(1);
					}
					return InteractionResult.sidedSuccess(world.isClientSide());
				}
			}
			return InteractionResult.PASS;
		});

		// Placeable Milk
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!CONFIG.enablePlaceableMilk) return InteractionResult.PASS;

			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(Items.MILK_BUCKET)) {
				BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());
				if (world.getBlockState(pos).canBeReplaced()) {
					if (!world.isClientSide()) {
						world.setBlock(pos, MILK_BLOCK.defaultBlockState(), 3);
						if (!player.isCreative()) {
							stack.shrink(1);
							ItemStack bucket = new ItemStack(Items.BUCKET);
							if (stack.isEmpty()) {
								player.setItemInHand(hand, bucket);
							} else if (!player.getInventory().add(bucket)) {
								player.drop(bucket, false);
							}
						}
					}
					return InteractionResult.sidedSuccess(world.isClientSide());
				}
			}
			return InteractionResult.PASS;
		});

		// Repairable Anvils
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!CONFIG.enableRepairableAnvils) return InteractionResult.PASS;

			BlockState state = world.getBlockState(hitResult.getBlockPos());
			if (state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
				ItemStack stack = player.getItemInHand(hand);
				Block repairTarget = null;
				if (stack.is(Items.IRON_BLOCK)) {
					if (!state.is(Blocks.ANVIL)) repairTarget = Blocks.ANVIL;
				} else if (stack.is(Items.IRON_INGOT)) {
					if (state.is(Blocks.DAMAGED_ANVIL)) repairTarget = Blocks.CHIPPED_ANVIL;
					else if (state.is(Blocks.CHIPPED_ANVIL)) repairTarget = Blocks.ANVIL;
				}
				if (repairTarget != null) {
					if (!world.isClientSide()) {
						var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
						world.setBlock(hitResult.getBlockPos(), repairTarget.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
						if (!player.isCreative()) stack.shrink(1);
						world.levelEvent(1031, hitResult.getBlockPos(), 0);
					}
					return InteractionResult.sidedSuccess(world.isClientSide());
				}
			}
			return InteractionResult.PASS;
		});

		LOGGER.info("GarrettTheCarrotMod initialized!");
	}
}
