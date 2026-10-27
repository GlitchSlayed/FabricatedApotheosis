package dev.shadowsoffire.apotheosis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client entrypoint for Fabricated Apotheosis.
 */
public class ApotheosisClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Fabricated Apotheosis client subsystems");
        // Client rendering, screen factories, and tooltips will be initialized here
    }
}
