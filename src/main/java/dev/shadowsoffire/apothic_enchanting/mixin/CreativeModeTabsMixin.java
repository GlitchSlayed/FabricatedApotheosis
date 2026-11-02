package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {

    @Redirect(method = {
        "lambda$generateEnchantmentBookTypesOnlyMaxLevel$0",
        "lambda$generateEnchantmentBookTypesAllLevels$0"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private static int apoth_creativeMaxLevel(Enchantment ench) {
        return EnchHooks.getMaxLevel(ench);
    }
}
