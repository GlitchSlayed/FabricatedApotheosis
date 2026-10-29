package dev.shadowsoffire.apothic_spawners.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;

@Mixin(Entity.class)
public class EntityTeleportMixin {

    @Inject(
        method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z",
        at = @At("HEAD"),
        cancellable = true)
    private void apoth_cancelMovableTeleport(ServerLevel serverLevel, double d, double e, double f, Set<Relative> set, float g, float h, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (AttributesUtil.getPersistentData(self).getBooleanOr("apotheosis:movable", false)) {
            cir.setReturnValue(false);
        }
    }
}
