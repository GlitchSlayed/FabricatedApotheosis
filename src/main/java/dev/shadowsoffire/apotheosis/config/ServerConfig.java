package dev.shadowsoffire.apotheosis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Server-authoritative gameplay configuration schema.
 * Contains gameplay rules, math formulas, limits, and module toggles.
 */
public class ServerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/ServerConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final int CURRENT_SCHEMA_VERSION = 1;

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

    // --- Enchanting Gameplay (LIVE) ---
    public int maxEnchantmentLevelLimit = 100;
    public int maxBookshelfRange = 2;

    // --- Attributes Gameplay (LIVE) ---
    public float knowledgeMultiplier = 4.0F;
    public float negativeArmorFactor = 0.015F;
    public float critDamageModifierCap = 10.0F;

    // --- Adventure Gameplay (LIVE) ---
    public int bossSpawnCooldown = 3600;
    public boolean bossGlowOnSpawn = true;
    public boolean curseBossItems = false;
    public boolean undergroundTrader = true;

    /**
     * Validates and normalizes all configuration values, clamping them to safe limits.
     * Logs actionable diagnostics if values are out of bounds.
     */
    public void validate() {
        if (this.spawnerSilkLevel < -1 || this.spawnerSilkLevel > 127) {
            int clamped = Math.clamp(this.spawnerSilkLevel, -1, 127);
            LOGGER.warn("spawnerSilkLevel '{}' out of bounds [-1, 127]. Clamping to {}.", this.spawnerSilkLevel, clamped);
            this.spawnerSilkLevel = clamped;
        }

        if (this.spawnerSilkDamage < 0 || this.spawnerSilkDamage > 100000) {
            int clamped = Math.clamp(this.spawnerSilkDamage, 0, 100000);
            LOGGER.warn("spawnerSilkDamage '{}' out of bounds [0, 100000]. Clamping to {}.", this.spawnerSilkDamage, clamped);
            this.spawnerSilkDamage = clamped;
        }

        if (this.entityDespawnDelay < 0 || this.entityDespawnDelay > 24000) {
            int clamped = Math.clamp(this.entityDespawnDelay, 0, 24000);
            LOGGER.warn("entityDespawnDelay '{}' out of bounds [0, 24000]. Clamping to {}.", this.entityDespawnDelay, clamped);
            this.entityDespawnDelay = clamped;
        }

        if (this.maxSpawnDelayLimit < 20 || this.maxSpawnDelayLimit > 100000) {
            int clamped = Math.clamp(this.maxSpawnDelayLimit, 20, 100000);
            LOGGER.warn("maxSpawnDelayLimit '{}' out of bounds [20, 100000]. Clamping to {}.", this.maxSpawnDelayLimit, clamped);
            this.maxSpawnDelayLimit = clamped;
        }

        if (this.minSpawnDelayLimit < 0 || this.minSpawnDelayLimit > this.maxSpawnDelayLimit) {
            int clamped = Math.clamp(this.minSpawnDelayLimit, 0, this.maxSpawnDelayLimit);
            LOGGER.warn("minSpawnDelayLimit '{}' exceeds maxSpawnDelayLimit '{}'. Clamping to {}.", this.minSpawnDelayLimit, this.maxSpawnDelayLimit, clamped);
            this.minSpawnDelayLimit = clamped;
        }

        if (this.maxEnchantmentLevelLimit < 1 || this.maxEnchantmentLevelLimit > 255) {
            int clamped = Math.clamp(this.maxEnchantmentLevelLimit, 1, 255);
            LOGGER.warn("maxEnchantmentLevelLimit '{}' out of bounds [1, 255]. Clamping to {}.", this.maxEnchantmentLevelLimit, clamped);
            this.maxEnchantmentLevelLimit = clamped;
        }

        if (this.maxBookshelfRange < 1 || this.maxBookshelfRange > 5) {
            int clamped = Math.clamp(this.maxBookshelfRange, 1, 5);
            LOGGER.warn("maxBookshelfRange '{}' out of bounds [1, 5]. Clamping to {}.", this.maxBookshelfRange, clamped);
            this.maxBookshelfRange = clamped;
        }

        if (this.knowledgeMultiplier < 1.0F || this.knowledgeMultiplier > 1024.0F) {
            float clamped = Math.clamp(this.knowledgeMultiplier, 1.0F, 1024.0F);
            LOGGER.warn("knowledgeMultiplier '{}' out of bounds [1.0, 1024.0]. Clamping to {}.", this.knowledgeMultiplier, clamped);
            this.knowledgeMultiplier = clamped;
        }

        if (this.negativeArmorFactor < 0.0F || this.negativeArmorFactor > 1.0F) {
            float clamped = Math.clamp(this.negativeArmorFactor, 0.0F, 1.0F);
            LOGGER.warn("negativeArmorFactor '{}' out of bounds [0.0, 1.0]. Clamping to {}.", this.negativeArmorFactor, clamped);
            this.negativeArmorFactor = clamped;
        }

        if (this.critDamageModifierCap < 1.0F || this.critDamageModifierCap > 100.0F) {
            float clamped = Math.clamp(this.critDamageModifierCap, 1.0F, 100.0F);
            LOGGER.warn("critDamageModifierCap '{}' out of bounds [1.0, 100.0]. Clamping to {}.", this.critDamageModifierCap, clamped);
            this.critDamageModifierCap = clamped;
        }

        if (this.bossSpawnCooldown < 0 || this.bossSpawnCooldown > 72000) {
            int clamped = Math.clamp(this.bossSpawnCooldown, 0, 72000);
            LOGGER.warn("bossSpawnCooldown '{}' out of bounds [0, 72000]. Clamping to {}.", this.bossSpawnCooldown, clamped);
            this.bossSpawnCooldown = clamped;
        }
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
