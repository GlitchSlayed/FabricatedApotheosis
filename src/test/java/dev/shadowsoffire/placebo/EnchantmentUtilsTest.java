package dev.shadowsoffire.placebo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.shadowsoffire.placebo.util.EnchantmentUtils;

public class EnchantmentUtilsTest {

    @Test
    public void testExperienceCalculations() {
        assertEquals(0, EnchantmentUtils.getExperienceForLevel(0));
        assertEquals(7, EnchantmentUtils.getExperienceForLevel(1));
        assertEquals(9, EnchantmentUtils.getExperienceForLevel(2));
        assertEquals(37, EnchantmentUtils.getExperienceForLevel(16));
        assertEquals(112, EnchantmentUtils.getExperienceForLevel(31));

        // Total experience for level 30 is 1395 points in vanilla
        assertEquals(1395, EnchantmentUtils.getTotalExperienceForLevel(30));

        // Inverse calculation: 1395 points should yield level 30
        assertEquals(30, EnchantmentUtils.getLevelForExperience(1395));
        assertEquals(0, EnchantmentUtils.getLevelForExperience(0));
        assertEquals(1, EnchantmentUtils.getLevelForExperience(7));
    }
}
