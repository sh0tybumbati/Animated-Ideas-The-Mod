package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StonecutterBlock.class)
public abstract class StonecutterDamageMixin extends Block {
    public StonecutterDamageMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (GarrettMod.CONFIG.enableDangerousStonecutters && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().cactus(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }
}
