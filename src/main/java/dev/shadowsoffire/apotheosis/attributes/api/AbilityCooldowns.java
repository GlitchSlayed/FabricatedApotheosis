package dev.shadowsoffire.apotheosis.attributes.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * Public entry point for the cooldown system.
 * The {@link ALObjects.Attributes#COOLDOWN_REDUCTION COOLDOWN_REDUCTION} attribute scales how long applied cooldowns last.
 */
public final class AbilityCooldowns {

    private AbilityCooldowns() {}

    /**
     * Returns true if {@code id} is on cooldown for {@code entity}. The base cooldown is scaled by the entity's
     * {@link ALObjects.Attributes#COOLDOWN_REDUCTION} value before comparison.
     */
    public static boolean isOnCooldown(LivingEntity entity, Identifier id, int baseCooldown) {
        int effective = applyCDR(entity, baseCooldown);
        CooldownTracker tracker = entity.getAttachedOrCreate(ALObjects.Attachments.COOLDOWNS);
        return tracker.isOnCooldown(id, effective, entity.level().getGameTime());
    }

    /**
     * Records that {@code id} fired for {@code entity}.
     */
    public static void startCooldown(LivingEntity entity, Identifier id) {
        long gameTime = entity.level().getGameTime();
        CooldownTracker tracker = entity.getAttachedOrCreate(ALObjects.Attachments.COOLDOWNS);
        tracker.startCooldown(id, gameTime);
        entity.setAttached(ALObjects.Attachments.COOLDOWNS, tracker);
    }

    /**
     * Returns the number of ticks remaining on {@code id} for {@code entity}, or 0 if not on cooldown.
     */
    public static long getRemaining(LivingEntity entity, Identifier id, int baseCooldown) {
        int effective = applyCDR(entity, baseCooldown);
        CooldownTracker tracker = entity.getAttachedOrCreate(ALObjects.Attachments.COOLDOWNS);
        return tracker.getRemaining(id, effective, entity.level().getGameTime());
    }

    /**
     * Returns the cooldown that would be applied to {@code entity} after multiplying by {@code (1 - cdr)}.
     */
    public static int applyCDR(LivingEntity entity, int baseCooldown) {
        if (baseCooldown <= 0) return baseCooldown;
        double cdr = entity.getAttributeValue(ALObjects.Attributes.COOLDOWN_REDUCTION);
        double scaled = baseCooldown * (1.0D - cdr);
        return scaled < 1 ? 1 : (int) Math.round(scaled);
    }

    /**
     * Clears the cooldown entry for {@code id} on {@code entity}, if present.
     */
    public static void clear(LivingEntity entity, Identifier id) {
        CooldownTracker tracker = entity.getAttachedOrCreate(ALObjects.Attachments.COOLDOWNS);
        tracker.clear(id);
        entity.setAttached(ALObjects.Attachments.COOLDOWNS, tracker);
    }
}
