package dev.shadowsoffire.apotheosis.net;

import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record RerollResultPayload(DynamicHolder<Affix> newAffix) implements CustomPacketPayload {

    public static final Type<RerollResultPayload> TYPE = new Type<>(Apotheosis.loc("reroll_result"));

    public static final StreamCodec<ByteBuf, RerollResultPayload> CODEC = AffixRegistry.INSTANCE.holderStreamCodec().map(RerollResultPayload::new, RerollResultPayload::newAffix);

    public static Consumer<DynamicHolder<Affix>> clientHandler = affix -> {};

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<RerollResultPayload> {

        @Override
        public Type<RerollResultPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RerollResultPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(RerollResultPayload msg, Player player) {
            clientHandler.accept(msg.newAffix());
        }
    }
}
