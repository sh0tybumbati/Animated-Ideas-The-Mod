package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateCoopMixin {

    // Guard against re-entrant calls when we call getDestroyProgress on co-miners
    private static final ThreadLocal<Boolean> BOOSTING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true, require = 0)
    private void coopBoost(Player player, BlockGetter level, BlockPos pos,
                           CallbackInfoReturnable<Float> cir) {
        if (BOOSTING.get() || !GarrettMod.CONFIG.enableCoopMining) return;
        if (!(player instanceof ServerPlayer) || !(level instanceof ServerLevel serverLevel)) return;

        UUID uuid = player.getUUID();
        float total = cir.getReturnValue();
        BOOSTING.set(true);
        try {
            for (ServerPlayer other : serverLevel.players()) {
                if (!other.getUUID().equals(uuid)
                        && pos.equals(GarrettMod.playerMiningPos.get(other.getUUID()))) {
                    total += ((BlockState) (Object) this).getDestroyProgress(other, level, pos);
                }
            }
        } finally {
            BOOSTING.set(false);
        }
        cir.setReturnValue(total);
    }
}
