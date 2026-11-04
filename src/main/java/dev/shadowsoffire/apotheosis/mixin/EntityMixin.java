package dev.shadowsoffire.apotheosis.mixin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.jetbrains.annotations.Nullable;

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

    public static class DropCaptureContext {
        public final LivingEntity entity;
        public final List<ItemEntity> drops = new ArrayList<>();

        public DropCaptureContext(LivingEntity entity) {
            this.entity = entity;
        }
    }

    @Unique
    private static final ThreadLocal<Deque<DropCaptureContext>> apoth$CAPTURE_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    @Unique
    public static void apoth$pushCaptureContext(DropCaptureContext ctx) {
        apoth$CAPTURE_STACK.get().push(ctx);
    }

    @Unique
    @Nullable
    public static DropCaptureContext apoth$popCaptureContext() {
        Deque<DropCaptureContext> stack = apoth$CAPTURE_STACK.get();
        if (stack.isEmpty()) return null;
        DropCaptureContext popped = stack.pop();
        if (stack.isEmpty()) {
            apoth$CAPTURE_STACK.remove();
        }
        return popped;
    }

    @Unique
    @Nullable
    public static DropCaptureContext apoth$peekCaptureContext() {
        Deque<DropCaptureContext> stack = apoth$CAPTURE_STACK.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    @WrapOperation(
        method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean apoth$interceptDeathDrop(ServerLevel level, Entity entity, Operation<Boolean> original) {
        DropCaptureContext ctx = apoth$peekCaptureContext();
        if (ctx != null && (Object) this == ctx.entity && entity instanceof ItemEntity item) {
            ctx.drops.add(item);
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
