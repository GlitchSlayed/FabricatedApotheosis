package dev.shadowsoffire.placebo.codec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;

/**
 * Utility functions for Codecs.
 */
public class PlaceboCodecs {

    public static <T extends CodecProvider<T>> Codec<T> mapBackedDefaulted(String name, BiMap<Identifier, Codec<? extends T>> reg, Codec<? extends T> defaultCodec) {
        return new MapBackedCodec<>(name, reg, () -> defaultCodec);
    }

    public static <T extends CodecProvider<? super T>> Codec<T> mapBacked(String name, BiMap<Identifier, Codec<? extends T>> reg) {
        return new MapBackedCodec<>(name, reg);
    }

    public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
        return setFromList(elementCodec.listOf());
    }

    public static <T> Codec<Set<T>> setFromList(Codec<List<T>> listCodec) {
        return listCodec.<Set<T>>xmap(LinkedHashSet::new, ArrayList::new);
    }

    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> clazz) {
        return Codec.stringResolver(e -> e.name().toLowerCase(Locale.ROOT), name -> Enum.valueOf(clazz, name.toUpperCase(Locale.ROOT)));
    }
}
