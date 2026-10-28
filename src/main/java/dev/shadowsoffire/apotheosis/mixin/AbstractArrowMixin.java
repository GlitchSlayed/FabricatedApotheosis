package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributeEvents;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {

    @Shadow
    private double baseDamage;

    @Shadow
    private IntOpenHashSet piercingIgnoreEntityIds;

    @Shadow
    public abstract byte getPierceLevel();

    protected AbstractArrowMixin(net.minecraft.world.entity.EntityType<? extends Projectile> entityType, net.minecraft.world.level.Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void apoth_scaleArrowOnFirstTick(CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (!self.level().isClientSide() && !AttributesUtil.getPersistentData(self).getBooleanOr("apothic_attributes.arrow.done", false)) {
            if (self.getOwner() instanceof LivingEntity owner) {
                this.baseDamage *= owner.getAttributeValue(ALObjects.Attributes.ARROW_DAMAGE);
                self.setDeltaMovement(self.getDeltaMovement().scale(owner.getAttributeValue(ALObjects.Attributes.ARROW_VELOCITY)));
            }
            AttributesUtil.getPersistentData(self).putBoolean("apothic_attributes.arrow.done", true);
        }
    }

    @Inject(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", at = @At("HEAD"), cancellable = true)
    private void apoth_dodgeArrow(EntityHitResult result, CallbackInfo ci) {
        if (result.getEntity() instanceof LivingEntity target) {
            if (AttributeEvents.isDodging(target)) {
                AttributeEvents.onDodge(target);
                if (this.getPierceLevel() > 0) {
                    if (this.piercingIgnoreEntityIds == null) {
                        this.piercingIgnoreEntityIds = new IntOpenHashSet(this.getPierceLevel());
                    }
                    this.piercingIgnoreEntityIds.add(target.getId());
                }
                ci.cancel();
            }
        }
    }
}
