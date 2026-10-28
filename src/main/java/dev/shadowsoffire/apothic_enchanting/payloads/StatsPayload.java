package dev.shadowsoffire.apothic_enchanting.payloads;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.table.EnchantmentTableStats;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StatsPayload(EnchantmentTableStats stats) implements CustomPacketPayload {

    public static final Type<StatsPayload> TYPE = new Type<>(ApothicEnchanting.loc("stats"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StatsPayload> CODEC = EnchantmentTableStats.STREAM_CODEC.map(StatsPayload::new, StatsPayload::stats);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<StatsPayload> {

        @Override
        public Type<StatsPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, StatsPayload> getCodec() {
            return CODEC;
        }
    }

}
