package dev.shadowsoffire.placebo.codec;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.resources.Identifier;

/**
 * Registry of named sub-codecs that simultaneously acts as a polymorphic {@link Codec}.
 *
 * @param <V> The base value type.
 */
public class CodecMap<V extends CodecProvider<? super V>> implements Codec<V> {

    protected final String name;
    protected final BiMap<Identifier, Codec<? extends V>> registry = HashBiMap.create();
    protected final Codec<V> codec;

    @Nullable
    protected Codec<? extends V> defaultCodec = null;

    public CodecMap(String name) {
        this.name = name;
        this.codec = new MapBackedCodec<>(name, this.registry, () -> this.defaultCodec);
    }

    public synchronized void register(Identifier key, Codec<? extends V> codec) {
        if (this.registry.containsKey(key)) {
            throw new IllegalArgumentException("Attempted to register duplicate codec with key: " + key);
        }
        this.registry.put(key, codec);
    }

    public synchronized void setDefaultCodec(Codec<? extends V> codec) {
        this.defaultCodec = codec;
    }

    @Nullable
    public Codec<? extends V> getDefaultCodec() {
        return this.defaultCodec;
    }

    public Set<Identifier> getKeys() {
        return Collections.unmodifiableSet(this.registry.keySet());
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return this.codec.decode(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return this.codec.encode(input, ops, prefix);
    }
}
