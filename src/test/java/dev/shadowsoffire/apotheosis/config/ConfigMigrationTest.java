package dev.shadowsoffire.apotheosis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;

public class ConfigMigrationTest {

    @Test
    public void testSchemaV0ToV1Migration() {
        JsonObject v0Json = new JsonObject();
        v0Json.addProperty("spawnerSilkTouchHarvest", false);
        v0Json.addProperty("minSpawnDelayLimit", 30);

        ServerConfig serverConfig = new ServerConfig();
        boolean migrated = ConfigMigration.migrateServerJson(v0Json, serverConfig);

        assertTrue(migrated);
        assertEquals(ServerConfig.CURRENT_SCHEMA_VERSION, serverConfig.schemaVersion);
        assertEquals(-1, serverConfig.spawnerSilkLevel, "spawnerSilkTouchHarvest=false should map to spawnerSilkLevel=-1");
    }

    @Test
    public void testLegacyCfgMigration(@TempDir Path tempDir) throws IOException {
        File legacyFile = tempDir.resolve("spawner.cfg").toFile();
        Files.writeString(legacyFile.toPath(), """
            # Configuration file
            general {
                I:"Spawner Silk Level"=3
                I:"Spawner Silk Damage"=450
                B:"Spawners Drop Empty"=true
                I:"Entity Despawn Delay"=1200
            }
            """);

        ServerConfig serverConfig = new ServerConfig();
        ClientConfig clientConfig = new ClientConfig();

        boolean migrated = ConfigMigration.migrateLegacyCfg(tempDir.toFile(), serverConfig, clientConfig);

        assertTrue(migrated);
        assertEquals(3, serverConfig.spawnerSilkLevel);
        assertEquals(450, serverConfig.spawnerSilkDamage);
        assertTrue(serverConfig.spawnersDropEmpty);
        assertEquals(1200, serverConfig.entityDespawnDelay);
    }
}
