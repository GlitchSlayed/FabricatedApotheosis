package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Mixin to tridents to enable Piercing to work.
 */
@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {

    protected ThrownTridentMixin(
        EntityType<? extends AbstractArrow> entityType,
        double x, double y, double z,
        Level level,
        ItemStack pickupItemStack,
        ItemStack firedFromWeapon) {
        super(entityType, x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void apoth_init(CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            int pierce = EnchantmentHelper.getPiercingCount((ServerLevel) this.level(), this.getPickupItem(), this.getPickupItem());
            this.setPierceLevel((byte) pierce);
        }
    }
}
