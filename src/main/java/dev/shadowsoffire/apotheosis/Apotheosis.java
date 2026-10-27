package dev.shadowsoffire.apotheosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.attributes.AttributesModule;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apotheosis.docs.DocsSupport;
import dev.shadowsoffire.apotheosis.enchanting.EnchantingModule;
import dev.shadowsoffire.apotheosis.integrations.IntegrationsModule;
import dev.shadowsoffire.apotheosis.placebo.PlaceboSupport;
import dev.shadowsoffire.apotheosis.platform.PlatformHelper;
import dev.shadowsoffire.apotheosis.spawners.SpawnersModule;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

/**
 * Main entrypoint for the Fabricated Apotheosis ecosystem.
 */
public class Apotheosis implements ModInitializer {

    public static final String MODID = "apotheosis";
    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Fabricated Apotheosis ecosystem on Minecraft 26.3 Fabric");

        // 1. Load server-authoritative configuration
        ApotheosisConfig.load();

        // 2. Initialize platform abstraction
        PlatformHelper.init();

        // 3. Initialize Placebo compatibility utilities
        PlaceboSupport.init();

        // 4. Initialize approved modules according to configuration
        if (ApotheosisConfig.get().enableAttributes) {
            AttributesModule.init();
        }

        if (ApotheosisConfig.get().enableEnchanting) {
            EnchantingModule.init();
        }

        if (ApotheosisConfig.get().enableSpawners) {
            SpawnersModule.init();
        }

        if (ApotheosisConfig.get().enableAdventure) {
            AdventureModule.init();
        }

        // 5. Initialize optional integrations and documentation support
        IntegrationsModule.init();
        DocsSupport.init();

        LOGGER.info("Fabricated Apotheosis core initialization complete");
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
