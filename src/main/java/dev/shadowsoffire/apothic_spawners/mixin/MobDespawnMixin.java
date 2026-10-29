package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public class MobDespawnMixin {

    @Inject(
        method = "checkDespawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;isPersistenceRequired()Z"),
        cancellable = true)
    private void apoth_preventSpawnerDespawn(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (AttributesUtil.getPersistentData(self).getBooleanOr("apothic_spawners:from_spawner", false) && ASConfig.entityDespawnDelay >= self.tickCount) {
            ci.cancel();
        }
    }
}
