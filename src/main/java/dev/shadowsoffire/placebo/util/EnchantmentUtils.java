package dev.shadowsoffire.placebo.util;

import net.minecraft.world.entity.player.Player;

/**
 * Utility functions for experience and level calculations.
 */
public class EnchantmentUtils {

    public static boolean chargeExperience(Player player, int cost) {
        int exp = getExperience(player);
        if (exp >= cost) {
            player.giveExperiencePoints(-cost);
            if (getExperience(player) <= 0) {
                player.experienceProgress = 0.0F;
            }
            return true;
        }
        return false;
    }

    public static int getExperience(Player player) {
        int exp = getTotalExperienceForLevel(player.experienceLevel);
        exp += (int) (player.experienceProgress * getExperienceForLevel(player.experienceLevel + 1));
        return exp;
    }

    public static int getExperienceForLevel(int level) {
        if (level <= 0) return 0;
        if (level > 30) return 112 + (level - 31) * 9;
        if (level > 15) return 37 + (level - 16) * 5;
        return 7 + (level - 1) * 2;
    }

    public static int getTotalExperienceForLevel(int level) {
        if (level <= 0) return 0;
        if (level > 30) return (int) (4.5 * level * level - 162.5 * level + 2220);
        if (level > 15) return (int) (2.5 * level * level - 40.5 * level + 360);
        return level * level + 6 * level;
    }

    public static int getLevelForExperience(int targetExp) {
        int level = 0;
        while (true) {
            int nextExp = getExperienceForLevel(level + 1);
            if (targetExp < nextExp) return level;
            level++;
            targetExp -= nextExp;
        }
    }
}
