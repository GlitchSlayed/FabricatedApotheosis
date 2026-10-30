package dev.shadowsoffire.apotheosis.net;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.RadialUtil;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialState;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record RadialStatePayload(RadialState state) implements CustomPacketPayload {

    public static final Type<RadialStatePayload> TYPE = new Type<>(Apotheosis.loc("radial_state"));

    public static final StreamCodec<ByteBuf, RadialStatePayload> CODEC = RadialState.STREAM_CODEC.map(RadialStatePayload::new, RadialStatePayload::state);

    public RadialStatePayload() {
        this(RadialState.ENABLED);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<RadialStatePayload> {

        @Override
        public Type<RadialStatePayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RadialStatePayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(RadialStatePayload msg, Player player) {
            RadialState.setState(player, msg.state);
        }

        @Override
        public void handleServer(RadialStatePayload msg, ServerPlayer player) {
            RadialUtil.toggleRadialState(player);
        }
    }
}
