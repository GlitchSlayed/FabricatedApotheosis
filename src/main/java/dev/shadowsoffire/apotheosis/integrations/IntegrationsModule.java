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
        if (PlatformHelper.isModLoaded("jei")) {
            LOGGER.info("Just Enough Items (JEI) detected, enabling recipe viewer support");
        }
        if (PlatformHelper.isModLoaded("jade")) {
            LOGGER.info("Jade detected, enabling block & spawner inspection support");
        }
        if (PlatformHelper.isModLoaded("accessories") || PlatformHelper.isModLoaded("trinkets")) {
            LOGGER.info("Accessory slot provider detected, enabling accessory modifier bridge");
        }
    }
}
