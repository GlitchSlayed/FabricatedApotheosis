package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * Upgrades the vanilla jump-critical damage multiplier from 1.5x to max(1.5, critDamage).
     */
    @ModifyConstant(method = "attack(Lnet/minecraft/world/entity/Entity;)V", constant = @Constant(floatValue = 1.5F))
    private float apoth_modifyVanillaCritMultiplier(float constant) {
        Player self = (Player) (Object) this;
        return Math.max(constant, (float) self.getAttributeValue(ALObjects.Attributes.CRIT_DAMAGE));
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), method = "attack(Lnet/minecraft/world/entity/Entity;)V")
    private boolean apoth_handleKilledByAuxDmg(Entity target, DamageSource src, float dmg, Operation<Boolean> wrapped) {
        boolean res = wrapped.call(target, src, dmg);
        return res || AttributesUtil.getPersistentData(target).getBooleanOr("apoth.killed_by_aux_dmg", false);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"), method = "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;F)V")
    private boolean apoth_markSweepAttacks(LivingEntity target, ServerLevel level, DamageSource src, float dmg, Operation<Boolean> wrapped) {
        AttributesUtil.getPersistentData(target).putBoolean("apoth.hit_by_sweep_attack", true);
        boolean res = wrapped.call(target, level, src, dmg);
        AttributesUtil.getPersistentData(target).remove("apoth.hit_by_sweep_attack");
        return res;
    }

    @WrapOperation(method = "maybeBackOffFromEdge", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;maxUpStep()F"))
    private float apoth_dontFallOffACliff(Player player, Operation<Float> original) {
        return Math.min(original.call(player), 0.6F);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void apoth_creativeFlightTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!self.level().isClientSide()) {
            boolean canFly = self.hasEffect(ALObjects.MobEffects.FLYING);
            if (canFly) {
                if (!self.getAbilities().mayfly) {
                    self.getAbilities().mayfly = true;
                    self.onUpdateAbilities();
                    AttributesUtil.getPersistentData(self).putBoolean("apoth.had_flight_granted", true);
                }
            } else if (AttributesUtil.getPersistentData(self).getBooleanOr("apoth.had_flight_granted", false)) {
                AttributesUtil.getPersistentData(self).remove("apoth.had_flight_granted");
                if (!self.isCreative() && !self.isSpectator()) {
                    self.getAbilities().mayfly = false;
                    self.getAbilities().flying = false;
                    self.onUpdateAbilities();
                }
            }
        }
    }
}
