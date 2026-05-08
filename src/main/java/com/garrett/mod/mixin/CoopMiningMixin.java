package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerGameMode.class)
public class CoopMiningMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerGameModeAccessor self = (ServerPlayerGameModeAccessor) (Object) this;
        ServerPlayer player = self.getPlayer();
        UUID uuid = player.getUUID();

        if (!self.isDestroyingBlock()) {
            GarrettMod.playerMiningPos.remove(uuid);
            return;
        }

        BlockPos pos = self.getDestroyPos();
        GarrettMod.playerMiningPos.put(uuid, pos);

        if (!GarrettMod.CONFIG.enableCoopMining) return;

        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);

        float bonus = 0;
        for (ServerPlayer other : level.players()) {
            if (other.getUUID().equals(uuid)) continue;
            if (pos.equals(GarrettMod.playerMiningPos.get(other.getUUID()))) {
                bonus += state.getDestroyProgress(other, level, pos);
            }
        }

        if (bonus > 0) {
            self.setDestroyProgress(self.getDestroyProgress() + bonus);
        }
    }
}
