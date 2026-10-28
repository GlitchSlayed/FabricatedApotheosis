package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyVariable(at = @At("HEAD"), method = "checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", argsOnly = true)
    public double apoth_checkFallDamageWithGravity(double yMotion, double motion, boolean onGround, BlockState state, BlockPos pos) {
        if (yMotion < 0 && (Object) this instanceof LivingEntity le) {
            double gravity = le.getAttributeValue(Attributes.GRAVITY);
            yMotion *= gravity / 0.08D;
            if (gravity <= 0.01D) {
                yMotion = 0;
            }
        }
        return yMotion;
    }
}
