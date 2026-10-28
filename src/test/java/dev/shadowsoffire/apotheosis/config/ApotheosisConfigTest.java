package dev.shadowsoffire.apotheosis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApotheosisConfigTest {

    @BeforeEach
    public void setup(@TempDir Path tempDir) {
        ApotheosisConfig.setConfigDir(tempDir);
        ApotheosisConfig.clearRemoteServerConfig();
    }

    @Test
    public void testAtomicWrite(@TempDir Path tempDir) throws IOException {
        Path targetFile = tempDir.resolve("test_config.json");
        String content = "{\"test\": 123}";

        ApotheosisConfig.atomicWrite(targetFile, content);

        assertTrue(Files.exists(targetFile));
        assertEquals(content, Files.readString(targetFile));

        // Verify no leftover .tmp files
        File[] tmpFiles = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".tmp"));
        assertEquals(0, tmpFiles != null ? tmpFiles.length : 0);
    }

    @Test
    public void testRemoteOverrideAndDisconnectClearing() {
        // Ensure starting in local state
        ApotheosisConfig.clearRemoteServerConfig();
        assertFalse(ApotheosisConfig.isRemoteServer());

        int localSilkLevel = ApotheosisConfig.getLocalServer().spawnerSilkLevel;
        assertEquals(localSilkLevel, ApotheosisConfig.getServer().spawnerSilkLevel);

        // Simulate remote server sending authoritative settings
        ServerConfig remoteConfig = new ServerConfig();
        remoteConfig.spawnerSilkLevel = 7;
        remoteConfig.knowledgeMultiplier = 16.0F;

        ApotheosisConfig.setRemoteServerConfig(remoteConfig);
        assertTrue(ApotheosisConfig.isRemoteServer());
        assertEquals(7, ApotheosisConfig.getServer().spawnerSilkLevel, "Remote authoritative setting must override local setting");
        assertEquals(16.0F, ApotheosisConfig.getServer().knowledgeMultiplier);

        // Simulate disconnect from remote server
        ApotheosisConfig.clearRemoteServerConfig();
        assertFalse(ApotheosisConfig.isRemoteServer());
        assertEquals(localSilkLevel, ApotheosisConfig.getServer().spawnerSilkLevel, "Disconnect must clear remote state and restore local settings");
    }

    @Test
    public void testLoadAndSavePersistence() {
        // Change local server setting and save
        ApotheosisConfig.getLocalServer().spawnerSilkLevel = 4;
        ApotheosisConfig.getClient().bossHealthBarGlow = false;

        ApotheosisConfig.saveLocalServer();
        ApotheosisConfig.saveClient();

        assertTrue(Files.exists(ApotheosisConfig.getServerFile().toPath()));
        assertTrue(Files.exists(ApotheosisConfig.getClientFile().toPath()));

        // Reload from disk
        ApotheosisConfig.load();

        assertEquals(4, ApotheosisConfig.getLocalServer().spawnerSilkLevel);
        assertFalse(ApotheosisConfig.getClient().bossHealthBarGlow);
    }
}
