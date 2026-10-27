package dev.shadowsoffire.placebo.codec;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import dev.shadowsoffire.placebo.Placebo;
import net.minecraft.resources.Identifier;

/**
 * Map-backed codec with polymorphic dispatch on a 'type' key.
 *
 * @param <V> Target value type extending {@link CodecProvider}.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapBackedCodec<V extends CodecProvider<? super V>> implements Codec<V> {

    protected final String name;
    protected final BiMap<Identifier, Codec<? extends V>> registry;
    protected final Supplier<Codec<? extends V>> defaultCodec;

    public MapBackedCodec(String name, BiMap<Identifier, Codec<? extends V>> registry, Supplier<Codec<? extends V>> defaultCodec) {
        this.name = name;
        this.registry = registry;
        this.defaultCodec = defaultCodec;
    }

    public MapBackedCodec(String name, BiMap<Identifier, Codec<? extends V>> registry) {
        this(name, registry, () -> null);
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        Optional<T> typeObj = ops.get(input, "type").result();
        Optional<Identifier> key = typeObj.flatMap(t -> Identifier.CODEC.decode(ops, t).result().map(Pair::getFirst));

        Codec codec = key.map(this.registry::get).orElseGet(this.defaultCodec);

        if (codec == null) {
            return DataResult.error(() -> "Failure when parsing " + this.name + ": Unrecognized type '" + key.map(Identifier::toString).orElse("null") + "'");
        }
        return codec.decode(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        Codec codec = input.getCodec();
        Identifier key = this.registry.inverse().get(codec);
        if (key == null) {
            Codec def = this.defaultCodec.get();
            if (codec == def) {
                return ((Codec<V>) codec).encode(input, ops, prefix);
            }
            return DataResult.error(() -> "Failure when encoding " + this.name + ": Codec " + codec + " is not registered in registry.");
        }

        DataResult<T> encoded = ((Codec<V>) codec).encode(input, ops, prefix);
        T keyObj = ops.createString(key.toString());
        return encoded.flatMap(res -> ops.mergeToMap(res, ops.createString("type"), keyObj));
    }
}
