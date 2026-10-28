package dev.shadowsoffire.apothic_enchanting.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.shadowsoffire.apothic_enchanting.enchantments.ShearsEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(Sheep.class)
public abstract class SheepMixin {

    @WrapOperation(
        method = "shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/sheep/Sheep;dropFromShearingLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/item/ItemInstance;Ljava/util/function/BiConsumer;)V")
    )
    private void apoth_shearEnchantments(Sheep sheep, ServerLevel level, ResourceKey<LootTable> table, ItemInstance itemInstance, BiConsumer<ServerLevel, ItemStack> consumer, Operation<Void> original, ServerLevel origLevel, SoundSource soundSource, ItemStack shears) {
        List<ItemStack> drops = new ArrayList<>();
        original.call(sheep, level, table, itemInstance, (BiConsumer<ServerLevel, ItemStack>) (sl, drop) -> drops.add(drop));
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), shears);
        List<ItemStack> modified = ShearsEnchantments.applyFortune(sheep, shears, drops, fortune);
        modified = ShearsEnchantments.applyExploitation(sheep, shears, modified);
        modified = ShearsEnchantments.applyChromatic(sheep, shears, modified);
        for (ItemStack s : modified) {
            consumer.accept(level, s);
        }
        ShearsEnchantments.applyGrowthSerum(sheep, shears);
    }
}
