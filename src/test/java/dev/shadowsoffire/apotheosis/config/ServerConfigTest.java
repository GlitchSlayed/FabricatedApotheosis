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

    @Test
    public void testExpandedDefaultsAndValidation() {
        ServerConfig config = new ServerConfig();
        assertEquals(2, config.schemaVersion);
        assertEquals(java.util.List.of("minecraft:overworld"), config.generationDimensionWhitelist);
        assertEquals("minecraft:torch", config.torchItem);
        assertEquals(96.0F, config.bossAnnounceRange);
        assertFalse(config.bossAutoAggro);
        assertEquals(12000, config.golemBossDespawnTicks);
        assertTrue(config.applyWorldTierToEndAndWither);
        assertTrue(config.enableSkeletonCrossbows);
        assertTrue(config.enableGemSmashing);
        assertEquals(2, config.upgradeSigilCost);
        assertEquals(225, config.upgradeLevelCost);
        assertEquals(1, config.rerollSigilCost);
        assertEquals(175, config.rerollLevelCost);
        assertTrue(config.enableManualWorldTierChanges);
        assertTrue(config.enableItemLinking);
        assertEquals(100, config.itemLinkingCooldown);
        assertEquals(0.025F, config.protectionFactorPerPoint, 0.0001F);
        assertEquals(0.85F, config.protectionMaxCap, 0.0001F);
        assertEquals(0.02F, config.toughnessBypassResistPerPoint, 0.0001F);
        assertEquals(0.60F, config.toughnessMaxBypassResist, 0.0001F);
        assertEquals(0.50F, config.overhealMaxAbsorptionRatio, 0.0001F);
        assertEquals(0.85F, config.multicritDegradationFactor, 0.0001F);
        assertEquals(30, config.prismaticWebCost);
        assertEquals(5, config.anvilRepairXpCost);
        assertTrue(config.allowTridentCombatEnchantments);
        assertEquals(5, config.chainsawBreaksPerTick);
        assertEquals(60, config.spawnerInstabilityDurationTicks);
        assertEquals(8.0F, config.spawnerInstabilityExplosionRadius, 0.0001F);
        assertEquals(12, config.spawnerUnstableMobCount);
        assertEquals(0.005F, config.capturingDropChancePerLevel, 0.0001F);
        assertEquals(90, config.wandererMaxChance);
        assertEquals(4, config.wandererRngRoll);
        assertEquals(28000, config.wandererDespawnDelay);

        // Bounds clamping and sanitization
        config.torchItem = "INVALID IDENTIFIER!!";
        config.generationDimensionWhitelist = new java.util.ArrayList<>(java.util.Arrays.asList("minecraft:overworld", " ", "BAD ID!", "minecraft:the_nether"));
        config.protectionFactorPerPoint = 5.0F;
        config.protectionMaxCap = 2.0F;
        config.spawnerInstabilityDurationTicks = 2;
        config.chainsawBreaksPerTick = 999;
        config.wandererMaxChance = 500;
        config.validate();

        assertEquals("minecraft:torch", config.torchItem);
        assertEquals(java.util.List.of("minecraft:overworld", "minecraft:the_nether"), config.generationDimensionWhitelist);
        assertEquals(0.20F, config.protectionFactorPerPoint, 0.0001F);
        assertEquals(0.99F, config.protectionMaxCap, 0.0001F);
        assertEquals(10, config.spawnerInstabilityDurationTicks);
        assertEquals(64, config.chainsawBreaksPerTick);
        assertEquals(100, config.wandererMaxChance);
    }

    @Test
    public void testAdventureDimensionWhitelistDefaultAndSync() {
        assertTrue(dev.shadowsoffire.apotheosis.AdventureConfig.DIM_WHITELIST.contains(
            net.minecraft.resources.Identifier.withDefaultNamespace("overworld")),
            "AdventureConfig.DIM_WHITELIST must default to containing minecraft:overworld");

        ServerConfig local = ApotheosisConfig.getLocalServer();
        var origDims = new java.util.ArrayList<>(local.generationDimensionWhitelist);
        float origRange = local.bossAnnounceRange;
        int origSigil = local.upgradeSigilCost;
        boolean origCleave = local.cleaveHitsPlayers;
        try {
            local.generationDimensionWhitelist = new java.util.ArrayList<>(java.util.List.of("minecraft:overworld", "minecraft:the_end"));
            local.bossAnnounceRange = 144.0F;
            local.upgradeSigilCost = 4;
            local.cleaveHitsPlayers = true;
            ApotheosisConfig.syncModuleConfigs();

            assertTrue(dev.shadowsoffire.apotheosis.AdventureConfig.DIM_WHITELIST.contains(
                net.minecraft.resources.Identifier.withDefaultNamespace("the_end")));
            assertEquals(144.0F, dev.shadowsoffire.apotheosis.AdventureConfig.bossAnnounceRange);
            assertEquals(4, dev.shadowsoffire.apotheosis.AdventureConfig.upgradeSigilCost);
            assertTrue(dev.shadowsoffire.apotheosis.AdventureConfig.cleaveHitsPlayers);
        } finally {
            local.generationDimensionWhitelist = origDims;
            local.bossAnnounceRange = origRange;
            local.upgradeSigilCost = origSigil;
            local.cleaveHitsPlayers = origCleave;
            ApotheosisConfig.syncModuleConfigs();
        }
    }
}
