package dev.shadowsoffire.apotheosis.attributes.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AttributesUtil {

    public static final ThreadLocal<ServerPlayer> CURRENT_BREAKER = new ThreadLocal<>();

    public static boolean isPhysicalDamage(DamageSource src) {
        return src.is(ALObjects.Tags.IS_PHYSICAL) && !src.is(ALObjects.Tags.IS_NON_PHYSICAL);
    }

    /**
     * Retrieves or creates the persistent data compound tag attached to an entity.
     */
    public static CompoundTag getPersistentData(Entity entity) {
        if (ALObjects.Attachments.PERSISTENT_DATA != null) {
            return entity.getAttachedOrCreate(ALObjects.Attachments.PERSISTENT_DATA);
        }
        return new CompoundTag();
    }

    /**
     * Computes the maximum total absorption that {@link ALObjects.Attributes#OVERHEAL} will fill up to, which is 50% of the entity's max health.
     */
    public static float getOverhealCap(LivingEntity entity) {
        return entity.getMaxHealth() * 0.5F;
    }

    /**
     * Returns the portion of the entity's current absorption that was granted by {@link ALObjects.Attributes#OVERHEAL}.
     * Clamped to current absorption amount so it never reports more absorption than the entity actually has.
     */
    public static float getOverhealAbsorption(LivingEntity entity) {
        if (!entity.hasAttached(ALObjects.Attachments.OVERHEAL_ABSORPTION)) return 0;
        return Math.min(entity.getAttached(ALObjects.Attachments.OVERHEAL_ABSORPTION), entity.getAbsorptionAmount());
    }

    /**
     * Records the portion of the entity's current absorption that was granted by {@link ALObjects.Attributes#OVERHEAL}.
     */
    public static void setOverhealAbsorption(LivingEntity entity, float amount) {
        entity.setAttached(ALObjects.Attachments.OVERHEAL_ABSORPTION, Math.max(0, amount));
    }
}
