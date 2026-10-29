package dev.shadowsoffire.apothic_spawners.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;

@Mixin(AbstractCubeMob.class)
public class SlimeSplitMixin {

    @Inject(method = "setUpSplitCube", at = @At("HEAD"))
    private void apoth_inheritMovableNoAi(AbstractCubeMob child, int i, float f, float g, CallbackInfo ci) {
        AbstractCubeMob self = (AbstractCubeMob) (Object) this;
        if (self.isNoAi() && AttributesUtil.getPersistentData(self).getBooleanOr("apotheosis:movable", false)) {
            AttributesUtil.getPersistentData(child).putBoolean("apotheosis:movable", true);
            child.setNoAi(true);
        }
    }
}
