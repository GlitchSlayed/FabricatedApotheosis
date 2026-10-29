package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @WrapOperation(
        method = "addDetailsToTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"
        )
    )
    private boolean apoth_skipVanillaSpawnerTooltip(ItemStack self, Object predicate, Operation<Boolean> original) {
        if (predicate == Items.SPAWNER && self.getItem() instanceof ApothSpawnerItem) {
            return false;
        }
        return original.call(self, predicate);
    }
}
