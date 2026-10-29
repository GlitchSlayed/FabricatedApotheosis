package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    /**
     * Removes the level-40 "Too Expensive!" cap in AnvilMenu.createResult.
     * In Minecraft 26.3, createResult has 3 occurrences of bipush 40:
     * - ordinal 0: when input count > 1
     * - ordinal 1: renaming cost cap
     * - ordinal 2: the level-40 cost check setting output item to EMPTY.
     * We modify ordinal 2 with require = 1 to guarantee mandatory behavior does not silently disappear.
     */
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2), require = 1)
    public int apoth_removeLevelCap(int old) {
        if (!ApotheosisConfig.getServer().enableEnchanting) {
            return old;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Redirects Enchantment.getMaxLevel() calls in AnvilMenu.createResult so that enchantments
     * can combine past vanilla max level up to Apothic's configured anvil combine level cap.
     */
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    public int apoth_allowHighLevelAnvilCombine(Enchantment ench) {
        if (!ApotheosisConfig.getServer().enableEnchanting) {
            return ench.getMaxLevel();
        }
        return EnchHooks.getMaxAnvilCombineLevel(ench);
    }

    /**
     * Reduces the XP cost to the "optimal" cost (the amount of XP that would have been used if the player had exactly that level).
     *
     * @param player The player using the anvil.
     * @param level  The negative of the cost of performing the anvil operation.
     */
    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    public void apoth_chargeOptimalLevels(Player player, int level) {
        if (!ApotheosisConfig.getServer().enableEnchanting) {
            player.giveExperienceLevels(level);
            return;
        }
        EnchantmentUtils.chargeExperience(player, EnchantmentUtils.getTotalExperienceForLevel(-level));
    }

}
