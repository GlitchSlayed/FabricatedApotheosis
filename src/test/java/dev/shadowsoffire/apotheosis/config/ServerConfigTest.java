package dev.shadowsoffire.apotheosis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ServerConfigTest {

    @Test
    public void testDefaultsAreValid() {
        ServerConfig config = new ServerConfig();
        assertEquals(ServerConfig.CURRENT_SCHEMA_VERSION, config.schemaVersion);
        assertTrue(config.enableSpawners);
        assertTrue(config.enableAttributes);
        assertTrue(config.enableEnchanting);
        assertTrue(config.enableAdventure);

        assertEquals(1, config.spawnerSilkLevel);
        assertEquals(100, config.spawnerSilkDamage);
        assertEquals(20, config.minSpawnDelayLimit);
        assertEquals(800, config.maxSpawnDelayLimit);
        assertEquals(100, config.maxEnchantmentLevelLimit);
        assertEquals(4.0F, config.knowledgeMultiplier);
    }

    @Test
    public void testValidationAndClamping() {
        ServerConfig config = new ServerConfig();

        // Test silk touch level clamping
        config.spawnerSilkLevel = 500;
        config.validate();
        assertEquals(127, config.spawnerSilkLevel);

        config.spawnerSilkLevel = -50;
        config.validate();
        assertEquals(-1, config.spawnerSilkLevel);

        // Test min/max delay constraint
        config.maxSpawnDelayLimit = 500;
        config.minSpawnDelayLimit = 900;
        config.validate();
        assertEquals(500, config.minSpawnDelayLimit, "minSpawnDelayLimit must be clamped to maxSpawnDelayLimit");

        // Test enchantment level cap bounds
        config.maxEnchantmentLevelLimit = 1000;
        config.validate();
        assertEquals(255, config.maxEnchantmentLevelLimit);

        config.maxEnchantmentLevelLimit = -5;
        config.validate();
        assertEquals(1, config.maxEnchantmentLevelLimit);

        // Test multiplier bounds
        config.knowledgeMultiplier = 5000.0F;
        config.validate();
        assertEquals(1024.0F, config.knowledgeMultiplier);
    }

    @Test
    public void testNetworkStreamCodecSerialization() {
        ServerConfig original = new ServerConfig();
        original.spawnerSilkLevel = 5;
        original.spawnerSilkDamage = 250;
        original.spawnersDropEmpty = true;
        original.knowledgeMultiplier = 8.5F;
        original.maxEnchantmentLevelLimit = 150;

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), null);
        ServerConfig.STREAM_CODEC.encode(buf, original);

        ServerConfig decoded = ServerConfig.STREAM_CODEC.decode(buf);
        assertEquals(5, decoded.spawnerSilkLevel);
        assertEquals(250, decoded.spawnerSilkDamage);
        assertTrue(decoded.spawnersDropEmpty);
        assertEquals(8.5F, decoded.knowledgeMultiplier);
        assertEquals(150, decoded.maxEnchantmentLevelLimit);
    }
}
