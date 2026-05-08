package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerGameMode.class)
public class CoopMiningMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!GarrettMod.CONFIG.enableCoopMining) return;
        ServerPlayerGameModeAccessor self = (ServerPlayerGameModeAccessor) (Object) this;
        UUID uuid = self.getPlayer().getUUID();
        if (self.isDestroyingBlock()) {
            GarrettMod.playerMiningPos.put(uuid, self.getDestroyPos());
        } else {
            GarrettMod.playerMiningPos.remove(uuid);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float onGetDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float total = state.getDestroyProgress(player, level, pos);
        if (!GarrettMod.CONFIG.enableCoopMining || !(level instanceof ServerLevel serverLevel)) return total;
        for (ServerPlayer other : serverLevel.players()) {
            if (other.getUUID().equals(player.getUUID())) continue;
            if (pos.equals(GarrettMod.playerMiningPos.get(other.getUUID()))) {
                total += state.getDestroyProgress(other, level, pos);
            }
        }
        return total;
    }
}
