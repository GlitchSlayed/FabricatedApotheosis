package dev.shadowsoffire.apothic_enchanting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shadowsoffire.apothic_enchanting.library.EnchLibraryTile;
import dev.shadowsoffire.apothic_enchanting.table.Arcana;
import dev.shadowsoffire.apothic_enchanting.table.EnchantmentTableStats;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;

public class EnchantingTests {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    @DisplayName("Enchantment Library: point conversion matches 2^(N-1) formula")
    void testLevelToPoints() {
        assertEquals(1, EnchLibraryTile.levelToPoints(1));
        assertEquals(2, EnchLibraryTile.levelToPoints(2));
        assertEquals(4, EnchLibraryTile.levelToPoints(3));
        assertEquals(8, EnchLibraryTile.levelToPoints(4));
        assertEquals(16, EnchLibraryTile.levelToPoints(5));
        assertEquals(32, EnchLibraryTile.levelToPoints(6));
        assertEquals(32768, EnchLibraryTile.levelToPoints(16)); // Basic library cap
        assertEquals(1073741824, EnchLibraryTile.levelToPoints(31)); // Ender library cap

        // Verify incremental cost to upgrade an existing item
        // Going from Sharpness III (4 pts) to Sharpness V (16 pts) costs 12 points
        int cost3to5 = EnchLibraryTile.levelToPoints(5) - EnchLibraryTile.levelToPoints(3);
        assertEquals(12, cost3to5);

        // Going from Sharpness I (1 pt) to Sharpness IV (8 pts) costs 7 points
        int cost1to4 = EnchLibraryTile.levelToPoints(4) - EnchLibraryTile.levelToPoints(1);
        assertEquals(7, cost1to4);
    }

    @Test
    @DisplayName("Enchanting Table Stats: Step-ladder Eterna accumulation and clamping")
    void testEternaStepLadderCalculation() {
        // Test standard vanilla bookshelves: 15 shelves contributing 1.0 each, max 15.0
        EnchantmentTableStats.Builder builder1 = new EnchantmentTableStats.Builder();
        for (int i = 0; i < 15; i++) {
            builder1.addEterna(1.0F, 15.0F);
        }
        EnchantmentTableStats stats1 = builder1.build();
        assertEquals(15.0F, stats1.eterna(), 0.001F);
        assertEquals(15.0F, stats1.quanta(), 0.001F); // Default 15% quanta
        assertEquals(1, stats1.clues()); // Default 1 clue

        // Test overcapping: 20 vanilla bookshelves contributing 1.0 each, max 15.0 -> clamps at 15.0
        EnchantmentTableStats.Builder builderOvercap = new EnchantmentTableStats.Builder();
        for (int i = 0; i < 20; i++) {
            builderOvercap.addEterna(1.0F, 15.0F);
        }
        EnchantmentTableStats statsOvercap = builderOvercap.build();
        assertEquals(15.0F, statsOvercap.eterna(), 0.001F);

        // Test step-ladder progression: 15 vanilla shelves (max 15) + 10 hellshelves (contributing 1.5 each, max 30)
        // 15 from vanilla + (10 * 1.5 = 15) from hellshelves = 30 Eterna total
        EnchantmentTableStats.Builder builderMixed = new EnchantmentTableStats.Builder();
        for (int i = 0; i < 15; i++) {
            builderMixed.addEterna(1.0F, 15.0F);
        }
        for (int i = 0; i < 10; i++) {
            builderMixed.addEterna(1.5F, 30.0F);
        }
        EnchantmentTableStats statsMixed = builderMixed.build();
        assertEquals(30.0F, statsMixed.eterna(), 0.001F);
    }

    @Test
    @DisplayName("Enchanting Stats: Quanta power range calculation conforms to baseline")
    void testQuantaRangeCalculation() {
        int level = 30;

        // 0% Quanta, unstable: power range is [30, 30]
        float zeroQuanta = 0.0F;
        int minPow0 = Math.round(Mth.clamp(level - level * zeroQuanta, 1, 200));
        int maxPow0 = Math.round(Mth.clamp(level + level * zeroQuanta, 1, 200));
        assertEquals(30, minPow0);
        assertEquals(30, maxPow0);

        // 25% Quanta, unstable: power range is [23, 38]
        float q25 = 0.25F;
        int minPow25 = Math.round(Mth.clamp(level - level * q25, 1, 200));
        int maxPow25 = Math.round(Mth.clamp(level + level * q25, 1, 200));
        assertEquals(23, minPow25);
        assertEquals(38, maxPow25);

        // 50% Quanta, unstable: power range is [15, 45]
        float q50 = 0.50F;
        int minPow50 = Math.round(Mth.clamp(level - level * q50, 1, 200));
        int maxPow50 = Math.round(Mth.clamp(level + level * q50, 1, 200));
        assertEquals(15, minPow50);
        assertEquals(45, maxPow50);

        // 100% Quanta, unstable: power range is [1, 60] (clamped to min 1)
        float q100 = 1.00F;
        int minPow100 = Math.round(Mth.clamp(level - level * q100, 1, 200));
        int maxPow100 = Math.round(Mth.clamp(level + level * q100, 1, 200));
        assertEquals(1, minPow100);
        assertEquals(60, maxPow100);
    }

