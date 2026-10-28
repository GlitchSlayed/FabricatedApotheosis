package dev.shadowsoffire.apothic_enchanting.payloads;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.table.RavenEnchantmentMenu;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client->server commit of the three slider values on the Enchanting Table of the Raven's screen.
 * Validation + clamping happens server-side in {@link RavenEnchantmentMenu#setPlayerStats}.
 */
public record SetRavenStatsPayload(int eterna, int quanta, int arcana) implements CustomPacketPayload {

    public static final Type<SetRavenStatsPayload> TYPE = new Type<>(ApothicEnchanting.loc("set_raven_stats"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetRavenStatsPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SetRavenStatsPayload::eterna,
        ByteBufCodecs.INT, SetRavenStatsPayload::quanta,
        ByteBufCodecs.INT, SetRavenStatsPayload::arcana,
        SetRavenStatsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<SetRavenStatsPayload> {

        @Override
        public Type<SetRavenStatsPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, SetRavenStatsPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleServer(SetRavenStatsPayload msg, ServerPlayer player) {
            if (player.containerMenu instanceof RavenEnchantmentMenu menu) {
                menu.setPlayerStats(msg.eterna(), msg.quanta(), msg.arcana());
            }
        }
    }

}
