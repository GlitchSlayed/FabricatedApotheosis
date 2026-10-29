package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerItem;
import net.minecraft.references.BlockItemId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

@Mixin(Items.class)
public class ItemsMixin {

    @Redirect(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Items;registerBlock(Lnet/minecraft/references/BlockItemId;Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/item/Item;"),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/references/BlockItemIds;SPAWNER:Lnet/minecraft/references/BlockItemId;"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/references/BlockItemIds;CREAKING_HEART:Lnet/minecraft/references/BlockItemId;")),
        require = 1)
    private static Item apoth_overrideSpawnerItem(BlockItemId id, Block block) {
        return Items.registerBlock(id, block, ApothSpawnerItem::new);
    }
}
