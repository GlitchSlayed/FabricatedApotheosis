package dev.shadowsoffire.apotheosis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.placebo.client.PlaceboClientPayloadHandler;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client entrypoint for Fabricated Apotheosis.
 */
public class ApotheosisClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Fabricated Apotheosis client subsystems");
        PayloadHelper.setClientHandler(new PlaceboClientPayloadHandler());
        // Client rendering, screen factories, and tooltips will be initialized here
    }
}
