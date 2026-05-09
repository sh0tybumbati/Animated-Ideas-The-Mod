package com.garrett.mod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
import java.util.LinkedHashMap;
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

	// bread(5,0.6) + bread(5,0.6) + meat → summed nutrition, weighted saturation
	public static final Map<String, Item> SANDWICHES = new LinkedHashMap<>();
	static {
		record Meat(String name, int nutrition, float sat) {}
		Meat[] meats = {
			new Meat("beef",    18, 0.7f),
			new Meat("pork",    18, 0.7f),
			new Meat("chicken", 16, 0.6f),
			new Meat("mutton",  16, 0.6f),
			new Meat("rabbit",  15, 0.6f),
			new Meat("salmon",  16, 0.7f),
			new Meat("cod",     15, 0.6f),
		};
		for (Meat m : meats) {
			SANDWICHES.put(m.name() + "_sandwich", new Item(new Item.Properties()
				.food(new FoodProperties.Builder().nutrition(m.nutrition()).saturationModifier(m.sat()).build())));
		}
	}

	public static final FlowingFluid MILK_FLUID_STILL = new MilkFluid.Source();
	public static final FlowingFluid MILK_FLUID_FLOWING = new MilkFluid.Flowing();
	public static final Block MILK_BLOCK = new MilkLiquidBlock(
		MILK_FLUID_STILL,
		BlockBehaviour.Properties.of().noCollission().strength(100.0f).liquid()
			.pushReaction(PushReaction.DESTROY).replaceable().noLootTable()
	);

	public static final Map<DyeColor, CanvasBlock> CANVAS_BLOCKS = new EnumMap<>(DyeColor.class);
	public static CanvasBlock TRANSPARENT_CANVAS;
	public static BlockEntityType<CanvasBlockEntity> CANVAS_BLOCK_ENTITY_TYPE;

	static {
		for (DyeColor color : DyeColor.values()) {
			CANVAS_BLOCKS.put(color, new CanvasBlock(color,
				BlockBehaviour.Properties.of().noOcclusion().strength(0.5f).noLootTable()
			));
		}
		TRANSPARENT_CANVAS = new CanvasBlock(null, true,
			BlockBehaviour.Properties.of().noOcclusion().strength(0.5f).noLootTable()
		);
	}

	@Override
	public void onInitialize() {
		AutoConfig.register(GarrettModConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(GarrettModConfig.class).getConfig();

		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "gunpowder_block"), GUNPOWDER_BLOCK);
		Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk"), MILK_FLUID_STILL);
		Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "flowing_milk"), MILK_FLUID_FLOWING);
		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk_block"), MILK_BLOCK);
		for (Map.Entry<String, Item> e : SANDWICHES.entrySet()) {
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, e.getKey()), e.getValue());
		}

		// Register all 16 colored canvas blocks and items
		CanvasBlock[] canvasBlockArray = new CanvasBlock[17];
		for (DyeColor color : DyeColor.values()) {
			CanvasBlock block = CANVAS_BLOCKS.get(color);
			String name = color.getName() + "_canvas";
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, name), block);
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, name),
				new CanvasBlockItem(block, new Item.Properties()));
			canvasBlockArray[color.getId()] = block;
		}
		// Transparent canvas (cobweb recipe)
		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "transparent_canvas"), TRANSPARENT_CANVAS);
		Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "transparent_canvas"),
			new CanvasBlockItem(TRANSPARENT_CANVAS, new Item.Properties()));
		canvasBlockArray[16] = TRANSPARENT_CANVAS;

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

		// Canvas: left-click with brush + offhand dye = replace pixel
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (!CONFIG.enableCanvases) return InteractionResult.PASS;
			BlockState state = world.getBlockState(pos);
			if (!(state.getBlock() instanceof CanvasBlock canvas)) return InteractionResult.PASS;
			if (state.getValue(CanvasBlock.WAXED)) return InteractionResult.PASS;
			ItemStack mainHand = player.getMainHandItem();
			if (!mainHand.is(Items.BRUSH)) return InteractionResult.PASS;
			ItemStack offhand = player.getOffhandItem();
			if (!(offhand.getItem() instanceof DyeItem dye)) return InteractionResult.PASS;

			HitResult hit = player.pick(5.0, 0, false);
			if (!(hit instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(pos))
				return InteractionResult.PASS;

			if (!world.isClientSide() && world.getBlockEntity(pos) instanceof CanvasBlockEntity be) {
				int pixel = canvas.hitToPixel(state.getValue(CanvasBlock.FACING), blockHit.getLocation(), pos);
				if (pixel >= 0) be.setPixel(pixel, (byte) dye.getDyeColor().getId());
			}
			return InteractionResult.sidedSuccess(world.isClientSide());
		});

		// Canvas: eyedropper C2S packet — set offhand to sampled dye color
		PayloadTypeRegistry.playC2S().register(EyedropperPayload.TYPE, EyedropperPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(EyedropperPayload.TYPE, (payload, context) -> {
			DyeColor color = DyeColor.byId(payload.colorId());
			if (color == null) return;
			ItemStack dye = new ItemStack(DyeItem.byColor(color));
			context.server().execute(() -> {
				var offhand = context.player().getInventory().offhand;
				offhand.set(0, dye);
			});
		});

		LOGGER.info("GarrettTheCarrotMod initialized!");
	}
}
