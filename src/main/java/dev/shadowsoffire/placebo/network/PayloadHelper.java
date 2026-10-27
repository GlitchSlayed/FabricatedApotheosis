package dev.shadowsoffire.placebo.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric networking abstraction for registering and dispatching custom payload packets.
 * Dedicated-server safe: client networking registration and dispatch are delegated to {@link ClientPayloadHandler}.
 */
public class PayloadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("Placebo/Networking");
    private static final Map<CustomPacketPayload.Type<?>, PayloadProvider<?>> PROVIDERS = new ConcurrentHashMap<>();
    private static final List<Consumer<ClientPayloadHandler>> PENDING_CLIENT_REGISTRATIONS = new ArrayList<>();
    private static volatile ClientPayloadHandler clientHandler = null;

    public static synchronized void setClientHandler(ClientPayloadHandler handler) {
        clientHandler = handler;
        for (Consumer<ClientPayloadHandler> reg : PENDING_CLIENT_REGISTRATIONS) {
            reg.accept(handler);
        }
        PENDING_CLIENT_REGISTRATIONS.clear();
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void registerPayload(PayloadProvider<T> provider) {
        CustomPacketPayload.Type<T> type = provider.getType();
        if (PROVIDERS.putIfAbsent(type, provider) != null) {
            throw new IllegalArgumentException("Duplicate payload provider registration for type: " + type.id());
        }

        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = provider.getCodec();

        // Register on both S2C and C2S channels for Fabric networking
        try {
            PayloadTypeRegistry.clientboundPlay().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>) codec);
            PayloadTypeRegistry.serverboundPlay().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>) codec);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Payload {} already registered on channel: {}", type.id(), e.getMessage());
        }

        // Server-side receiver
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            context.server().execute(() -> {
                provider.handleServer(payload, context.player());
            });
        });

        // Client-side receiver
        synchronized (PayloadHelper.class) {
            if (clientHandler != null) {
                clientHandler.registerClient(type, provider);
            } else {
                PENDING_CLIENT_REGISTRATIONS.add(h -> h.registerClient(type, provider));
            }
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPayloadHandler handler = clientHandler;
        if (handler != null) {
            handler.sendToServer(payload);
        } else {
            LOGGER.warn("Attempted to send packet {} to server from environment without client networking handler!", payload.type().id());
        }
    }
}
