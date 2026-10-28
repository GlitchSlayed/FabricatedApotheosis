package dev.shadowsoffire.apotheosis.attributes.api;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.mixin.EntityAccessor;
import dev.shadowsoffire.apotheosis.mixin.LivingEntityAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Tracks invulnerability times and last hurt amounts for auxiliary damage types.
 */
public class AuxDmgTracker {

    public static final Codec<AuxDmgTracker> CODEC = Codec.unboundedMap(ResourceKey.codec(Registries.DAMAGE_TYPE), Entry.CODEC)
        .xmap(AuxDmgTracker::new, tracker -> tracker.data);

    private final IdentityHashMap<ResourceKey<DamageType>, Entry> data = new IdentityHashMap<>(3);

    private transient int origInvulTime = 0;
    private transient float origLastHurt = 0;

    public AuxDmgTracker() {}

    public AuxDmgTracker(Map<ResourceKey<DamageType>, Entry> data) {
        this.data.putAll(data);
    }

    /**
     * Retrieves the aux damage tracker for the given entity and runs an execution pipeline with it.
     */
    public static void executeWith(LivingEntity entity, Consumer<AuxDmgTracker> consumer) {
        AuxDmgTracker tracker = ALObjects.Attachments.AUX_DMG_TRACKER != null
            ? entity.getAttachedOrCreate(ALObjects.Attachments.AUX_DMG_TRACKER)
            : new AuxDmgTracker();
        tracker.start(entity);
        try {
            consumer.accept(tracker);
        }
        finally {
            tracker.end(entity);
        }
    }

    public boolean attackWith(LivingEntity attacker, LivingEntity target, ResourceKey<DamageType> type, Holder<Attribute> attribute, @Nullable PostAttackEffect callback) {
        float damage = (float) attacker.getAttributeValue(attribute);
        return attackWith(attacker, target, type, damage, callback);
    }

    public boolean attackWith(LivingEntity attacker, LivingEntity target, ResourceKey<DamageType> type, float damage, @Nullable PostAttackEffect callback) {
        float atkStrength = attacker instanceof Player player ? player.getAttackStrengthScale(0.5F) : 1.0F;
        if (damage > 0.001 && atkStrength >= 0.55F && !target.isDeadOrDying() && target.level() instanceof ServerLevel serverLevel) {
            this.setup(target, type);
            float dmg = modifyDamage(attacker, target, atkStrength * damage);
            float health = target.getHealth();
            if (target.hurtServer(serverLevel, src(type, attacker), dmg)) {
                float delta = health - target.getHealth();
                if (callback != null) {
                    callback.apply(attacker, target, src(type, attacker), dmg, delta);
                }
                this.record(target, type);
                return true;
            }
        }
        return false;
    }

    public void start(LivingEntity entity) {
        this.origInvulTime = ((EntityAccessor) entity).apoth_getInvulnerableTime();
        this.origLastHurt = ((LivingEntityAccessor) entity).apoth_getLastHurt();
    }

    public void setup(LivingEntity entity, ResourceKey<DamageType> type) {
        Entry entry = getData(type);
        ((EntityAccessor) entity).apoth_setInvulnerableTime(entry.time());
        ((LivingEntityAccessor) entity).apoth_setLastHurt(entry.lastHurt());
    }

    public void record(LivingEntity entity, ResourceKey<DamageType> type) {
        int invul = ((EntityAccessor) entity).apoth_getInvulnerableTime();
        float lastHurt = ((LivingEntityAccessor) entity).apoth_getLastHurt();
        Entry entry = new Entry(invul, lastHurt);
        data.put(type, entry);
    }

    public void end(LivingEntity entity) {
        ((EntityAccessor) entity).apoth_setInvulnerableTime(this.origInvulTime);
        ((LivingEntityAccessor) entity).apoth_setLastHurt(this.origLastHurt);
    }

    public void tick() {
        this.data.values().forEach(entry -> {
            if (entry.time > 0) {
                entry.time--;
            }
        });
    }

    public Entry getData(ResourceKey<DamageType> type) {
        return data.getOrDefault(type, Entry.DEFAULT);
    }

    private static DamageSource src(ResourceKey<DamageType> type, LivingEntity entity) {
        return entity.level().damageSources().source(type, entity);
    }

    private static float modifyDamage(LivingEntity attacker, LivingEntity target, float damage) {
        if (AttributesUtil.getPersistentData(target).getBooleanOr("apoth.hit_by_sweep_attack", false) && attacker.getAttributes().hasAttribute(Attributes.SWEEPING_DAMAGE_RATIO)) {
            return Math.min(damage, 1.0F + (float) attacker.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * damage);
        }
        return damage;
    }

    @FunctionalInterface
    public interface PostAttackEffect {
        void apply(LivingEntity attacker, LivingEntity target, DamageSource src, float damage, float delta);
    }

    public static class Entry {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("time").forGetter(Entry::time),
                Codec.floatRange(0, Float.MAX_VALUE).fieldOf("lastHurt").forGetter(Entry::lastHurt))
            .apply(inst, Entry::new));

        public static final Entry DEFAULT = new Entry(0, 0);

        protected int time;
        protected final float lastHurt;

        public Entry(int time, float lastHurt) {
            this.time = Math.max(0, time);
            this.lastHurt = Math.max(0, lastHurt);
        }

        public int time() {
            return time;
        }

        public float lastHurt() {
            return lastHurt;
        }
    }
}
