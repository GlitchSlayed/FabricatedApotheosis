package dev.shadowsoffire.apotheosis.net;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.effect.AttributeToggleAffix;
import dev.shadowsoffire.apotheosis.attachments.AttributeToggles;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Payload for {@link AttributeToggles} state.
 */
public record AttributeTogglesPayload(AttributeToggles toggles) implements CustomPacketPayload {

    public static final Type<AttributeTogglesPayload> TYPE = new Type<>(Apotheosis.loc("attribute_toggles"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeTogglesPayload> CODEC = AttributeToggles.STREAM_CODEC.map(AttributeTogglesPayload::new, AttributeTogglesPayload::toggles);

    public AttributeTogglesPayload() {
        this(AttributeToggles.EMPTY);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<AttributeTogglesPayload> {

        @Override
        public Type<AttributeTogglesPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, AttributeTogglesPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(AttributeTogglesPayload msg, Player player) {
            AttributeToggleAffix.setToggles(player, msg.toggles);
        }

        @Override
        public void handleServer(AttributeTogglesPayload msg, ServerPlayer player) {
            AttributeToggleAffix.handleToggle(player);
        }
    }
}
