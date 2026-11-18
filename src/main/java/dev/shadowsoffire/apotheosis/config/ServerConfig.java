package dev.shadowsoffire.apotheosis.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * Server-authoritative gameplay configuration schema.
 * Contains gameplay rules, math formulas, limits, and module toggles.
 */
public class ServerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/ServerConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final int CURRENT_SCHEMA_VERSION = 2;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;

    // --- Module Toggles (RESTART_REQUIRED) ---
    public boolean enableSpawners = true;
    public boolean enableAttributes = true;
    public boolean enableEnchanting = true;
    public boolean enableAdventure = true;

    // --- Spawners Gameplay (LIVE) ---
    public int spawnerSilkLevel = 1;
    public int spawnerSilkDamage = 100;
    public boolean spawnersDropEmpty = false;
    public int entityDespawnDelay = 600;
    public boolean spawnerRedstoneControl = true;
    public int minSpawnDelayLimit = 20;
    public int maxSpawnDelayLimit = 800;
    public int spawnerInstabilityDurationTicks = 60;
    public float spawnerInstabilityExplosionRadius = 8.0F;
    public int spawnerUnstableMobCount = 12;
    public float capturingDropChancePerLevel = 0.005F;

    // --- Enchanting Gameplay (LIVE) ---
    public int maxEnchantmentLevelLimit = 100;
    public int maxBookshelfRange = 2;
    public int prismaticWebCost = 30;
    public int anvilRepairXpCost = 5;
    public boolean allowTridentCombatEnchantments = true;
    public int chainsawBreaksPerTick = 5;

    // --- Attributes & Combat Gameplay (LIVE) ---
    public float knowledgeMultiplier = 4.0F;
    public float negativeArmorFactor = 0.015F;
    public float critDamageModifierCap = 10.0F;
    public float protectionFactorPerPoint = 0.025F;
    public float protectionMaxCap = 0.85F;
    public float toughnessBypassResistPerPoint = 0.02F;
    public float toughnessMaxBypassResist = 0.60F;
    public float overhealMaxAbsorptionRatio = 0.50F;
    public float multicritDegradationFactor = 0.85F;

    // --- Adventure, Bosses & Affixes Gameplay (LIVE) ---
    public int bossSpawnCooldown = 3600;
    public float bossAnnounceRange = 96.0F;
    public boolean bossGlowOnSpawn = true;
    public boolean bossAutoAggro = false;
    public boolean curseBossItems = false;
    public int golemBossDespawnTicks = 12000;
    public boolean applyWorldTierToEndAndWither = true;
    public boolean enableSkeletonCrossbows = true;
    public boolean cleaveHitsPlayers = false;
    public String torchItem = "minecraft:torch";
    public boolean enableGemSmashing = true;

    // --- Augmenting Table (LIVE) ---
    public int upgradeSigilCost = 2;
    public int upgradeLevelCost = 225;
    public int rerollSigilCost = 1;
    public int rerollLevelCost = 175;

    // --- World Tiers & QoL (LIVE) ---
    public boolean enableManualWorldTierChanges = true;
    public boolean enableItemLinking = true;
    public int itemLinkingCooldown = 100;

    // --- Worldgen, Rogue Spawners & Wanderer (LIVE) ---
    public List<String> generationDimensionWhitelist = new ArrayList<>(List.of("minecraft:overworld"));
    public float spawnerValueChance = 0.11F;
    public boolean worldGenDebug = false;
    public boolean undergroundTrader = true;
    public int wandererMaxChance = 90;
    public int wandererRngRoll = 4;
    public int wandererDespawnDelay = 28000;

    /**
     * Validates and normalizes all configuration values, clamping them to safe limits.
     * Logs actionable diagnostics if values are out of bounds.
     */
    public void validate() {
        this.spawnerSilkLevel = clampInt("spawnerSilkLevel", this.spawnerSilkLevel, -1, 127);
        this.spawnerSilkDamage = clampInt("spawnerSilkDamage", this.spawnerSilkDamage, 0, 100000);
        this.entityDespawnDelay = clampInt("entityDespawnDelay", this.entityDespawnDelay, 0, 24000);
        this.maxSpawnDelayLimit = clampInt("maxSpawnDelayLimit", this.maxSpawnDelayLimit, 20, 100000);
        this.minSpawnDelayLimit = clampInt("minSpawnDelayLimit", this.minSpawnDelayLimit, 0, this.maxSpawnDelayLimit);
        this.spawnerInstabilityDurationTicks = clampInt("spawnerInstabilityDurationTicks", this.spawnerInstabilityDurationTicks, 10, 1200);
        this.spawnerInstabilityExplosionRadius = clampFloat("spawnerInstabilityExplosionRadius", this.spawnerInstabilityExplosionRadius, 0.0F, 32.0F);
        this.spawnerUnstableMobCount = clampInt("spawnerUnstableMobCount", this.spawnerUnstableMobCount, 0, 64);
        this.capturingDropChancePerLevel = clampFloat("capturingDropChancePerLevel", this.capturingDropChancePerLevel, 0.0F, 1.0F);

        this.maxEnchantmentLevelLimit = clampInt("maxEnchantmentLevelLimit", this.maxEnchantmentLevelLimit, 1, 255);
        this.maxBookshelfRange = clampInt("maxBookshelfRange", this.maxBookshelfRange, 1, 5);
        this.prismaticWebCost = clampInt("prismaticWebCost", this.prismaticWebCost, 0, 255);
        this.anvilRepairXpCost = clampInt("anvilRepairXpCost", this.anvilRepairXpCost, 0, 255);
        this.chainsawBreaksPerTick = clampInt("chainsawBreaksPerTick", this.chainsawBreaksPerTick, 1, 64);

        this.knowledgeMultiplier = clampFloat("knowledgeMultiplier", this.knowledgeMultiplier, 1.0F, 1024.0F);
        this.negativeArmorFactor = clampFloat("negativeArmorFactor", this.negativeArmorFactor, 0.0F, 1.0F);
        this.critDamageModifierCap = clampFloat("critDamageModifierCap", this.critDamageModifierCap, 1.0F, 100.0F);
        this.protectionFactorPerPoint = clampFloat("protectionFactorPerPoint", this.protectionFactorPerPoint, 0.001F, 0.20F);
        this.protectionMaxCap = clampFloat("protectionMaxCap", this.protectionMaxCap, 0.0F, 0.99F);
        this.toughnessBypassResistPerPoint = clampFloat("toughnessBypassResistPerPoint", this.toughnessBypassResistPerPoint, 0.0F, 0.20F);
        this.toughnessMaxBypassResist = clampFloat("toughnessMaxBypassResist", this.toughnessMaxBypassResist, 0.0F, 1.0F);
        this.overhealMaxAbsorptionRatio = clampFloat("overhealMaxAbsorptionRatio", this.overhealMaxAbsorptionRatio, 0.0F, 5.0F);
        this.multicritDegradationFactor = clampFloat("multicritDegradationFactor", this.multicritDegradationFactor, 0.10F, 1.0F);

        this.bossSpawnCooldown = clampInt("bossSpawnCooldown", this.bossSpawnCooldown, 0, 720000);
        this.bossAnnounceRange = clampFloat("bossAnnounceRange", this.bossAnnounceRange, 0.0F, 1024.0F);
        this.golemBossDespawnTicks = clampInt("golemBossDespawnTicks", this.golemBossDespawnTicks, 200, 720000);
        if (this.torchItem == null || this.torchItem.isBlank() || Identifier.tryParse(this.torchItem.trim()) == null) {
            LOGGER.warn("torchItem '{}' is invalid. Resetting to minecraft:torch.", this.torchItem);
            this.torchItem = "minecraft:torch";
        } else {
            this.torchItem = this.torchItem.trim();
        }

        this.upgradeSigilCost = clampInt("upgradeSigilCost", this.upgradeSigilCost, 0, 64);
        this.upgradeLevelCost = clampInt("upgradeLevelCost", this.upgradeLevelCost, 0, 65536);
        this.rerollSigilCost = clampInt("rerollSigilCost", this.rerollSigilCost, 0, 64);
        this.rerollLevelCost = clampInt("rerollLevelCost", this.rerollLevelCost, 0, 65536);

        this.itemLinkingCooldown = clampInt("itemLinkingCooldown", this.itemLinkingCooldown, 0, 65536);

        if (this.generationDimensionWhitelist == null) {
            LOGGER.warn("generationDimensionWhitelist was null. Resetting to [minecraft:overworld].");
            this.generationDimensionWhitelist = new ArrayList<>(List.of("minecraft:overworld"));
        } else {
            List<String> sanitized = new ArrayList<>();
            for (String entry : this.generationDimensionWhitelist) {
                if (entry == null || entry.isBlank()) continue;
                String trimmed = entry.trim();
                if (Identifier.tryParse(trimmed) != null) {
                    sanitized.add(trimmed);
                } else {
                    LOGGER.warn("Ignoring malformed dimension whitelist identifier '{}'.", entry);
                }
            }
            this.generationDimensionWhitelist = sanitized;
        }
        this.spawnerValueChance = clampFloat("spawnerValueChance", this.spawnerValueChance, 0.0F, 1.0F);
        this.wandererMaxChance = clampInt("wandererMaxChance", this.wandererMaxChance, 1, 100);
        this.wandererRngRoll = clampInt("wandererRngRoll", this.wandererRngRoll, 1, 100);
        this.wandererDespawnDelay = clampInt("wandererDespawnDelay", this.wandererDespawnDelay, 1200, 240000);
    }

    private static int clampInt(String name, int value, int min, int max) {
        if (value < min || value > max) {
            int clamped = Math.clamp(value, min, max);
            LOGGER.warn("{} '{}' out of bounds [{}, {}]. Clamping to {}.", name, value, min, max, clamped);
            return clamped;
        }
        return value;
    }

    private static float clampFloat(String name, float value, float min, float max) {
        if (Float.isNaN(value) || value < min || value > max) {
            float clamped = Float.isNaN(value) ? min : Math.clamp(value, min, max);
            LOGGER.warn("{} '{}' out of bounds [{}, {}]. Clamping to {}.", name, value, min, max, clamped);
            return clamped;
        }
        return value;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfig> STREAM_CODEC = StreamCodec.of(
        (buf, config) -> ByteBufCodecs.STRING_UTF8.encode(buf, GSON.toJson(config)),
        buf -> {
            String json = ByteBufCodecs.STRING_UTF8.decode(buf);
            ServerConfig config = GSON.fromJson(json, ServerConfig.class);
            if (config == null) {
                config = new ServerConfig();
            }
            config.validate();
            return config;
        }
    );
}
