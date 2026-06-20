package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

// Extends AbstractArrow (the real superclass of ThrownTrident) so the overridden
// super.isPickable()/super.canBeCollidedWith() calls resolve correctly. inGround
// is read via AbstractArrowAccessor because it lives on AbstractArrow, not here.
@Mixin(ThrownTrident.class)
public abstract class TridentHitboxMixin extends AbstractArrow {
    public TridentHitboxMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    private boolean gtcai$grounded() {
        return GarrettMod.CONFIG.enableTridentHitboxes
            && ((AbstractArrowAccessor) (Object) this).gtcai$getInGround();
    }

    @Override
    public boolean canBeCollidedWith() {
        return gtcai$grounded() || super.canBeCollidedWith();
    }

    @Override
    public boolean isPickable() {
        return gtcai$grounded() || super.isPickable();
    }
}
