package dev.shadowsoffire.apotheosis.net;

import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseMenu;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record GemCaseSelectPayload(DynamicHolder<Gem> gem) implements CustomPacketPayload {

    public static final Type<GemCaseSelectPayload> TYPE = new Type<>(Apotheosis.loc("gem_case_select"));

    public static final StreamCodec<ByteBuf, GemCaseSelectPayload> CODEC = GemRegistry.INSTANCE.holderStreamCodec().map(GemCaseSelectPayload::new, GemCaseSelectPayload::gem);

    public static Consumer<DynamicHolder<Gem>> clientHandler = gem -> {};

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<GemCaseSelectPayload> {

        @Override
        public Type<GemCaseSelectPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, GemCaseSelectPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(GemCaseSelectPayload msg, Player player) {
            clientHandler.accept(msg.gem());
        }

        @Override
        public void handleServer(GemCaseSelectPayload msg, ServerPlayer player) {
            if (player.containerMenu instanceof GemCaseMenu menu) {
                menu.setSelectedGem(msg.gem());
            }
        }
    }
}
