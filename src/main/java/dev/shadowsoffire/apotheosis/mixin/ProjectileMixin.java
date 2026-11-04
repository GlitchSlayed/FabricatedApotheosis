package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @Inject(method = "shoot(DDDFF)V", at = @At("HEAD"))
    private void apoth$onProjectileShoot(double x, double y, double z, float velocity, float inaccuracy, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (self.getOwner() instanceof LivingEntity user) {
            ItemStack weapon = user.getUseItem();
            if (weapon.isEmpty()) {
                weapon = user.getMainHandItem();
                if (weapon.isEmpty() || !LootCategory.forItem(weapon).isRanged()) {
                    weapon = user.getOffhandItem();
                }
            }
            if (!weapon.isEmpty()) {
                SocketHelper.getGems(weapon).onProjectileFired(user, self);
                AffixHelper.streamAffixes(weapon).forEach(a -> a.onProjectileFired(user, self));
                AffixHelper.copyToProjectile(weapon, self);
            }
        }
    }

    @Inject(method = "onHit", at = @At("HEAD"))
    private void apoth$onProjectileHit(HitResult hitResult, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        SocketHelper.getGemInstances(self).forEach(inst -> inst.onProjectileImpact(self, hitResult));
        var affixes = AffixHelper.getAffixes(self);
        affixes.values().forEach(inst -> inst.onProjectileImpact(self, hitResult, hitResult.getType()));
    }
}
