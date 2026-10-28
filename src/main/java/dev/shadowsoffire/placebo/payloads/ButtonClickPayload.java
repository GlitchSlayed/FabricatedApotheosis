package dev.shadowsoffire.placebo.payloads;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ButtonClickPayload(int button) implements CustomPacketPayload {

    public static final Type<ButtonClickPayload> TYPE = new Type<>(Placebo.loc("button_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ButtonClickPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ButtonClickPayload::button,
        ButtonClickPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public interface IButtonContainer {
        void onButtonClick(int id);
    }

    public static class Provider implements PayloadProvider<ButtonClickPayload> {

        @Override
        public Type<ButtonClickPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ButtonClickPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleServer(ButtonClickPayload msg, ServerPlayer player) {
            if (player.containerMenu instanceof IButtonContainer btnContainer) {
                btnContainer.onButtonClick(msg.button);
            }
        }
    }
}
