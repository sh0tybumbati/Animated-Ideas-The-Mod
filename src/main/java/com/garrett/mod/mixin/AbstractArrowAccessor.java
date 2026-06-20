package com.garrett.mod.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// inGround is declared on AbstractArrow, so the accessor must target that class
// directly — a @Shadow from a ThrownTrident-targeted mixin cannot resolve it.
@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor("inGround")
    boolean gtcai$getInGround();
}
