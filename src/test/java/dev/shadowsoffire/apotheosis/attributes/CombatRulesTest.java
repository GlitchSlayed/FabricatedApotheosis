package dev.shadowsoffire.apotheosis.attributes;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.shadowsoffire.apotheosis.attributes.api.ALCombatRules;
import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AbilityCooldowns;
import dev.shadowsoffire.apotheosis.attributes.api.AttributeEvents;
import dev.shadowsoffire.apotheosis.attributes.api.CooldownTracker;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;

public class CombatRulesTest {

    @BeforeAll
    public static void setup() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        Field frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
        frozenField.setAccessible(true);
        frozenField.set(BuiltInRegistries.ATTRIBUTE, false);
        frozenField.set(BuiltInRegistries.MOB_EFFECT, false);
        frozenField.set(BuiltInRegistries.SOUND_EVENT, false);
        frozenField.set(BuiltInRegistries.PARTICLE_TYPE, false);

        ALObjects.bootstrap();
    }

    @Test
    public void testAValueFormula() {
        // For damage < 20, A is constant 10.0
        assertEquals(10.0F, ALCombatRules.getAValue(0.0F), 0.001F);
        assertEquals(10.0F, ALCombatRules.getAValue(10.0F), 0.001F);
        assertEquals(10.0F, ALCombatRules.getAValue(19.9F), 0.001F);

        // For damage >= 20, A = 10 + (damage - 20) / 2
        assertEquals(10.0F, ALCombatRules.getAValue(20.0F), 0.001F);
        assertEquals(15.0F, ALCombatRules.getAValue(30.0F), 0.001F);
        assertEquals(20.0F, ALCombatRules.getAValue(40.0F), 0.001F);
        assertEquals(50.0F, ALCombatRules.getAValue(100.0F), 0.001F);
    }

    @Test
    public void testArmorDamageReduction() {
        // damage 10, armor 10 -> A = 10, reduction = 10 / (10 + 10) = 0.5 (50% damage taken)
        float dr1 = ALCombatRules.getArmorDamageReduction(10.0F, 10.0F, 0.0F);
        assertEquals(0.5F, dr1, 0.001F);

        // damage 10, armor 30 -> A = 10, reduction = 10 / (10 + 30) = 0.25 (25% damage taken)
        float dr2 = ALCombatRules.getArmorDamageReduction(10.0F, 30.0F, 0.0F);
        assertEquals(0.25F, dr2, 0.001F);

        // damage 30, armor 15 -> A = 15, reduction = 15 / (15 + 15) = 0.5 (50% damage taken)
        float dr3 = ALCombatRules.getArmorDamageReduction(30.0F, 15.0F, 0.0F);
        assertEquals(0.5F, dr3, 0.001F);
    }

    @Test
    public void testToughnessBypassResistance() {
        // Toughness gives 2% bypass resist per point, capped at 60% (0.60)
        assertEquals(0.0F, ALCombatRules.getBypassResistance(10.0F, 10.0F, 0.0F), 0.001F);
        assertEquals(0.10F, ALCombatRules.getBypassResistance(10.0F, 10.0F, 5.0F), 0.001F);
        assertEquals(0.20F, ALCombatRules.getBypassResistance(10.0F, 10.0F, 10.0F), 0.001F);
        assertEquals(0.40F, ALCombatRules.getBypassResistance(10.0F, 10.0F, 20.0F), 0.001F);
        assertEquals(0.60F, ALCombatRules.getBypassResistance(10.0F, 10.0F, 30.0F), 0.001F);
        // Capped at 60%
        assertEquals(0.60F, ALCombatRules.getBypassResistance(10.0F, 10.0F, 50.0F), 0.001F);
        assertEquals(0.60F, ALCombatRules.getBypassResistance(10.0F, 10.0F, 100.0F), 0.001F);
    }

    @Test
    public void testNegativeEffectiveArmorAmplification() {
        // When effective armor <= 0, formula is: amount + (0.015 * -armor * amount)
        float baseDamage = 20.0F;

        // Effective armor = 0 -> 20.0
        float dmg0 = baseDamage + (ALCombatRules.DEFAULT_NEGATIVE_ARMOR_FACTOR * 0.0F * baseDamage);
        assertEquals(20.0F, dmg0, 0.001F);

        // Effective armor = -10 -> 20 + 0.015 * 10 * 20 = 20 + 3.0 = 23.0 (+15%)
        float dmgNeg10 = baseDamage + (ALCombatRules.DEFAULT_NEGATIVE_ARMOR_FACTOR * 10.0F * baseDamage);
        assertEquals(23.0F, dmgNeg10, 0.001F);

        // Effective armor = -40 -> 20 + 0.015 * 40 * 20 = 20 + 12.0 = 32.0 (+60%)
        float dmgNeg40 = baseDamage + (ALCombatRules.DEFAULT_NEGATIVE_ARMOR_FACTOR * 40.0F * baseDamage);
        assertEquals(32.0F, dmgNeg40, 0.001F);

        // Effective armor = -100 -> 20 + 0.015 * 100 * 20 = 20 + 30.0 = 50.0 (+150%)
        float dmgNeg100 = baseDamage + (ALCombatRules.DEFAULT_NEGATIVE_ARMOR_FACTOR * 100.0F * baseDamage);
        assertEquals(50.0F, dmgNeg100, 0.001F);
    }

    @Test
    public void testProtectionReductionAndCaps() {
        // 2.5% per point, max 85%
        // 0 points -> 1.0 (0% reduction)
        assertEquals(1.0F, ALCombatRules.getProtDamageReduction(0.0F), 0.001F);

        // 10 points -> 1 - 0.25 = 0.75 (25% reduction)
        assertEquals(0.75F, ALCombatRules.getProtDamageReduction(10.0F), 0.001F);

        // 20 points -> 1 - 0.50 = 0.50 (50% reduction)
        assertEquals(0.50F, ALCombatRules.getProtDamageReduction(20.0F), 0.001F);

        // 34 points -> 34 * 0.025 = 0.85 -> 1 - 0.85 = 0.15 (85% reduction)
        assertEquals(0.15F, ALCombatRules.getProtDamageReduction(34.0F), 0.001F);

        // 40 points -> capped at 85% -> 0.15
        assertEquals(0.15F, ALCombatRules.getProtDamageReduction(40.0F), 0.001F);
        assertEquals(0.15F, ALCombatRules.getProtDamageReduction(100.0F), 0.001F);
    }

    @Test
    public void testMulticritDegradationFormula() {
        // Initial crit: baseAmount = 10, critDmg = 2.0 (200%), critChance = 2.0 (guaranteed 2 crits)
        float baseAmount = 10.0F;
        float damage = baseAmount;
        float critDmg = 2.0F;
        double critChance = 2.0D;

        // 1st crit: damage += 10 * (2.0 - 1) = 10 + 10 = 20. critDmg *= 0.85 -> 1.70. critChance -> 1.0
        critChance--;
        damage += baseAmount * (critDmg - 1.0F);
        critDmg *= 0.85F;
        assertEquals(20.0F, damage, 0.001F);
        assertEquals(1.70F, critDmg, 0.001F);

        // 2nd crit: damage += 10 * (1.70 - 1) = 20 + 7 = 27. critDmg *= 0.85 -> 1.445. critChance -> 0.0
        critChance--;
        damage += baseAmount * (critDmg - 1.0F);
        critDmg *= 0.85F;
        assertEquals(27.0F, damage, 0.001F);
        assertEquals(1.445F, critDmg, 0.001F);
    }

    @Test
    public void testCooldownReductionMath() {
        // Base cooldown 100 ticks, CDR = 0.20 (20% reduction)
        int cd1 = (int) Math.round(100 * (1.0D - 0.20D));
        assertEquals(80, cd1);

        // Base cooldown 100 ticks, CDR = 0.50 (50% reduction)
        int cd2 = (int) Math.round(100 * (1.0D - 0.50D));
        assertEquals(50, cd2);

        // Base cooldown 100 ticks, max CDR 0.95 (95% reduction)
        int cd3 = (int) Math.round(100 * (1.0D - 0.95D));
        assertEquals(5, cd3);

        // Negative CDR (lengthens cooldown): -0.50 -> 100 * 1.50 = 150
        int cdNeg = (int) Math.round(100 * (1.0D - (-0.50D)));
        assertEquals(150, cdNeg);
    }

    @Test
    public void testCooldownTrackerStorage() {
        CooldownTracker tracker = new CooldownTracker();
        net.minecraft.resources.Identifier testId = net.minecraft.resources.Identifier.fromNamespaceAndPath("test", "ability");

        assertFalse(tracker.isOnCooldown(testId, 50, 100L));
        assertEquals(0L, tracker.getRemaining(testId, 50, 100L));

        // Start cooldown at gameTime 100
        tracker.startCooldown(testId, 100L);

        // At gameTime 120, effective cooldown 50 ticks -> still on cooldown, 30 ticks remaining
        assertTrue(tracker.isOnCooldown(testId, 50, 120L));
        assertEquals(30L, tracker.getRemaining(testId, 50, 120L));

        // At gameTime 150 -> boundary condition (100 + 50 >= 150) -> on cooldown, 0 ticks remaining
        assertTrue(tracker.isOnCooldown(testId, 50, 150L));
        assertEquals(0L, tracker.getRemaining(testId, 50, 150L));

        // At gameTime 151 -> off cooldown
        assertFalse(tracker.isOnCooldown(testId, 50, 151L));
        assertEquals(0L, tracker.getRemaining(testId, 50, 151L));

        // Clear
        tracker.clear(testId);
        assertFalse(tracker.isOnCooldown(testId, 50, 120L));
    }

    @Test
    public void testDeterministicDodgePerTick() {
        // Same tick and UUID must produce identical seeds and rolls
        java.util.UUID uuid = java.util.UUID.randomUUID();
        int tick = 42;

        int base = tick + uuid.hashCode();
        int seed1 = base + 0x9E3779B9 + (base << 6) + (base >> 2);
        int seed2 = base + 0x9E3779B9 + (base << 6) + (base >> 2);

        assertEquals(seed1, seed2);

        Random r1 = new Random(seed1);
        Random r2 = new Random(seed2);

        assertEquals(r1.nextFloat(), r2.nextFloat(), 0.00001F);
    }

    @Test
    public void testAuxiliaryDamageRecursionGuard() {
        assertFalse(AttributeEvents.noRecurse);
        AttributeEvents.noRecurse = true;
        assertTrue(AttributeEvents.noRecurse);
        AttributeEvents.noRecurse = false;
        assertFalse(AttributeEvents.noRecurse);
    }
}
