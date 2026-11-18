package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apotheosis.mobs.ApothMobEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {

    @Inject(method = "createNewDragon", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void apoth_finalizeEnderDragons(CallbackInfoReturnable<EnderDragon> ci, @Local EnderDragon dragon) {
        var cfg = ApotheosisConfig.getServer();
        if (!cfg.enableAdventure || !cfg.applyWorldTierToEndAndWither) {
            return;
        }
        if (dragon.level() instanceof ServerLevelAccessor level) {
            DifficultyInstance difficulty = level.getCurrentDifficultyAt(dragon.blockPosition());
            ApothMobEvents.onFinalizeSpawn(dragon, level, difficulty, EntitySpawnReason.EVENT, null);
        }
    }

}
