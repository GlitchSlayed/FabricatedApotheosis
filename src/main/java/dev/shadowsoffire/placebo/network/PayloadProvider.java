package dev.shadowsoffire.placebo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Encapsulates payload registration and packet handlers.
 *
 * @param <T> Payload type.
 */
public interface PayloadProvider<T extends CustomPacketPayload> {

    CustomPacketPayload.Type<T> getType();

    StreamCodec<? super RegistryFriendlyByteBuf, T> getCodec();

    default void handleClient(T msg, Player player) {}

    default void handleServer(T msg, ServerPlayer player) {}
}
