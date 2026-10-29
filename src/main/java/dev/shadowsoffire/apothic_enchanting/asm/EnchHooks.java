package dev.shadowsoffire.apothic_enchanting.asm;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.util.MiscUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Hook methods replacing upstream bytecode coremod redirections.
 */
public class EnchHooks {

    /**
     * Replaces calls to {@link Enchantment#getMaxLevel()} when resolving max enchantment level.
     */
    public static int getMaxLevel(Holder<Enchantment> ench) {
        if (ench != null) {
            return ApothicEnchanting.getEnchInfo(ench).getMaxLevel(ench);
        }
        return 1;
    }

    /**
     * Fallback overload resolving the holder via active registry context.
     */
    public static int getMaxLevel(Enchantment ench) {
        Holder<Enchantment> holder = MiscUtil.findHolder(Registries.ENCHANTMENT, ench);
        if (holder != null) {
            return ApothicEnchanting.getEnchInfo(holder).getMaxLevel(holder);
        }
        return ench.getMaxLevel();
    }

    /**
     * Replaces calls to {@link Enchantment#getMaxLevel()} in loot-only classes.
     */
    public static int getMaxLootLevel(Holder<Enchantment> ench) {
        if (ench != null) {
            return ApothicEnchanting.getEnchInfo(ench).getMaxLootLevel(ench);
        }
        return 1;
    }

    /**
     * Fallback overload resolving the holder via active registry context for loot generation.
     */
    public static int getMaxLootLevel(Enchantment ench) {
        Holder<Enchantment> holder = MiscUtil.findHolder(Registries.ENCHANTMENT, ench);
        if (holder != null) {
            return ApothicEnchanting.getEnchInfo(holder).getMaxLootLevel(holder);
        }
        return ench.getMaxLevel();
    }

    /**
     * Returns the maximum level up to which an enchantment can be combined in an anvil.
     */
    public static int getMaxAnvilCombineLevel(Holder<Enchantment> ench) {
        if (ench != null) {
            return ApothicEnchanting.getEnchInfo(ench).getMaxAnvilCombineLevel(ench);
        }
        return 1;
    }

    /**
     * Fallback overload for anvil combination level cap.
     */
    public static int getMaxAnvilCombineLevel(Enchantment ench) {
        Holder<Enchantment> holder = MiscUtil.findHolder(Registries.ENCHANTMENT, ench);
        if (holder != null) {
            return ApothicEnchanting.getEnchInfo(holder).getMaxAnvilCombineLevel(holder);
        }
        return ench.getMaxLevel();
    }

    /**
     * Calculates the delay for catching a fish. Ensures that the value never returns <= 0,
     * so that the bobber doesn't get infinitely locked.
     */
    public static int getTicksCaughtDelay(FishingHook bobber) {
        int lowBound = Math.max(1, 100 - bobber.lureSpeed);
        int highBound = Math.max(lowBound, 600 - bobber.lureSpeed);
        return Mth.nextInt(bobber.level().getRandom(), lowBound, highBound);
    }
}
