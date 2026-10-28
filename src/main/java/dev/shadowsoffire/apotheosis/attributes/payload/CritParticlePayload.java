package dev.shadowsoffire.apotheosis.attributes.payload;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CritParticlePayload(int entityId) implements CustomPacketPayload {

    public static final Type<CritParticlePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ALObjects.MODID, "crit_particle"));

    public static final StreamCodec<FriendlyByteBuf, CritParticlePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, CritParticlePayload::entityId,
        CritParticlePayload::new);

    @Override
    public Type<CritParticlePayload> type() {
        return TYPE;
    }
}
