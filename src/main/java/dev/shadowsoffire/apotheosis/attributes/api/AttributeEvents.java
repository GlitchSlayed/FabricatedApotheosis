package dev.shadowsoffire.apotheosis.attributes.api;

import java.util.Random;

import dev.shadowsoffire.apotheosis.attributes.payload.CritParticlePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;

/**
 * Handles Apothic Attributes combat events, auxiliary damage, dodging, critical strikes, and draw speed.
 */
public class AttributeEvents {

    private static final Random DODGE_RAND = new Random();
    public static boolean noRecurse = false;

    public static int computeDodgeSeed(LivingEntity target) {
        int delta = 0x9E3779B9;
        int base = target.tickCount + target.getUUID().hashCode();
        return base + delta + (base << 6) + (base >> 2);
    }

    public static boolean isDodging(LivingEntity target) {
        double chance = target.getAttributeValue(ALObjects.Attributes.DODGE_CHANCE);
        if (chance <= 0.0) return false;
        DODGE_RAND.setSeed(computeDodgeSeed(target));
        return DODGE_RAND.nextFloat() <= chance;
    }

    public static void onDodge(LivingEntity target) {
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
            ALObjects.Sounds.DODGE.value(), SoundSource.NEUTRAL, 1.0F, 0.7F + target.getRandom().nextFloat() * 0.3F);
        if (target.level() instanceof ServerLevel sl) {
            double height = target.getBbHeight();
            double width = target.getBbWidth();
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX() - width / 4, target.getY(), target.getZ() - width / 4,
                6, -width / 4, height / 8, -width / 4, 0.0);
        }
    }

    public static boolean checkMeleeDodge(LivingEntity target, DamageSource source) {
        if (target.level().isClientSide()) return false;
        Entity attacker = source.getDirectEntity();
        if (attacker instanceof Player player) {
            double atkRange = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
            double atkRangeSqr = atkRange * atkRange;
            if (attacker.distanceToSqr(target) <= atkRangeSqr && isDodging(target)) {
                onDodge(target);
                return true;
            }
        }
        else if (attacker instanceof Mob mob) {
            if (mob.isWithinMeleeAttackRange(target) && isDodging(target)) {
                onDodge(target);
                return true;
            }
        }
        return false;
    }

    public static float rollCriticalStrikes(LivingEntity attacker, LivingEntity target, float initialAmount) {
        double critChance = attacker.getAttributeValue(ALObjects.Attributes.CRIT_CHANCE);
        float critDmg = (float) attacker.getAttributeValue(ALObjects.Attributes.CRIT_DAMAGE);
        RandomSource rand = target.getRandom();
        float damage = initialAmount;

        while (rand.nextFloat() <= critChance && critDmg > 1.0F) {
            critChance--;
            damage += initialAmount * (critDmg - 1.0F);
            critDmg *= 0.85F;
        }

        if (damage > initialAmount && !attacker.level().isClientSide() && attacker.level() instanceof ServerLevel serverLevel) {
            CritParticlePayload payload = new CritParticlePayload(target.getId());
            for (ServerPlayer player : PlayerLookup.tracking(serverLevel, target.chunkPosition())) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        return damage;
    }

    public static void applyMeleeAuxDamage(LivingEntity target, DamageSource source) {
        if (target.level().isClientSide() || target.isDeadOrDying()) return;
        if (noRecurse) return;
        noRecurse = true;
        try {
            if (source.getDirectEntity() instanceof LivingEntity attacker && AttributesUtil.isPhysicalDamage(source)) {
                AuxDmgTracker.executeWith(target, tracker -> {
                    float hpDmg = (float) attacker.getAttributeValue(ALObjects.Attributes.CURRENT_HP_DAMAGE) * target.getHealth();
                    tracker.attackWith(attacker, target, ALObjects.DamageTypes.CURRENT_HP_DAMAGE, hpDmg, null);
                    tracker.attackWith(attacker, target, ALObjects.DamageTypes.FIRE_DAMAGE, ALObjects.Attributes.FIRE_DAMAGE, AttributeEvents::applyPostFireDamage);
                    tracker.attackWith(attacker, target, ALObjects.DamageTypes.COLD_DAMAGE, ALObjects.Attributes.COLD_DAMAGE, AttributeEvents::applyPostColdDamage);
                });

                if (target.isDeadOrDying()) {
                    AttributesUtil.getPersistentData(target).putBoolean("apoth.killed_by_aux_dmg", true);
                }
            }
        }
        finally {
            noRecurse = false;
        }
    }

    public static void applyPostFireDamage(LivingEntity attacker, LivingEntity target, DamageSource src, float dmg, float delta) {
        target.setRemainingFireTicks(target.getRemainingFireTicks() + (int) (10 * dmg));
    }

    public static void applyPostColdDamage(LivingEntity attacker, LivingEntity target, DamageSource src, float dmg, float delta) {
        int duration = (int) Math.min(150, 15 * dmg);
        int amp = Math.max(0, Mth.log2(Math.round(dmg / 5)));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, amp));
    }

    public static boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ProjectileWeaponItem || stack.getItem() instanceof TridentItem;
    }

    public static int computeDrawSpeedOffset(Player player, ItemStack stack) {
        if (!canBenefitFromDrawSpeed(stack)) return 0;
        double t = player.getAttributeValue(ALObjects.Attributes.DRAW_SPEED) - 1.0;
        if (t == 0.0) return 0;

        int offset = -1;
        if (t < 0) {
            offset = 1;
            t = -t;
        }

        int totalOffset = 0;
        while (t > 1) {
            totalOffset += offset;
            t--;
        }

        if (t > 0.5F) {
            if (player.tickCount % 2 == 0) totalOffset += offset;
            t -= 0.5F;
        }

        int mod = (int) Math.floor(1 / Math.min(1, t));
        if (mod > 0 && player.tickCount % mod == 0) totalOffset += offset;

        return totalOffset;
    }
}
