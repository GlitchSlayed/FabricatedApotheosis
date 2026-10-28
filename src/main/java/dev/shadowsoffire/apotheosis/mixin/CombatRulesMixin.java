package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.shadowsoffire.apotheosis.attributes.api.ALCombatRules;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@Mixin(CombatRules.class)
public class CombatRulesMixin {

    /**
     * @author Shadows_of_Fire / Fabric Port
     * @reason Overwrite vanilla protection formula with Apothic Attributes protection curve.
     */
    @Overwrite
    public static float getDamageAfterMagicAbsorb(float damage, float protPoints) {
        return damage * ALCombatRules.getProtDamageReduction(protPoints);
    }

    /**
     * @author Shadows_of_Fire / Fabric Port
     * @reason Overwrite vanilla armor formula with Apothic Attributes armor and toughness curve.
     */
    @Overwrite
    public static float getDamageAfterAbsorb(LivingEntity entity, float damage, DamageSource damageSource, float armor, float toughness) {
        return ALCombatRules.getDamageAfterArmor(entity, damageSource, damage, armor, toughness);
    }
}
