package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Extends AbstractArrow (the real superclass of ThrownTrident) so the overridden
// super.isPickable()/super.canBeCollidedWith() calls resolve correctly. inGround
// is read via AbstractArrowAccessor because it lives on AbstractArrow.
@Mixin(ThrownTrident.class)
public abstract class TridentHitboxMixin extends AbstractArrow {
    // Thin, end-rod-like collision box, aligned to the trident's facing axis.
    // Tweak these to taste: half the rod length, and half its thickness/height.
    @Unique private static final double GTCAI_HALF_LENGTH = 0.85;  // ~1.7 long
    @Unique private static final double GTCAI_HALF_THICK = 0.08;   // ~0.16 across

    public TridentHitboxMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    // Only tridents embedded in a wall act as standable pegs. A wall-stuck trident sits
    // roughly horizontal, while one stuck in the floor or ceiling points steeply, so we
    // gate on pitch. Pitch/yaw are synced to the client (where the player's collision is
    // resolved). Keeping ground tridents non-collidable also stops them blocking other
    // tridents from landing.
    @Unique
    private boolean gtcai$standable() {
        return GarrettMod.CONFIG.enableTridentHitboxes
            && ((AbstractArrowAccessor) (Object) this).gtcai$getInGround()
            && Math.abs(this.getXRot()) < 45.0f;
    }

    @Unique
    private AABB gtcai$rodBox() {
        // The entity origin sits at the trident's tip (embedded in the wall); the shaft
        // protrudes the opposite way it faces. Span the box from the tip back along the
        // shaft rather than centring it on the tip.
        Direction back = Direction.fromYRot(this.getYRot()).getOpposite();
        double len = GTCAI_HALF_LENGTH * 2.0;
        double x = this.getX(), y = this.getY(), z = this.getZ();
        double ex = x + back.getStepX() * len;
        double ez = z + back.getStepZ() * len;
        double minX = Math.min(x, ex), maxX = Math.max(x, ex);
        double minZ = Math.min(z, ez), maxZ = Math.max(z, ez);
        if (back.getAxis() == Direction.Axis.X) {
            minZ -= GTCAI_HALF_THICK; maxZ += GTCAI_HALF_THICK;
        } else {
            minX -= GTCAI_HALF_THICK; maxX += GTCAI_HALF_THICK;
        }
        return new AABB(minX, y - GTCAI_HALF_THICK, minZ, maxX, y + GTCAI_HALF_THICK, maxZ);
    }

    // Return the thin box from every recompute so vanilla's position handling can't flip
    // it back to the default cube (which caused the box to blink between sizes).
    @Override
    protected AABB makeBoundingBox() {
        return gtcai$standable() ? gtcai$rodBox() : super.makeBoundingBox();
    }

    // A stuck trident never moves, so force the thin box to apply once it's standable.
    @Inject(method = "tick", at = @At("TAIL"))
    private void gtcai$applyRodBox(CallbackInfo ci) {
        if (gtcai$standable()) {
            this.setBoundingBox(gtcai$rodBox());
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return gtcai$standable() || super.canBeCollidedWith();
    }

    @Override
    public boolean isPickable() {
        return gtcai$standable() || super.isPickable();
    }
}
