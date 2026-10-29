package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

@Mixin(Mob.class)
public class MobTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void apoth_tickMovableNoAi(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (!self.level().isClientSide() && self.isNoAi() && AttributesUtil.getPersistentData(self).getBooleanOr("apotheosis:movable", false)) {
            self.setNoAi(false);
            self.travel(new Vec3(self.xxa, self.zza, self.yya));
            self.setNoAi(true);
        }
    }
}
