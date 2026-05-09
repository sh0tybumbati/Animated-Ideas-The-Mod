package com.garrett.mod.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerShoulderAccessor {
    @Invoker("setShoulderEntityLeft")
    void gtcai$setShoulderEntityLeft(CompoundTag tag);

    @Invoker("setShoulderEntityRight")
    void gtcai$setShoulderEntityRight(CompoundTag tag);
}
