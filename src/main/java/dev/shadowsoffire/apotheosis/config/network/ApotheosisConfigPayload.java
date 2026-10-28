package dev.shadowsoffire.apotheosis.config.network;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apotheosis.config.ServerConfig;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Server-to-client packet that synchronizes authoritative gameplay configuration.
 */
public record ApotheosisConfigPayload(ServerConfig config) implements CustomPacketPayload {

    public static final Type<ApotheosisConfigPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("apotheosis", "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ApotheosisConfigPayload> CODEC = StreamCodec.composite(
        ServerConfig.STREAM_CODEC, ApotheosisConfigPayload::config,
        ApotheosisConfigPayload::new
    );

    @Override
    public Type<ApotheosisConfigPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<ApotheosisConfigPayload> {

        @Override
        public Type<ApotheosisConfigPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ApotheosisConfigPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(ApotheosisConfigPayload msg, Player player) {
            ApotheosisConfig.setRemoteServerConfig(msg.config());
        }
    }
}
