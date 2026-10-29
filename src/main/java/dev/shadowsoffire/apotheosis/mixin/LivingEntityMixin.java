package dev.shadowsoffire.apotheosis.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apotheosis.attributes.api.ALCombatRules;
import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributeEvents;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.apotheosis.attributes.api.LEInvoker;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LEInvoker {

    @Shadow
    protected abstract void internalSetAbsorptionAmount(float absorptionAmount);

    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> ef);

    @Shadow
    public abstract MobEffectInstance getEffect(Holder<MobEffect> ef);

    @Shadow
    public abstract float getHealth();

    @Shadow
    protected int useItemRemaining;

    @Unique
    private float initialMagicDamage = 0.0F;

    @Unique
    private float preDamageHealth = 0.0F;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void apoth_setInternalAbsorption(float absorptionAmount) {
        this.internalSetAbsorptionAmount(absorptionAmount);
    }

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("HEAD"))
    private void apoth_captureInitialMagicDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Float> cir) {
        this.initialMagicDamage = amount;
    }

    @ModifyVariable(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageProtection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)F"), argsOnly = true)
    private float apoth_applySundering(float amount, DamageSource damageSource) {
        if (this.hasEffect(ALObjects.MobEffects.SUNDERING) && !damageSource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            int level = this.getEffect(ALObjects.MobEffects.SUNDERING).getAmplifier() + 1;
            amount += this.initialMagicDamage * level * 0.2F;
        }
        return Math.max(0.0F, amount);
    }

    @Redirect(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterMagicAbsorb(FF)F"))
    private float apoth_applyProtPen(float amount, float protPoints, DamageSource src) {
        return ALCombatRules.getDamageAfterProtection((LivingEntity) (Object) this, src, amount, protPoints);
    }

    @Inject(method = "canGlide()Z", at = @At("HEAD"), cancellable = true)
    private void apoth_canGlideFromAttribute(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getAttributeValue(ALObjects.Attributes.ELYTRA_FLIGHT) > 0
            && !self.onGround()
            && !self.isPassenger()
            && !self.hasEffect(MobEffects.LEVITATION)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canGlideUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z", at = @At("HEAD"), cancellable = true)
    private static void apoth_canGlideUsingFromAttribute(ItemStack stack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (mods == null) return;
        for (ItemAttributeModifiers.Entry entry : mods.modifiers()) {
            if (entry.attribute().equals(ALObjects.Attributes.ELYTRA_FLIGHT)
                && entry.slot().test(slot)
                && entry.modifier().amount() > 0) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "updateFallFlying()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getRandom(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/lang/Object;"), cancellable = true)
    private void apoth_skipEmptyGliderDamage(CallbackInfo ci, @Local List<EquipmentSlot> slotsWithGliders) {
        if (slotsWithGliders.isEmpty()) {
            ci.cancel();
            this.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
    }

    @ModifyReturnValue(method = "getMaxAbsorption()F", at = @At("RETURN"))
    private float apoth_overhealMaxAbsorption(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) {
            return Math.max(original, self.getAbsorptionAmount());
        }
        return original + AttributesUtil.getOverhealAbsorption(self);
    }

    @Inject(method = "setAbsorptionAmount(F)V", at = @At("TAIL"))
    private void apoth_trimOverhealAbsorption(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide() && self.hasAttached(ALObjects.Attachments.OVERHEAL_ABSORPTION)) {
            float tracked = self.getAttached(ALObjects.Attachments.OVERHEAL_ABSORPTION);
            float current = self.getAbsorptionAmount();
            if (tracked > current) {
                AttributesUtil.setOverhealAbsorption(self, current);
            }
        }
    }

    @Inject(method = "heal(F)V", at = @At("HEAD"), cancellable = true)
    private void apoth_cancelZeroHeal(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        float factor = (float) self.getAttributeValue(ALObjects.Attributes.HEALING_RECEIVED);
        if (amount * factor <= 0.0F) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "heal(F)V", at = @At("HEAD"), argsOnly = true)
    private float apoth_scaleHealingReceived(float amount) {
        LivingEntity self = (LivingEntity) (Object) this;
        float factor = (float) self.getAttributeValue(ALObjects.Attributes.HEALING_RECEIVED);
        return amount * factor;
    }

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void apoth_checkDodge(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (AttributeEvents.checkMeleeDodge(self, damageSource)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), argsOnly = true)
    private float apoth_scaleAttackDamage(float amount, ServerLevel level, DamageSource damageSource) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (damageSource.getDirectEntity() instanceof Projectile && damageSource.getEntity() instanceof LivingEntity projOwner) {
            amount *= (float) projOwner.getAttributeValue(ALObjects.Attributes.PROJECTILE_DAMAGE);
        }
        if (damageSource.getEntity() instanceof LivingEntity attacker && !damageSource.is(ALObjects.Tags.CANNOT_CRITICALLY_STRIKE)) {
            amount = AttributeEvents.rollCriticalStrikes(attacker, self, amount);
        }
        return amount;
    }

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("TAIL"))
    private void apoth_applyAuxDamage(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            LivingEntity self = (LivingEntity) (Object) this;
            AttributeEvents.applyMeleeAuxDamage(self, damageSource);
        }
    }

    @Inject(method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("HEAD"))
    private void apoth_capturePreDamageHealth(ServerLevel level, DamageSource damageSource, float amount, CallbackInfo ci) {
        this.preDamageHealth = this.getHealth();
    }

    @Inject(method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("TAIL"))
    private void apoth_applyLifeStealOverheal(ServerLevel level, DamageSource damageSource, float amount, CallbackInfo ci) {
        float healthDamage = Math.min(this.preDamageHealth, Math.max(0.0F, this.preDamageHealth - this.getHealth()));
        if (healthDamage > 0.001F && damageSource.getDirectEntity() instanceof LivingEntity attacker && AttributesUtil.isPhysicalDamage(damageSource)) {
            float lifesteal = (float) attacker.getAttributeValue(ALObjects.Attributes.LIFE_STEAL);
            if (lifesteal > 0.001F) {
                attacker.heal(healthDamage * lifesteal);
            }
            float overheal = (float) attacker.getAttributeValue(ALObjects.Attributes.OVERHEAL);
            if (overheal > 0.0F) {
                float cap = AttributesUtil.getOverhealCap(attacker);
                float current = attacker.getAbsorptionAmount();
                if (current < cap) {
                    float gain = Math.min(cap - current, healthDamage * overheal);
                    AttributesUtil.setOverhealAbsorption(attacker, AttributesUtil.getOverhealAbsorption(attacker) + gain);
                    ((LEInvoker) attacker).apoth_setInternalAbsorption(current + gain);
                }
            }
        }
    }

    @Inject(method = "updateUsingItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void apoth_applyDrawSpeed(ItemStack stack, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof net.minecraft.world.entity.player.Player player) {
            int offset = AttributeEvents.computeDrawSpeedOffset(player, stack);
            this.useItemRemaining += offset;
        }
    }

    @ModifyReturnValue(method = "getExperienceReward(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I", at = @At("RETURN"))
    private int apoth_scaleMobExperience(int original, ServerLevel level, Entity attackingEntity) {
        LivingEntity self = (LivingEntity) (Object) this;
        int echoes = AttributesUtil.getPersistentData(self).getIntOr(dev.shadowsoffire.apothic_spawners.stats.SpawnerStats.ECHOING.getId().toString(), 0);
        if (echoes > 0) {
            original = original * (1 + echoes);
        }
        if (attackingEntity instanceof LivingEntity living) {
            double mult = living.getAttributeValue(ALObjects.Attributes.EXPERIENCE_GAINED);
            return (int) (original * mult);
        }
        return original;
    }

    @Unique
    private boolean apoth_isEchoingDrops = false;

    @Inject(method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At("TAIL"))
    private void apoth_handleSpawnerDrops(ServerLevel serverLevel, net.minecraft.world.damagesource.DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Capturing
        Entity killer = damageSource.getEntity();
        if (killer instanceof LivingEntity living) {
            var capturingPair = net.minecraft.world.item.enchantment.EnchantmentHelper.getHighestLevel(living.getWeaponItem(), dev.shadowsoffire.apothic_spawners.ASObjects.CAPTURING);
            if (capturingPair != null && !self.getType().builtInRegistryHolder().is(dev.shadowsoffire.apothic_spawners.ASObjects.BLACKLISTED_FROM_SPAWNERS)) {
                if (self.level().getRandom().nextFloat() < capturingPair.getSecond() * capturingPair.getFirst()) {
                    net.minecraft.world.item.SpawnEggItem.byId(self.getType()).ifPresent(eggItem -> {
                        ItemStack egg = new ItemStack(eggItem);
                        self.spawnAtLocation(serverLevel, egg);
                    });
                }
            }
        }

        // Echoing extra loot table rolls
        if (!this.apoth_isEchoingDrops) {
            int echoes = AttributesUtil.getPersistentData(self).getIntOr(dev.shadowsoffire.apothic_spawners.stats.SpawnerStats.ECHOING.getId().toString(), 0);
            if (echoes > 0) {
                this.apoth_isEchoingDrops = true;
                try {
                    for (int i = 0; i < echoes; i++) {
                        self.dropFromLootTable(serverLevel, damageSource, hitByPlayer);
                    }
                } finally {
                    this.apoth_isEchoingDrops = false;
                }
            }
        }
    }
}
