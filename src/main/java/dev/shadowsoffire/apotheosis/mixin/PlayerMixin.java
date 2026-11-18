package dev.shadowsoffire.apotheosis.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.shadowsoffire.apotheosis.affix.effect.OmneticAffix;
import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.OmneticBonus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * Upgrades the vanilla jump-critical damage multiplier from 1.5x to max(1.5, critDamage).
     */
    @ModifyConstant(method = "attack(Lnet/minecraft/world/entity/Entity;)V", constant = @Constant(floatValue = 1.5F))
    private float apoth_modifyVanillaCritMultiplier(float constant) {
        Player self = (Player) (Object) this;
        float critVal = (float) self.getAttributeValue(ALObjects.Attributes.CRIT_DAMAGE);
        float cap = dev.shadowsoffire.apotheosis.config.ApotheosisConfig.getServer().critDamageModifierCap;
        return Math.min(cap, Math.max(constant, critVal));
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

    @Unique
    private static boolean apoth$hasExternalFlightGrant(Player player) {
        if (player.isCreative() || player.isSpectator()) return true;
        for (java.util.function.Predicate<Player> predicate : AttributesUtil.EXTERNAL_FLIGHT_PREDICATES) {
            if (predicate.test(player)) return true;
        }
        return false;
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void apoth_creativeFlightTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!self.level().isClientSide()) {
            boolean canFly = self.hasEffect(ALObjects.MobEffects.FLYING)
                || (self.getAttributes().hasAttribute(ALObjects.Attributes.CREATIVE_FLIGHT) && self.getAttributeValue(ALObjects.Attributes.CREATIVE_FLIGHT) > 0);
            if (canFly) {
                if (!self.getAbilities().mayfly) {
                    self.getAbilities().mayfly = true;
                    self.onUpdateAbilities();
                    AttributesUtil.getPersistentData(self).putBoolean("apoth.had_flight_granted", true);
                }
            } else if (AttributesUtil.getPersistentData(self).getBooleanOr("apoth.had_flight_granted", false)) {
                AttributesUtil.getPersistentData(self).remove("apoth.had_flight_granted");
                if (!apoth$hasExternalFlightGrant(self)) {
                    self.getAbilities().mayfly = false;
                    self.getAbilities().flying = false;
                    self.onUpdateAbilities();
                }
            }
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void apoth_omneticDestroySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player self = (Player) (Object) this;
        float speed = cir.getReturnValueF();
        PlayerEvent.BreakSpeed event = new PlayerEvent.BreakSpeed(self, state, speed, Optional.empty());
        OmneticAffix.speed(event);
        OmneticBonus.speed(event);
        if (event.getNewSpeed() != speed) {
            cir.setReturnValue(event.getNewSpeed());
        }
    }

    @Inject(method = "hasCorrectToolForDrops", at = @At("RETURN"), cancellable = true)
    private void apoth_omneticHarvestCheck(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            Player self = (Player) (Object) this;
            PlayerEvent.HarvestCheck event = new PlayerEvent.HarvestCheck(self, state, false);
            OmneticAffix.harvest(event);
            OmneticBonus.harvest(event);
            if (event.canHarvest()) {
                cir.setReturnValue(true);
            }
        }
    }
}
