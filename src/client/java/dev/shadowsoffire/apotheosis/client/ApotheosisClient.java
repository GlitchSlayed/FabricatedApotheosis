package dev.shadowsoffire.apotheosis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.placebo.client.PlaceboClientPayloadHandler;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Client entrypoint for Fabricated Apotheosis.
 */
public class ApotheosisClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Fabricated Apotheosis client subsystems");
        PayloadHelper.setClientHandler(new PlaceboClientPayloadHandler());

        // Clear remote server settings on disconnect to guarantee no stale state persists
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ApotheosisConfig.clearRemoteServerConfig();
        });
    }
}
