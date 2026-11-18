package dev.shadowsoffire.apotheosis.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Preserves deserialized entity health above base max health until equipment attribute modifiers are applied (MC-17876).
 * Ported from upstream Darkhax MaxHealthFix backup included in Apotheosis.
 */
@Mixin(LivingEntity.class)
public abstract class MHFMixinLivingEntity {

    @Unique
    @Nullable
    private Float apoth$actualHealth = null;

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("HEAD"))
    private void maxhealthfix$readAdditionalSaveData(ValueInput input, CallbackInfo callback) {
        final float savedHealth = input.getFloatOr("Health", 0F);
        if (savedHealth > this.getMaxHealth() && savedHealth > 0) {
            this.apoth$actualHealth = savedHealth;
        }
    }

    @Inject(method = "detectEquipmentUpdates()V", at = @At("RETURN"))
    private void maxhealthfix$detectEquipmentUpdates(CallbackInfo callback) {
        if (this.apoth$actualHealth != null) {
            if (this.apoth$actualHealth > 0 && this.apoth$actualHealth > this.getHealth()) {
                this.setHealth(this.apoth$actualHealth);
            }
            this.apoth$actualHealth = null;
        }
    }

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float newHealth);
}
