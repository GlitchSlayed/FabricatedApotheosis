package dev.shadowsoffire.apothic_attributes.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public final class AbilityCooldowns {

    private AbilityCooldowns() {}

    public static boolean isOnCooldown(LivingEntity entity, Identifier id, int baseCooldown) {
        return dev.shadowsoffire.apotheosis.attributes.api.AbilityCooldowns.isOnCooldown(entity, id, baseCooldown);
    }

    public static void startCooldown(LivingEntity entity, Identifier id) {
        dev.shadowsoffire.apotheosis.attributes.api.AbilityCooldowns.startCooldown(entity, id);
    }

    public static long getRemaining(LivingEntity entity, Identifier id, int baseCooldown) {
        return dev.shadowsoffire.apotheosis.attributes.api.AbilityCooldowns.getRemaining(entity, id, baseCooldown);
    }

    public static int applyCDR(LivingEntity entity, int baseCooldown) {
        return dev.shadowsoffire.apotheosis.attributes.api.AbilityCooldowns.applyCDR(entity, baseCooldown);
    }
}
