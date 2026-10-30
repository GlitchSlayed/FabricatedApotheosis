package dev.shadowsoffire.placebo.dynreg;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Encapsulates the serialization and optional network sync codecs for a {@link DynamicRegistry}.
 *
 * @param <R> Registry element type.
 */
public abstract class RegistrySerializer<R> {

    public abstract Codec<R> codec();

    @Nullable
    public abstract StreamCodec<RegistryFriendlyByteBuf, R> streamCodec();

    public final boolean isSynced() {
        return this.streamCodec() != null;
    }

    public static <R> RegistrySerializer<R> simple(Codec<R> codec) {
        return new Simple<>(codec, null);
    }

    public static <R> RegistrySerializer<R> synced(Codec<R> codec) {
        return new Simple<>(codec, ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    public static <R> RegistrySerializer<R> synced(Codec<R> codec, StreamCodec<RegistryFriendlyByteBuf, R> streamCodec) {
        return new Simple<>(codec, streamCodec);
    }

    public static <R extends CodecProvider<? super R>> Subtyped<R> subtyped(String name) {
        return new Subtyped<>(name, false);
    }

    public static <R extends CodecProvider<? super R>> Subtyped<R> subtypedSynced(String name) {
        return new Subtyped<>(name, true);
    }

    private static class Simple<R> extends RegistrySerializer<R> {
        private final Codec<R> codec;
        @Nullable
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        Simple(Codec<R> codec, @Nullable StreamCodec<RegistryFriendlyByteBuf, R> streamCodec) {
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        @Override
        public Codec<R> codec() {
            return this.codec;
        }

        @Override
        @Nullable
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return this.streamCodec;
        }
    }

    public static class Subtyped<R extends CodecProvider<? super R>> extends RegistrySerializer<R> {
        private final CodecMap<R> codecMap;
        private final boolean synced;

        Subtyped(String name, boolean synced) {
            this.codecMap = new CodecMap<>(name);
            this.synced = synced;
        }

        public CodecMap<R> getCodecMap() {
            return this.codecMap;
        }

        public Subtyped<R> register(net.minecraft.resources.Identifier id, Codec<? extends R> codec) {
            this.codecMap.register(id, codec);
            return this;
        }

        @Override
        public Codec<R> codec() {
            return this.codecMap;
        }

        @Override
        @Nullable
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return this.synced ? ByteBufCodecs.fromCodecWithRegistries(this.codecMap) : null;
        }
    }
}
