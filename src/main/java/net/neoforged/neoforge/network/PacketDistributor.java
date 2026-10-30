package net.neoforged.neoforge.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class PacketDistributor {

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (player != null && player.connection != null) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
