package com.garrett.mod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerGameMode.class)
public interface ServerPlayerGameModeAccessor {
    @Accessor("player") ServerPlayer getPlayer();
    @Accessor("isDestroyingBlock") boolean isDestroyingBlock();
    @Accessor("destroyPos") BlockPos getDestroyPos();
    @Accessor("destroyProgress") float getDestroyProgress();
    @Accessor("destroyProgress") void setDestroyProgress(float value);
}
