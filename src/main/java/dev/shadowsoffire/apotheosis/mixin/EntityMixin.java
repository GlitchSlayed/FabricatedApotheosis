package dev.shadowsoffire.apotheosis.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique
    public static final ThreadLocal<List<ItemEntity>> apoth$CAPTURED_DROPS = new ThreadLocal<>();

    @WrapOperation(
        method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean apoth$interceptDeathDrop(ServerLevel level, Entity entity, Operation<Boolean> original) {
        List<ItemEntity> drops = apoth$CAPTURED_DROPS.get();
        if (drops != null && entity instanceof ItemEntity item) {
            drops.add(item);
            return true;
        }
        return original.call(level, entity);
    }

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
