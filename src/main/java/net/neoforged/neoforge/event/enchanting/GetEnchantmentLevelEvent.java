package net.neoforged.neoforge.event.enchanting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class GetEnchantmentLevelEvent {

    private final ItemStack stack;
    private final ItemEnchantments.Mutable enchantments;

    public GetEnchantmentLevelEvent(ItemStack stack, ItemEnchantments.Mutable enchantments) {
        this.stack = stack;
        this.enchantments = enchantments;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public ItemEnchantments.Mutable getEnchantments() {
        return this.enchantments;
    }
}
