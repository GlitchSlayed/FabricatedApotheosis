package dev.shadowsoffire.placebo.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * A cached, immutable representation derived from an {@link ItemStack}.
 * <p>
 * Cached objects recompute only when the relevant component hash changes.
 *
 * @param <T> The type of object being cached.
 */
public final class CachedObject<T> {

    public static final int HAS_NEVER_BEEN_INITIALIZED = -2;

    protected final Identifier id;
    protected final Function<ItemStack, T> deserializer;
    protected final ToIntFunction<ItemStack> hasher;

    protected volatile T data = null;
    protected volatile int lastHash = HAS_NEVER_BEEN_INITIALIZED;

    public CachedObject(Identifier id, Function<ItemStack, T> deserializer, ToIntFunction<ItemStack> hasher) {
        this.id = id;
        this.deserializer = deserializer;
        this.hasher = hasher;
    }

    @Nullable
    public T get(ItemStack stack) {
        if (this.lastHash == HAS_NEVER_BEEN_INITIALIZED || this.hasher.applyAsInt(stack) != this.lastHash) {
            this.compute(stack);
        }
        return this.data;
    }

    public void reset() {
        this.data = null;
        this.lastHash = HAS_NEVER_BEEN_INITIALIZED;
    }

    protected synchronized void compute(ItemStack stack) {
        this.data = this.deserializer.apply(stack);
        this.lastHash = this.hasher.applyAsInt(stack);
    }

    public static ToIntFunction<ItemStack> hashComponents(DataComponentType<?>... types) {
        List<DataComponentType<?>> typeList = Arrays.asList(types);
        return stack -> Arrays.hashCode(typeList.stream().map(stack::get).filter(Objects::nonNull).toArray());
    }
}
