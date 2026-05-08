package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.server.level.ServerPlayerGameMode;
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
        UUID uuid = self.getPlayer().getUUID();
        if (self.isDestroyingBlock()) {
            GarrettMod.playerMiningPos.put(uuid, self.getDestroyPos());
        } else {
            GarrettMod.playerMiningPos.remove(uuid);
        }
    }
}
