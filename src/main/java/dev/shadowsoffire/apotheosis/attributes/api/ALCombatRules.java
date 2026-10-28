package dev.shadowsoffire.apotheosis.attributes.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Contains Apothic Attributes combat calculations for armor, toughness, and protection values.
 */
public class ALCombatRules {

    public static final float DEFAULT_NEGATIVE_ARMOR_FACTOR = 0.015F;

    /**
     * Gets the amount of damage the user would take after applying protection points and protection bypass.<br>
     * Protection bypass is based on {@linkplain ALObjects.Attributes#PROT_PIERCE Protection Pierce} and {@linkplain ALObjects.Attributes#PROT_SHRED Protection Shred}.
     *
     * @param target     The attack's target.
     * @param src        The DamageSource of the attack.
     * @param amount     The amount of damage the attack is currently dealing, after armor has been applied.
     * @param protPoints The amount of protection points the target has against the incoming damage source.
     * @return The modified damage value, after applying protection points, accounting for the attacker's bypass.
     */
    public static float getDamageAfterProtection(LivingEntity target, DamageSource src, float amount, float protPoints) {
        if (src.getEntity() instanceof LivingEntity attacker) {
            float shred = (float) attacker.getAttributeValue(ALObjects.Attributes.PROT_SHRED);
            if (shred > 0.001F) {
                protPoints *= 1.0F - shred;
            }
            float pierce = (float) attacker.getAttributeValue(ALObjects.Attributes.PROT_PIERCE);
            if (pierce > 0.001F) {
                protPoints -= pierce;
            }
        }

        if (protPoints <= 0) return amount;
        return amount * getProtDamageReduction(protPoints);
    }

    /**
     * Computes the damage reduction factor for the given amount of protection points.<br>
     * Each protection point reduces damage by 2.5%, up to 85%.
     * <p>
     * In vanilla, each protection point reduces damage by 4%, up to 80%.
     *
     * @see #getDamageAfterProtection(LivingEntity, DamageSource, float, float)
     */
    public static float getProtDamageReduction(float protPoints) {
        return 1.0F - Math.min(0.025F * protPoints, 0.85F);
    }

    /**
     * Gets the amount of damage the user would take after applying armor, toughness, and armor bypass.<br>
     * Armor bypass is based on {@linkplain ALObjects.Attributes#ARMOR_PIERCE Armor Pierce} and {@linkplain ALObjects.Attributes#ARMOR_SHRED Armor Shred}.
     * <p>
     * With Apothic Attributes, armor toughness acts as a shield against armor bypass.<br>
     * Each point of armor toughness reduces the effectiveness of all armor bypass by 2%, up to 60%.<br>
     * Armor toughness no longer reduces damage directly, only armor bypass.
     *
     * @param target    The attack's target.
     * @param src       The DamageSource of the attack.
     * @param amount    The amount of damage the attack is currently dealing, after armor has been applied.
     * @param armor     The amount of armor points the target has.
     * @param toughness The amount of armor toughness points the target has.
     * @return The modified damage value, after applying armor, accounting for the attacker's bypass.
     */
    public static float getDamageAfterArmor(LivingEntity target, DamageSource src, float amount, float armor, float toughness) {
        if (src.getEntity() instanceof LivingEntity attacker) {
            float shred = (float) attacker.getAttributeValue(ALObjects.Attributes.ARMOR_SHRED);
            float bypassResist = getBypassResistance(amount, armor, toughness);
            if (shred > 0.001F) {
                shred *= 1.0F - bypassResist;
                armor *= 1.0F - shred;
            }
            float pierce = (float) attacker.getAttributeValue(ALObjects.Attributes.ARMOR_PIERCE);
            if (pierce > 0.001F) {
                pierce *= 1.0F - bypassResist;
                armor -= pierce;
            }
        }

        if (armor <= 0) {
            return amount + (DEFAULT_NEGATIVE_ARMOR_FACTOR * -armor * amount);
        }

        float reduction = getArmorDamageReduction(amount, armor, toughness);

        if (src.getWeaponItem() != null && target.level() instanceof ServerLevel serverlevel) {
            float effectiveness = 1.0F - reduction;
            effectiveness = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverlevel, src.getWeaponItem(), target, src, effectiveness), 0.0F, 1.0F);
            reduction = 1.0F - effectiveness;
        }

        return amount * reduction;
    }

    /**
     * Computes the A value used in the Y = A / (A + X) formula used by {@link #getArmorDamageReduction(float, float, float)}.<br>
     * This value is a flat 10 for small damage values (&lt; 20), and increases after that point.
     *
     * @param damage The amount of incoming damage.
     * @return The A value, for use in {@link #getArmorDamageReduction(float, float, float)}
     */
    public static float getAValue(float damage) {
        return damage < 20 ? 10.0F : 10.0F + (damage - 20.0F) / 2.0F;
    }

    /**
     * Computes the damage reduction factor of the given armor level.<br>
     * Armor reduces a percentage of incoming damage equal to <code>A / (A + armor)</code>, where A varies based on the damage.
     *
     * @see #getAValue(float)
     * @see #getDamageAfterArmor(LivingEntity, DamageSource, float, float, float)
     */
    public static float getArmorDamageReduction(float damage, float armor, float toughness) {
        float a = getAValue(damage);
        return a / (a + armor);
    }

    /**
     * Computes the amount of armor bypass that the target's toughness value will resist.
     * <p>
     * A returned value of 1.0 will nullify enemy armor bypass. Max is 0.60 (60%).
     */
    public static float getBypassResistance(float damage, float armor, float toughness) {
        return Math.min(toughness * 0.02F, 0.60F);
    }
}
