package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

@Mixin(BlockEntityTypes.class)
public class BlockEntityTypesMixin {

    @ModifyArg(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BlockEntityTypes;register(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType;"),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BlockEntityTypeIds;MOB_SPAWNER:Lnet/minecraft/resources/ResourceKey;"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BlockEntityTypeIds;CREAKING_HEART:Lnet/minecraft/resources/ResourceKey;")),
        index = 1,
        require = 1)
    private static BlockEntityType.BlockEntitySupplier<?> apoth_overrideSpawnerSupplier(BlockEntityType.BlockEntitySupplier<?> original) {
        return ApothSpawnerTile::new;
    }
}
