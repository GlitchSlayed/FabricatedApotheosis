package dev.shadowsoffire.placebo.client;

import dev.shadowsoffire.placebo.network.ClientPayloadHandler;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-side implementation of payload receiver registration and server packet sending.
 */
public class PlaceboClientPayloadHandler implements ClientPayloadHandler {

    @Override
    public <T extends CustomPacketPayload> void registerClient(CustomPacketPayload.Type<T> type, PayloadProvider<T> provider) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            context.client().execute(() -> {
                provider.handleClient(payload, context.player());
            });
        });
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
