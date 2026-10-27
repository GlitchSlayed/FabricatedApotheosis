package dev.shadowsoffire.apotheosis.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Server-authoritative configuration model for Apotheosis.
 * <p>
 * This class has ZERO client or UI dependencies and is completely safe
 * to load on dedicated servers.
 */
public class ApotheosisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "apotheosis.json");

    private static ApotheosisConfig INSTANCE = new ApotheosisConfig();

    // Module toggles
    public boolean enableSpawners = true;
    public boolean enableAttributes = true;
    public boolean enableEnchanting = true;
    public boolean enableAdventure = true;

    // Spawner Module settings
    public boolean spawnerSilkTouchHarvest = true;
    public boolean spawnerRedstoneControl = true;
    public int minSpawnDelayLimit = 20;
    public int maxSpawnDelayLimit = 800;

    // Attributes Module settings
    public boolean displayAttributeDeltas = true;

    // Enchanting Module settings
    public int maxEnchantmentLevelLimit = 100;

    public static ApotheosisConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ApotheosisConfig loaded = GSON.fromJson(reader, ApotheosisConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    LOGGER.info("Loaded configuration from {}", CONFIG_FILE.getName());
                    return;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read configuration file, using defaults", e);
            }
        }
        // Save defaults if file does not exist or failed
        save();
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
                LOGGER.info("Saved configuration to {}", CONFIG_FILE.getName());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration file", e);
        }
    }
}
