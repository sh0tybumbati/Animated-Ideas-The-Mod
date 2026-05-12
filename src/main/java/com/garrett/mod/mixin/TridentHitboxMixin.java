package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThrownTrident.class)
public abstract class TridentHitboxMixin extends Entity {
    @Shadow protected boolean inGround;

    public TridentHitboxMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean canBeCollidedWith() {
        return (GarrettMod.CONFIG.enableTridentHitboxes && inGround) || super.canBeCollidedWith();
    }

    @Override
    public boolean isPickable() {
        return (GarrettMod.CONFIG.enableTridentHitboxes && inGround) || super.isPickable();
    }
}
