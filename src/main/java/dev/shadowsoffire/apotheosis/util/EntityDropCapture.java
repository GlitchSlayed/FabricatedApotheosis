package dev.shadowsoffire.apotheosis.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Thread-scoped capture stack for intercepting death loot spawns during {@code dropAllDeathLoot}.
 * Held outside {@link dev.shadowsoffire.apotheosis.mixin.EntityMixin} because Spongepowered Mixin
 * forbids non-private static helpers inside mixin classes.
 */
public final class EntityDropCapture {

    public static class DropCaptureContext {
        public final LivingEntity entity;
        public final List<ItemEntity> drops = new ArrayList<>();

        public DropCaptureContext(LivingEntity entity) {
            this.entity = entity;
        }
    }

    private static final ThreadLocal<Deque<DropCaptureContext>> CAPTURE_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private EntityDropCapture() {}

    public static void pushCaptureContext(DropCaptureContext ctx) {
        CAPTURE_STACK.get().push(ctx);
    }

    @Nullable
    public static DropCaptureContext popCaptureContext() {
        Deque<DropCaptureContext> stack = CAPTURE_STACK.get();
        if (stack.isEmpty()) return null;
        DropCaptureContext popped = stack.pop();
        if (stack.isEmpty()) {
            CAPTURE_STACK.remove();
        }
        return popped;
    }

    @Nullable
    public static DropCaptureContext peekCaptureContext() {
        Deque<DropCaptureContext> stack = CAPTURE_STACK.get();
        return stack.isEmpty() ? null : stack.peek();
    }
}
