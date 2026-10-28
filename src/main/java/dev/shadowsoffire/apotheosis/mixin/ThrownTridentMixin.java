package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.entity.projectile.arrow.ThrownTrident;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {

    @ModifyConstant(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", constant = @Constant(floatValue = 8.0F))
    public float apoth_getTridentDamage(float defaultDmg) {
        double baseDamage = ((AbstractArrowAccessor) this).getBaseDamage();
        return (float) (baseDamage * 4.0F);
    }
}
