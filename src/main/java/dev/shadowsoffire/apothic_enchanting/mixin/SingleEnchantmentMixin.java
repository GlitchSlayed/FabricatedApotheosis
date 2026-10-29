package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.providers.SingleEnchantment;

@Mixin(SingleEnchantment.class)
public class SingleEnchantmentMixin {

    @Redirect(method = "enchant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private int apoth_singleEnchantmentMaxLootLevel(Enchantment ench) {
        return EnchHooks.getMaxLootLevel(((SingleEnchantment) (Object) this).enchantment());
    }
}
