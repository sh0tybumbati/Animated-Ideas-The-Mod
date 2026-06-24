package com.garrett.mod;

import com.garrett.mod.mixin.PlayerShoulderAccessor;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
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
	public static final String MOD_ID = "gtcai";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static GarrettModConfig CONFIG;
	public static final Map<UUID, BlockPos> playerMiningPos = new HashMap<>();

	// Which shoulder a perched parrot came from, so it perches on the matching side.
	public static final AttachmentType<Boolean> PARROT_LEFT_SHOULDER = AttachmentRegistry.createPersistent(
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "parrot_left_shoulder"), Codec.BOOL);

	public static final Block GUNPOWDER_BLOCK = new GunpowderBlock(
		BlockBehaviour.Properties.of().noOcclusion().instabreak()
	);

	public static final Block AIR_FRYER_BLOCK = new AirFryerBlock(
		BlockBehaviour.Properties.of().strength(3.5f).requiresCorrectToolForDrops()
			.lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0)
	);
	public static BlockEntityType<AirFryerBlockEntity> AIR_FRYER_BLOCK_ENTITY;

	public static final Block FRYING_PAN_BLOCK = new FryingPanBlock(
		BlockBehaviour.Properties.of().strength(2.0f).requiresCorrectToolForDrops().noOcclusion()
	);
	public static BlockEntityType<FryingPanBlockEntity> FRYING_PAN_BLOCK_ENTITY;

	public static final Block CARVABLE_PUMPKIN_BLOCK = new CarvablePumpkinBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN)
			.lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 15 : 0)
			.noLootTable() // breaking drops a carving-preserving item via playerWillDestroy
	);
	public static BlockEntityType<CarvablePumpkinBlockEntity> CARVABLE_PUMPKIN_BLOCK_ENTITY;

	// Sandwiches (every edible vanilla food) are defined in Sandwiches.

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

	public static final Block PUMPKIN_PIE_BLOCK = new PumpkinPieBlock(
		BlockBehaviour.Properties.of().strength(0.5f).noOcclusion()
	);

	public static final Item MILK_SPLASH_POTION = new MilkSplashPotionItem(
		new Item.Properties().stacksTo(1)
	);

	public static final Item DOODLE_BOOK = new DoodleBookItem(new Item.Properties().stacksTo(1));

	// Cheese: edible slice, building block, and the milk-cauldron that produces it.
	public static final Item CHEESE = new Item(new Item.Properties()
		.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build()));
	public static final Block CHEESE_BLOCK = new Block(
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.5f).sound(SoundType.WOOL));
	public static final Block MILK_CAULDRON_BLOCK = new MilkCauldronBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).noLootTable());

	// Cooking eggs, sleeping bag, trumpet (Part 3 smalls).
	public static final Item FRIED_EGG = new Item(new Item.Properties()
		.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build()));
	public static final Block SLEEPING_BAG_BLOCK = new SleepingBagBlock(DyeColor.RED,
		BlockBehaviour.Properties.ofFullCopy(Blocks.RED_BED));
	public static final Item TRUMPET = new TrumpetItem(new Item.Properties().stacksTo(1));
	public static final SoundEvent TRUMPET_LOW = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "trumpet.low"));
	public static final SoundEvent TRUMPET_MID = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "trumpet.mid"));
	public static final SoundEvent TRUMPET_HIGH = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "trumpet.high"));

	public static EntityType<ThrownMilkPotion> THROWN_MILK_POTION_ENTITY_TYPE;

	public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY =
		ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, "general"));

	@Override
	public void onInitialize() {
		AutoConfig.register(GarrettModConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(GarrettModConfig.class).getConfig();

		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "pumpkin_pie_block"), PUMPKIN_PIE_BLOCK);

		if (CONFIG.enableMilkSplashPotion) {
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk_splash_potion"), MILK_SPLASH_POTION);

			THROWN_MILK_POTION_ENTITY_TYPE = Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk_splash_potion"),
				EntityType.Builder.<ThrownMilkPotion>of(ThrownMilkPotion::new, net.minecraft.world.entity.MobCategory.MISC)
					.sized(0.25F, 0.25F).build("milk_splash_potion")
			);
		}

		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "gunpowder_block"), GUNPOWDER_BLOCK);
		Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk"), MILK_FLUID_STILL);
		Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "flowing_milk"), MILK_FLUID_FLOWING);
		Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk_block"), MILK_BLOCK);
		if (CONFIG.enableSandwiches) {
			Sandwiches.registerAll();
		}

		if (CONFIG.enableAirFryer) {
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "air_fryer"), AIR_FRYER_BLOCK);
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "air_fryer"),
				new BlockItem(AIR_FRYER_BLOCK, new Item.Properties()));
			AIR_FRYER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(MOD_ID, "air_fryer"),
				BlockEntityType.Builder.of(AirFryerBlockEntity::new, AIR_FRYER_BLOCK).build(null));
		}

		if (CONFIG.enableFryingPan) {
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "frying_pan"), FRYING_PAN_BLOCK);
			// Wieldable as a goofy, heavy, slow melee weapon: a light bonk but a slow swing.
			ItemAttributeModifiers panAttributes = ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
					new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 2.0, AttributeModifier.Operation.ADD_VALUE),
					EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
					new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.8, AttributeModifier.Operation.ADD_VALUE),
					EquipmentSlotGroup.MAINHAND)
				.build();
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "frying_pan"),
				new FryingPanItem(FRYING_PAN_BLOCK, new Item.Properties().attributes(panAttributes)));
			FRYING_PAN_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(MOD_ID, "frying_pan"),
				BlockEntityType.Builder.of(FryingPanBlockEntity::new, FRYING_PAN_BLOCK).build(null));
		}

		if (CONFIG.enableDoodleBooks) {
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "doodle_book"), DOODLE_BOOK);
			PayloadTypeRegistry.playC2S().register(DoodleBookSavePayload.TYPE, DoodleBookSavePayload.CODEC);
			ServerPlayNetworking.registerGlobalReceiver(DoodleBookSavePayload.TYPE, (payload, context) -> {
				byte[] px = payload.pixels();
				int page = payload.page();
				if (px.length != DoodleBookSavePayload.SIZE || page < 0 || page >= DoodleBookItem.MAX_PAGES) return;
				context.server().execute(() -> {
					ServerPlayer p = context.player();
					for (InteractionHand h : InteractionHand.values()) {
						ItemStack s = p.getItemInHand(h);
						if (s.getItem() instanceof DoodleBookItem) {
							DoodleBookItem.setPage(s, page, px);
							break;
						}
					}
				});
			});
		}

		if (CONFIG.enableCookingEggs) {
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fried_egg"), FRIED_EGG);
		}

		if (CONFIG.enableSleepingBags) {
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "sleeping_bag"), SLEEPING_BAG_BLOCK);
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "sleeping_bag"),
				new BlockItem(SLEEPING_BAG_BLOCK, new Item.Properties()));
			// Sleeping in a sleeping bag must NOT change the player's spawn point.
			EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) ->
				!(player.level().getBlockState(sleepingPos).getBlock() instanceof SleepingBagBlock));
		}

		if (CONFIG.enableTrumpets) {
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "trumpet"), TRUMPET);
			Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "trumpet.low"), TRUMPET_LOW);
			Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "trumpet.mid"), TRUMPET_MID);
			Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "trumpet.high"), TRUMPET_HIGH);
		}

		if (CONFIG.enableCheese) {
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "cheese"), CHEESE);
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "cheese_block"), CHEESE_BLOCK);
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "cheese_block"),
				new BlockItem(CHEESE_BLOCK, new Item.Properties()));
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk_cauldron"), MILK_CAULDRON_BLOCK);
		}

		if (CONFIG.enableCustomPumpkinCarving) {
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "carvable_pumpkin"), CARVABLE_PUMPKIN_BLOCK);
			Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "carvable_pumpkin"),
				new BlockItem(CARVABLE_PUMPKIN_BLOCK, new Item.Properties()));
			CARVABLE_PUMPKIN_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(MOD_ID, "carvable_pumpkin"),
				BlockEntityType.Builder.of(CarvablePumpkinBlockEntity::new, CARVABLE_PUMPKIN_BLOCK).build(null));
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

		// Placeable Pumpkin Pie
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!CONFIG.enablePlaceablePumpkinPie) return InteractionResult.PASS;

			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(Items.PUMPKIN_PIE)) {
				BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());
				if (world.getBlockState(pos).canBeReplaced()) {
					if (!world.isClientSide()) {
						world.setBlock(pos, PUMPKIN_PIE_BLOCK.defaultBlockState(), 3);
						if (!player.isCreative()) stack.shrink(1);
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
				if (pixel >= 0) be.setPixel(pixel, dye.getDyeColor().getTextureDiffuseColor() & 0xFFFFFF);
			}
			return InteractionResult.sidedSuccess(world.isClientSide());
		});

		// Parrots on armor stands
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!CONFIG.enableParrotArmorStands) return InteractionResult.PASS;
			if (world.isClientSide()) return InteractionResult.PASS;
			if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
			if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

			// Click a perched parrot to take it back onto your shoulder.
			if (entity instanceof Parrot perched && perched.getVehicle() instanceof ArmorStand) {
				CompoundTag back = new CompoundTag();
				perched.saveWithoutId(back);
				back.putString("id", "minecraft:parrot");
				if (serverPlayer.setEntityOnShoulder(back)) {
					perched.discard();
					return InteractionResult.sidedSuccess(world.isClientSide());
				}
				return InteractionResult.PASS;
			}

			if (!(entity instanceof ArmorStand stand)) return InteractionResult.PASS;

			// Find a parrot on either shoulder
			var left  = serverPlayer.getShoulderEntityLeft();
			var right = serverPlayer.getShoulderEntityRight();
			boolean onLeft = !left.isEmpty()  && "minecraft:parrot".equals(left.getString("id"));
			boolean onRight = !right.isEmpty() && "minecraft:parrot".equals(right.getString("id"));
			if (!onLeft && !onRight) return InteractionResult.PASS;

			var tag = (onLeft ? left : right).copy();
			ServerLevel level = (ServerLevel) world;

			// Free the player's shoulder slot first so a swapped-out parrot has room to return.
			PlayerShoulderAccessor accessor = (PlayerShoulderAccessor) serverPlayer;
			if (onLeft) accessor.gtcai$setShoulderEntityLeft(new CompoundTag());
			else        accessor.gtcai$setShoulderEntityRight(new CompoundTag());

			// One parrot per stand: send any existing perched parrot back to the player.
			for (Entity rider : stand.getPassengers()) {
				if (rider instanceof Parrot old) {
					CompoundTag back = new CompoundTag();
					old.saveWithoutId(back);
					back.putString("id", "minecraft:parrot");
					serverPlayer.setEntityOnShoulder(back);
					old.discard();
				}
			}

			Entity spawned = EntityType.loadEntityRecursive(tag, level, e -> {
				e.moveTo(stand.getX(), stand.getY(), stand.getZ(), stand.getYRot(), 0.0f);
				return e;
			});
			if (spawned != null) {
				level.addFreshEntity(spawned);
				if (spawned instanceof Parrot parrot) {
					parrot.setOrderedToSit(true);
					parrot.setInSittingPose(true);
					parrot.setAttached(PARROT_LEFT_SHOULDER, onLeft);
					// Ride the stand so it perches instead of dropping off (must be added to the level first).
					parrot.startRiding(stand, true);
				}
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

		// Creative mode tab — only adds items that are actually registered, so it
		// stays consistent with the enableSandwiches / enableMilkSplashPotion flags.
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP_KEY,
			FabricItemGroup.builder()
				.icon(() -> new ItemStack(CONFIG.enableMilkSplashPotion
					? MILK_SPLASH_POTION
					: CANVAS_BLOCKS.get(DyeColor.WHITE).asItem()))
				.title(Component.translatable("itemGroup.gtcai.general"))
				.build());

		ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY).register(entries -> {
			if (CONFIG.enableAirFryer) entries.accept(AIR_FRYER_BLOCK);
			if (CONFIG.enableFryingPan) entries.accept(FRYING_PAN_BLOCK);
			if (CONFIG.enableDoodleBooks) entries.accept(DOODLE_BOOK);
			if (CONFIG.enableCheese) {
				entries.accept(CHEESE);
				entries.accept(CHEESE_BLOCK);
			}
			if (CONFIG.enableCookingEggs) entries.accept(FRIED_EGG);
			if (CONFIG.enableSleepingBags) entries.accept(SLEEPING_BAG_BLOCK);
			if (CONFIG.enableTrumpets) entries.accept(TRUMPET);
			if (CONFIG.enableSandwiches) {
				for (Item sandwich : Sandwiches.ITEMS.values()) entries.accept(sandwich);
			}
			for (DyeColor color : DyeColor.values()) entries.accept(CANVAS_BLOCKS.get(color));
			entries.accept(TRANSPARENT_CANVAS);
			if (CONFIG.enableMilkSplashPotion) entries.accept(MILK_SPLASH_POTION);
		});

		// Custom Pumpkin Carving — left-click a pumpkin with a sword to carve a pixel.
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (!CONFIG.enableCustomPumpkinCarving) return InteractionResult.PASS;
			if (!(player.getMainHandItem().getItem() instanceof SwordItem)) return InteractionResult.PASS;
			if (!direction.getAxis().isHorizontal()) return InteractionResult.PASS;

			BlockState state = world.getBlockState(pos);
			boolean vanilla = state.is(Blocks.PUMPKIN);
			boolean carvable = state.is(CARVABLE_PUMPKIN_BLOCK);
			if (!vanilla && !carvable) return InteractionResult.PASS;

			HitResult hr = player.pick(5.0, 0, false);
			if (!(hr instanceof BlockHitResult bhr) || !bhr.getBlockPos().equals(pos)) return InteractionResult.PASS;

			if (world.isClientSide()) return InteractionResult.SUCCESS;

			if (vanilla) {
				world.setBlock(pos, CARVABLE_PUMPKIN_BLOCK.defaultBlockState(), 3);
				Block.popResource(world, pos, new ItemStack(Items.PUMPKIN_SEEDS, 4));
			}
			if (world.getBlockEntity(pos) instanceof CarvablePumpkinBlockEntity be) {
				int pixel = CarvablePumpkinBlock.hitToPixel(direction, bhr.getLocation(), pos);
				if (pixel >= 0) be.carve(direction, pixel);
				world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0f, 1.0f);
			}
			return InteractionResult.SUCCESS;
		});

		// Cheese: fill an empty cauldron with a milk bucket to start it curdling.
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!CONFIG.enableCheese) return InteractionResult.PASS;
			ItemStack stack = player.getItemInHand(hand);
			if (!stack.is(Items.MILK_BUCKET)) return InteractionResult.PASS;
			BlockPos pos = hitResult.getBlockPos();
			if (!world.getBlockState(pos).is(Blocks.CAULDRON)) return InteractionResult.PASS;
			if (!world.isClientSide()) {
				world.setBlockAndUpdate(pos, MILK_CAULDRON_BLOCK.defaultBlockState());
				world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
				if (!player.isCreative()) {
					stack.shrink(1);
					ItemStack bucket = new ItemStack(Items.BUCKET);
					if (stack.isEmpty()) player.setItemInHand(hand, bucket);
					else if (!player.getInventory().add(bucket)) player.drop(bucket, false);
				}
			}
			return InteractionResult.sidedSuccess(world.isClientSide());
		});

		LOGGER.info("GarrettTheCarrotMod initialized!");
	}
}