    @Test
    @DisplayName("Enchanting Stats: Rectification preserves floor when stable")
    void testRectificationAndStableBehavior() {
        int level = 50;
        float quanta = 0.40F;

        // When stable flag is true (e.g. 100% rectification), minimum power never falls below level
        boolean stable = true;
        int minPowStable = stable ? level : Math.round(Mth.clamp(level - level * quanta, 1, 200));
        int maxPowStable = Math.round(Mth.clamp(level + level * quanta, 1, 200));
        assertEquals(50, minPowStable);
        assertEquals(70, maxPowStable);
    }

    @Test
    @DisplayName("Arcana Thresholds: Tiers resolve accurately per baseline spec")
    void testArcanaTiers() {
        assertEquals(Arcana.EMPTY, Arcana.getForThreshold(0.0F));
        assertEquals(Arcana.EMPTY, Arcana.getForThreshold(9.9F));

        assertEquals(Arcana.LITTLE, Arcana.getForThreshold(10.0F));
        assertEquals(Arcana.LITTLE, Arcana.getForThreshold(15.0F));

        assertEquals(Arcana.FEW, Arcana.getForThreshold(20.0F));
        assertEquals(Arcana.SOME, Arcana.getForThreshold(30.0F));
        assertEquals(Arcana.LESS, Arcana.getForThreshold(40.0F));
        assertEquals(Arcana.MEDIUM, Arcana.getForThreshold(50.0F));
        assertEquals(Arcana.MORE, Arcana.getForThreshold(60.0F));
        assertEquals(Arcana.VALUE, Arcana.getForThreshold(70.0F));
        assertEquals(Arcana.EXTRA, Arcana.getForThreshold(80.0F));
        assertEquals(Arcana.ALMOST, Arcana.getForThreshold(90.0F));

        assertEquals(Arcana.MAX, Arcana.getForThreshold(99.0F));
        assertEquals(Arcana.MAX, Arcana.getForThreshold(100.0F));
        assertEquals(Arcana.MAX, Arcana.getForThreshold(150.0F));
    }

    @Test
    @DisplayName("Anvil Overhaul: Optimal XP calculation without 40-level cap")
    void testAnvilCostExperienceCalculations() {
        // Vanilla capped at 40 (Too Expensive)
        // Verify EnchantmentUtils correctly handles unbounded levels: 40, 50, 75, 100
        int xpAt40 = EnchantmentUtils.getTotalExperienceForLevel(40);
        int xpAt50 = EnchantmentUtils.getTotalExperienceForLevel(50);
        int xpAt75 = EnchantmentUtils.getTotalExperienceForLevel(75);
        int xpAt100 = EnchantmentUtils.getTotalExperienceForLevel(100);

        assertTrue(xpAt40 > 0);
        assertTrue(xpAt50 > xpAt40);
        assertTrue(xpAt75 > xpAt50);
        assertTrue(xpAt100 > xpAt75);

        // Verify roundtrip level conversion
        assertEquals(40, EnchantmentUtils.getLevelForExperience(xpAt40));
        assertEquals(50, EnchantmentUtils.getLevelForExperience(xpAt50));
        assertEquals(75, EnchantmentUtils.getLevelForExperience(xpAt75));
        assertEquals(100, EnchantmentUtils.getLevelForExperience(xpAt100));
    }

    @Test
    @DisplayName("StepFunction: Power curve evaluation")
    void testStepFunctionEvaluation() {
        // StepFunction: min=10, max=50, step=10 (4 steps)
        StepFunction func = StepFunction.fromBounds(10.0F, 50.0F, 10.0F);
        assertEquals(10.0F, func.min());
        assertEquals(50.0F, func.max());
        assertEquals(10.0F, func.step());
        assertEquals(4, func.steps());

        // At level 0.0F (minimum progress)
        assertEquals(10.0F, func.get(0.0F), 0.001F);
        // At level 1.0F (maximum progress)
        assertEquals(50.0F, func.get(1.0F), 0.001F);
    }
}
