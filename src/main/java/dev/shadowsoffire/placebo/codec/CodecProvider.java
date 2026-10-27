package dev.shadowsoffire.placebo.codec;

import com.mojang.serialization.Codec;

/**
 * An object that supplies the codec used to serialize/deserialize it.
 *
 * @param <R> The base registry type of the object
 */
public interface CodecProvider<R> {

    /**
     * @return The codec used to serialize/deserialize this object.
     */
    Codec<? extends R> getCodec();
}
