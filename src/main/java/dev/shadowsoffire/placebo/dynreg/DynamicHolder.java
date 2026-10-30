package dev.shadowsoffire.placebo.dynreg;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.Identifier;

/**
 * A Dynamic Holder provides an indirect, reload-safe reference to an entry in a {@link DynamicRegistry}.
 *
 * @param <R> The registry entry type.
 */
public class DynamicHolder<R> implements Supplier<R> {

    public static final Identifier EMPTY = Identifier.fromNamespaceAndPath("empty", "empty");

    protected final DynamicRegistry<R> registry;
    protected final Identifier id;

    @Nullable
    protected volatile R value;

    DynamicHolder(DynamicRegistry<R> registry, Identifier id) {
        this.registry = registry;
        this.id = id;
    }

    public Identifier getId() {
        return this.id;
    }

    public DynamicRegistry<R> getRegistry() {
        return this.registry;
    }

    public boolean isBound() {
        this.bind();
        return this.value != null;
    }

    public synchronized void bind() {
        if (this.value == null && !this.id.equals(EMPTY)) {
            this.value = this.registry.get(this.id);
        }
    }

    public synchronized void unbind() {
        this.value = null;
    }

    @Override
    public R get() {
        this.bind();
        if (this.value == null) {
            throw new IllegalStateException("DynamicHolder '" + this.id + "' in registry '" + this.registry.getId() + "' is unbound!");
        }
        return this.value;
    }

    @Nullable
    public R getOrNull() {
        this.bind();
        return this.value;
    }

    public java.util.Optional<R> getOptional() {
        return java.util.Optional.ofNullable(this.getOrNull());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicHolder<?> that = (DynamicHolder<?>) o;
        return Objects.equals(this.registry.getId(), that.registry.getId()) && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.registry.getId(), this.id);
    }

    @Override
    public String toString() {
        return "DynamicHolder{" + this.registry.getId() + "/" + this.id + "}";
    }
}
