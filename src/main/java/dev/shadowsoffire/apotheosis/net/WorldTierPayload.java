package dev.shadowsoffire.apotheosis.net;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;

public record WorldTierPayload(WorldTier tier) implements CustomPacketPayload {

    public static final Type<WorldTierPayload> TYPE = new Type<>(Apotheosis.loc("world_tier"));

    public static final StreamCodec<ByteBuf, WorldTierPayload> CODEC = WorldTier.STREAM_CODEC.map(WorldTierPayload::new, WorldTierPayload::tier);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<WorldTierPayload> {

        @Override
        public Type<WorldTierPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, WorldTierPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(WorldTierPayload msg, Player player) {
            WorldTier.setTier(player, msg.tier);
        }

        @Override
        public void handleServer(WorldTierPayload msg, ServerPlayer player) {
            if (AdventureConfig.enableManualWorldTierChanges) {
                if (WorldTier.isUnlocked(player, msg.tier)) {
                    WorldTier.setTier(player, msg.tier);
                }
            } else if (player.getStats().getValue(Stats.CUSTOM.get(Apoth.Stats.WORLD_TIERS_ACTIVATED)) == 0) {
                player.awardStat(Apoth.Stats.WORLD_TIERS_ACTIVATED);
            } else if (player.connection != null) {
                player.connection.disconnect(Apotheosis.lang("disconnect", "tier_changes_disabled"));
            }
        }
    }
}
