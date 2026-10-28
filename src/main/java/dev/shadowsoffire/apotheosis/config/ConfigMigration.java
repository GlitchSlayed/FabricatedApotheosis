package dev.shadowsoffire.apotheosis.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles schema upgrades and migration of legacy configuration files.
 */
public class ConfigMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/ConfigMigration");

    /**
     * Migrates an unversioned or older JSON object into the current {@link ServerConfig}.
     */
    public static boolean migrateServerJson(JsonObject json, ServerConfig config) {
        int version = json.has("schemaVersion") ? json.get("schemaVersion").getAsInt() : 0;
        if (version >= ServerConfig.CURRENT_SCHEMA_VERSION) {
            return false;
        }

        LOGGER.info("Migrating ServerConfig from schema version {} to {}", version, ServerConfig.CURRENT_SCHEMA_VERSION);

        // Version 0 migration: legacy field remapping from initial skeleton
        if (version < 1) {
            if (json.has("spawnerSilkTouchHarvest") && !json.has("spawnerSilkLevel")) {
                boolean harvest = json.get("spawnerSilkTouchHarvest").getAsBoolean();
                config.spawnerSilkLevel = harvest ? 1 : -1;
            }
        }

        config.schemaVersion = ServerConfig.CURRENT_SCHEMA_VERSION;
        return true;
    }

    /**
     * Attempts to migrate legacy NeoForge .cfg files from the given directory into the server/client configs.
     */
    public static boolean migrateLegacyCfg(File legacyDir, ServerConfig serverConfig, ClientConfig clientConfig) {
        if (!legacyDir.exists() || !legacyDir.isDirectory()) {
            return false;
        }

        File[] files = legacyDir.listFiles((dir, name) -> name.endsWith(".cfg"));
        if (files == null || files.length == 0) {
            return false;
        }

        LOGGER.info("Found {} legacy .cfg file(s) in {}. Attempting migration...", files.length, legacyDir.getPath());
        boolean modified = false;

        for (File file : files) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("#") || line.isEmpty()) continue;

                    // Parse legacy Forge format, e.g. "S:key=val", "I:key=val", "B:key=val", or "key=val"
                    int eqIndex = line.indexOf('=');
                    if (eqIndex > 0) {
                        String keyPart = line.substring(0, eqIndex).trim();
                        String valPart = line.substring(eqIndex + 1).trim();
                        if (keyPart.length() > 2 && keyPart.charAt(1) == ':') {
                            keyPart = keyPart.substring(2).trim();
                        }
                        keyPart = keyPart.replace("\"", "").trim();

                        if ("Spawner Silk Level".equalsIgnoreCase(keyPart)) {
                            serverConfig.spawnerSilkLevel = Integer.parseInt(valPart);
                            modified = true;
                        } else if ("Spawner Silk Damage".equalsIgnoreCase(keyPart)) {
                            serverConfig.spawnerSilkDamage = Integer.parseInt(valPart);
                            modified = true;
                        } else if ("Spawners Drop Empty".equalsIgnoreCase(keyPart)) {
                            serverConfig.spawnersDropEmpty = Boolean.parseBoolean(valPart);
                            modified = true;
                        } else if ("Entity Despawn Delay".equalsIgnoreCase(keyPart)) {
                            serverConfig.entityDespawnDelay = Integer.parseInt(valPart);
                            modified = true;
                        } else if ("Ancient Knowledge Multiplier".equalsIgnoreCase(keyPart)) {
                            serverConfig.knowledgeMultiplier = Float.parseFloat(valPart);
                            modified = true;
                        } else if ("Enable Attributes GUI".equalsIgnoreCase(keyPart)) {
                            clientConfig.enableAttributesGui = Boolean.parseBoolean(valPart);
                            modified = true;
                        } else if ("Enable Potion Tooltips".equalsIgnoreCase(keyPart)) {
                            clientConfig.enablePotionTooltips = Boolean.parseBoolean(valPart);
                            modified = true;
                        } else if ("Show Enchanted Book Metadata".equalsIgnoreCase(keyPart)) {
                            clientConfig.showEnchantedBookMetadata = Boolean.parseBoolean(valPart);
                            modified = true;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to migrate legacy config file: {}", file.getName(), e);
            }
        }

        return modified;
    }
}
