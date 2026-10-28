package dev.shadowsoffire.apotheosis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-only preferences configuration schema.
 * Contains purely visual, tooltip formatting, and screen positioning options.
 * Client preferences never affect server authority or multiplayer game rules.
 */
public class ClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/ClientConfig");

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;

    // --- Visual Preferences (LIVE) ---
    public boolean bossHealthBarGlow = true;
    public boolean sculkShelfParticles = true;
    public int sculkShelfNoiseChance = 200;

    // --- Tooltip Details (LIVE) ---
    public boolean displayAttributeDeltas = true;
    public boolean enablePotionTooltips = true;
    public boolean showEnchantedBookMetadata = true;
    public boolean detailedSpawnerTooltips = true;
    public boolean enableInlineEnchDescs = false;

    // --- Screen Positioning & GUI (LIVE) ---
    public boolean enableAttributesGui = true;
    public int attributesGuiOffsetX = -3;
    public int attributesGuiOffsetY = 3;

    /**
     * Validates and normalizes client preferences.
     */
    public void validate() {
        if (this.sculkShelfNoiseChance < 0 || this.sculkShelfNoiseChance > 32767) {
            int clamped = Math.clamp(this.sculkShelfNoiseChance, 0, 32767);
            LOGGER.warn("sculkShelfNoiseChance '{}' out of bounds [0, 32767]. Clamping to {}.", this.sculkShelfNoiseChance, clamped);
            this.sculkShelfNoiseChance = clamped;
        }

        if (this.attributesGuiOffsetX < -1000 || this.attributesGuiOffsetX > 1000) {
            int clamped = Math.clamp(this.attributesGuiOffsetX, -1000, 1000);
            LOGGER.warn("attributesGuiOffsetX '{}' out of bounds [-1000, 1000]. Clamping to {}.", this.attributesGuiOffsetX, clamped);
            this.attributesGuiOffsetX = clamped;
        }

        if (this.attributesGuiOffsetY < -1000 || this.attributesGuiOffsetY > 1000) {
            int clamped = Math.clamp(this.attributesGuiOffsetY, -1000, 1000);
            LOGGER.warn("attributesGuiOffsetY '{}' out of bounds [-1000, 1000]. Clamping to {}.", this.attributesGuiOffsetY, clamped);
            this.attributesGuiOffsetY = clamped;
        }
    }
}
