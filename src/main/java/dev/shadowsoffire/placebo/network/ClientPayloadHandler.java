package dev.shadowsoffire.placebo.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Interface implemented on the client source set to bind Fabric client-side networking.
 */
public interface ClientPayloadHandler {

    <T extends CustomPacketPayload> void registerClient(CustomPacketPayload.Type<T> type, PayloadProvider<T> provider);

    void sendToServer(CustomPacketPayload payload);
}
