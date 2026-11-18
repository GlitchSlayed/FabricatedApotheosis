package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apotheosis.mobs.ApothMobEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

/**
 * Triggers Apotheosis spawn finalization for withers when they are constructed using the usual structure.
 */
@Mixin(WitherSkullBlock.class)
public class WitherSkullBlockMixin {

    @Inject(method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static void apoth_finalizeWithers(Level level, BlockPos pos, SkullBlockEntity be, CallbackInfo ci, @Local WitherBoss wither) {
        if (!ApotheosisConfig.getServer().enableAdventure) {
            return;
        }
        if (level instanceof ServerLevelAccessor accessor) {
            DifficultyInstance difficulty = accessor.getCurrentDifficultyAt(pos);
            ApothMobEvents.onFinalizeSpawn(wither, accessor, difficulty, EntitySpawnReason.TRIGGERED, null);
        }
    }

}
