package com.garrett.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

/**
 * A four-slot campfire-style cooker. Mirrors {@link net.minecraft.world.level.block.entity.CampfireBlockEntity}
 * and only cooks while the campfire directly below it is lit; when it does cook it
 * runs at twice campfire speed (progress advances {@value #SPEED} per tick). Finished food pops off above
 * the pan, exactly like a campfire.
 */
public class FryingPanBlockEntity extends BlockEntity implements Clearable {
    public static final int NUM_SLOTS = 4;
    private static final int SPEED = 2; // ticks of progress per real tick -> 2x campfire speed

    private final NonNullList<ItemStack> items = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[NUM_SLOTS];
    private final int[] cookingTime = new int[NUM_SLOTS];
    private final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck =
        RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

    public FryingPanBlockEntity(BlockPos pos, BlockState state) {
        super(GarrettMod.FRYING_PAN_BLOCK_ENTITY, pos, state);
    }

    /** Server cooking tick: only advances while a lit campfire sits directly below. */
    public static void cookTick(Level level, BlockPos pos, BlockState state, FryingPanBlockEntity be) {
        if (!litCampfireBelow(level, pos)) return;
        boolean changed = false;
        for (int i = 0; i < be.items.size(); i++) {
            ItemStack stack = be.items.get(i);
            if (stack.isEmpty()) continue;
            changed = true;
            be.cookingProgress[i] += SPEED;
            if (be.cookingProgress[i] >= be.cookingTime[i]) {
                SingleRecipeInput input = new SingleRecipeInput(stack);
                ItemStack result = be.quickCheck.getRecipeFor(input, level)
                    .map(holder -> holder.value().assemble(input, level.registryAccess()))
                    .orElse(stack);
                if (result.isItemEnabled(level.enabledFeatures())) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.5, pos.getZ(), result);
                    be.items.set(i, ItemStack.EMPTY);
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                }
            }
        }
        if (changed) setChanged(level, pos, state);
    }

    private static boolean litCampfireBelow(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof CampfireBlock && CampfireBlock.isLitCampfire(below);
    }

    /** A recipe is cookable only if there is a free slot and the stack matches a campfire recipe. */
    public Optional<RecipeHolder<CampfireCookingRecipe>> getCookableRecipe(ItemStack stack) {
        if (items.stream().noneMatch(ItemStack::isEmpty)) return Optional.empty();
        return quickCheck.getRecipeFor(new SingleRecipeInput(stack), level);
    }

    /** Places one item into the first free slot. {@code cookTime} should already be the (halved) target. */
    public boolean placeFood(LivingEntity entity, ItemStack stack, int cookTime) {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) continue;
            cookingTime[i] = cookTime;
            cookingProgress[i] = 0;
            items.set(i, stack.consumeAndReturn(1, entity));
            if (level != null) {
                level.gameEvent(GameEvent.BLOCK_CHANGE, getBlockPos(), GameEvent.Context.of(entity, getBlockState()));
            }
            markUpdated();
            return true;
        }
        return false;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    private void markUpdated() {
        setChanged();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        if (tag.contains("CookingTimes")) {
            int[] times = tag.getIntArray("CookingTimes");
            System.arraycopy(times, 0, cookingProgress, 0, Math.min(times.length, cookingProgress.length));
        }
        if (tag.contains("CookingTotalTimes")) {
            int[] totals = tag.getIntArray("CookingTotalTimes");
            System.arraycopy(totals, 0, cookingTime, 0, Math.min(totals.length, cookingTime.length));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.putIntArray("CookingTimes", cookingProgress);
        tag.putIntArray("CookingTotalTimes", cookingTime);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, true, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
