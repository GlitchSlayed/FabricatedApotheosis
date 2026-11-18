package dev.shadowsoffire.apotheosis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

/**
 * Mixin to {@link AbstractSkeletonRenderer} to display crossbow hold/charge poses for crossbow-wielding skeletons.
 */
@Mixin(AbstractSkeletonRenderer.class)
public abstract class AbstractSkeletonRendererMixin {

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void apoth_overrideCrossbowPose(AbstractSkeleton mob, HumanoidArm arm, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (mob.getMainArm() != arm || !mob.isAggressive()) {
            return;
        }

        ItemStack held = mob.getMainHandItem();
        if (!(held.getItem() instanceof CrossbowItem)) {
            return;
        }

        if (mob.isUsingItem() && mob.getUseItem() == held) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_CHARGE);
        } else if (CrossbowItem.isCharged(held)) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}
