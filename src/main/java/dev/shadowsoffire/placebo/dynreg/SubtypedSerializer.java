package dev.shadowsoffire.placebo.dynreg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.resources.Identifier;

public class SubtypedSerializer<R extends CodecProvider<? super R>> extends RegistrySerializer.Subtyped<R> {

    public SubtypedSerializer(String name, boolean synced) {
        super(name, synced);
    }

    public SubtypedSerializer(String name) {
        super(name, false);
    }

    @Override
    public SubtypedSerializer<R> register(Identifier id, Codec<? extends R> codec) {
        super.register(id, codec);
        return this;
    }

    public SubtypedSerializer<R> register(Identifier id, MapCodec<? extends R> codec) {
        super.register(id, codec.codec());
        return this;
    }
}
