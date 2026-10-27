package dev.shadowsoffire.apotheosis.integrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.platform.PlatformHelper;

/**
 * Optional third-party integrations (ModMenu, Patchouli, Jade, EMI/JEI).
 */
public class IntegrationsModule {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Integrations");

    public static void init() {
        if (PlatformHelper.isModLoaded("patchouli")) {
            LOGGER.info("Patchouli detected, enabling book documentation bridge");
        }
        if (PlatformHelper.isModLoaded("gateways")) {
            LOGGER.info("Gateways To Eternity detected, enabling gateway compatibility");
        }
    }
}
