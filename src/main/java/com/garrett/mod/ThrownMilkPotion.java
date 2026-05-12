package com.garrett.mod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class ThrownMilkPotion extends ThrowableItemProjectile {
    public ThrownMilkPotion(EntityType<? extends ThrownMilkPotion> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownMilkPotion(Level level, LivingEntity livingEntity) {
        super(GarrettMod.THROWN_MILK_POTION_ENTITY_TYPE, livingEntity, level);
    }

    @Override
    protected Item getDefaultItem() {
        return GarrettMod.MILK_SPLASH_POTION;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            AABB aabb = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, aabb)) {
                livingEntity.removeAllEffects();
            }
            this.level().levelEvent(2002, this.blockPosition(), 0xFFFFFF); // Potion splash particles (white)
            this.discard();
        }
    }
}
