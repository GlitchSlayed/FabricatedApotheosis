package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.AntiGravityArrowBonus;
import dev.shadowsoffire.apotheosis.util.EntityDropCapture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @WrapOperation(
        method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean apoth$interceptDeathDrop(ServerLevel level, Entity entity, Operation<Boolean> original) {
        EntityDropCapture.DropCaptureContext ctx = EntityDropCapture.peekCaptureContext();
        if (ctx != null && (Object) this == ctx.entity && entity instanceof ItemEntity item) {
            ctx.drops.add(item);
            return true;
        }
        return original.call(level, entity);
    }

    @ModifyVariable(at = @At("HEAD"), method = "checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", argsOnly = true)
    public double apoth_checkFallDamageWithGravity(double yMotion, double motion, boolean onGround, BlockState state, BlockPos pos) {
        if (yMotion < 0 && (Object) this instanceof LivingEntity le) {
            double gravity = le.getAttributeValue(Attributes.GRAVITY);
            yMotion *= gravity / 0.08D;
            if (gravity <= 0.01D) {
                yMotion = 0;
            }
        }
        return yMotion;
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void apoth_entityTick(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide()) {
            if (self instanceof AbstractArrow arrow && arrow.hasAttached(Apoth.Attachments.ANTI_GRAVITY_ARROW_START)) {
                AntiGravityArrowBonus.tick(arrow);
            }
            if (AttributesUtil.getPersistentData(self).getBooleanOr("apoth.burns_in_sun", false)) {
                if (self.level().isBrightOutside()) {
                    float light = self.getLightLevelDependentMagicValue();
                    BlockPos pos = BlockPos.containing(self.getX(), self.getEyeY(), self.getZ());
                    boolean inWaterOrSnow = self.isInWaterOrRain() || self.isInPowderSnow || self.wasInPowderSnow;
                    if (light > 0.5F && self.getRandom().nextFloat() * 30.0F < (light - 0.4F) * 2.0F && !inWaterOrSnow && self.level().canSeeSky(pos)) {
                        self.setRemainingFireTicks(160);
                    }
                }
            }
        }
    }

    @Inject(method = "isInvulnerableToBase", at = @At("HEAD"), cancellable = true)
    private void apoth_preventBossSuffocate(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (source.is(DamageTypes.IN_WALL) && AttributesUtil.getPersistentData(self).contains("apoth.boss")) {
            cir.setReturnValue(true);
        }
    }
}
