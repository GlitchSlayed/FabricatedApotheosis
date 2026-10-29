package dev.shadowsoffire.apothic_spawners;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apotheosis.config.ServerConfig;

public class ASConfig {

    public static int spawnerSilkLevel = 1;
    public static int spawnerSilkDamage = 100;
    public static boolean spawnersDropEmpty = false;
    public static int entityDespawnDelay = 600;

    public static void load() {
        ServerConfig cfg = ApotheosisConfig.getServer();
        if (cfg != null) {
            spawnerSilkLevel = cfg.spawnerSilkLevel;
            spawnerSilkDamage = cfg.spawnerSilkDamage;
            spawnersDropEmpty = cfg.spawnersDropEmpty;
            entityDespawnDelay = cfg.entityDespawnDelay;
        }
    }
}
