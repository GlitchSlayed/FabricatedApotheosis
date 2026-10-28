package dev.shadowsoffire.apotheosis.enchanting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;

/**
 * Apothic Enchanting module initializer.
 */
public class EnchantingModule {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Enchanting");

    public static void init() {
        ApothicEnchanting.init();
        LOGGER.info("Apothic Enchanting module initialized successfully");
    }
}
