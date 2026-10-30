package dev.shadowsoffire.apotheosis.compat.enchanting;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.world.item.enchantment.Enchantment;

public class ApothicEnchantingCompat {

    public static final String MODID = "apothic_enchanting";

    public static boolean isLoaded() {
        return true;
    }

    public static int getMaxLevel(Enchantment ench) {
        return EnchHooks.getMaxLevel(ench);
    }
}
