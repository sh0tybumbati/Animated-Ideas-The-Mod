package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

// Perch a riding parrot on the armor stand's shoulder instead of its default (centered) mount point.
// Extends LivingEntity so the inherited getPassengerAttachmentPoint can be soft-overridden with a super call.
@Mixin(ArmorStand.class)
public abstract class ArmorStandPerchMixin extends LivingEntity {
    protected ArmorStandPerchMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        Vec3 base = super.getPassengerAttachmentPoint(entity, dimensions, scale);
        if (entity instanceof Parrot parrot) {
            // Perch on the same side the parrot was on the player; rotate the sideways offset by the
            // stand's yaw so it tracks facing, then drop to shoulder height.
            boolean left = parrot.getAttachedOrElse(GarrettMod.PARROT_LEFT_SHOULDER, false);
            Vec3 side = new Vec3(left ? -0.32 : 0.32, 0.0, 0.0).yRot(-(float) Math.toRadians(this.getYRot()));
            return base.add(side.x, -0.42, side.z);
        }
        return base;
    }
}
