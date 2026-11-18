package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.apotheosis.mobs.ApothMobEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), cancellable = true)
    private void apoth$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (!ApothMobEvents.onFinalizeSpawn((Mob) (Object) this, level, difficulty, reason, spawnData)) {
            cir.setReturnValue(null);
        }
    }

    /**
     * Allows bosses that descend from {@link AbstractGolem} to despawn naturally after 10 minutes.
     */
    @Inject(method = "checkDespawn()V", at = @At("HEAD"), cancellable = true)
    private void apoth_checkGolemBossDespawn(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        int despawnTicks = dev.shadowsoffire.apotheosis.config.ApotheosisConfig.getServer().golemBossDespawnTicks;
        if (self instanceof AbstractGolem g && !g.level().isClientSide() && g.tickCount > despawnTicks && AttributesUtil.getPersistentData(g).getBooleanOr("apoth.boss", false)) {
            Entity player = g.level().getNearestPlayer(g, -1.0D);
            if (player != null) {
                double dist = player.distanceToSqr(g);
                int despawnDist = g.getType().getCategory().getDespawnDistance();
                int dsDistSq = despawnDist * despawnDist;
                if (dist > dsDistSq) {
                    g.discard();
                    ci.cancel();
                }
            }
        }
    }
}
