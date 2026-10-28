package dev.shadowsoffire.apotheosis.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
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
    public List<String> hiddenAttributes = new ArrayList<>(List.of(
        "apothic_attributes:elytra_flight",
        "apothic_attributes:ghost_health",
        "minecraft:camera_distance",
        "minecraft:explosion_knockback_resistance",
        "minecraft:mining_efficiency",
        "minecraft:movement_efficiency",
        "minecraft:waypoint_receive_range",
        "minecraft:waypoint_transmit_range"
    ));

    /**
     * Checks whether an attribute should be hidden from the inspection GUI.
     */
    public boolean isAttributeHidden(Identifier id) {
        if (id == null) return false;
        String fullId = id.toString();
        String namespaceWildcard = id.getNamespace() + ":*";
        boolean hidden = false;
        for (String entry : this.hiddenAttributes) {
            if (entry == null || entry.isBlank()) continue;
            if (entry.startsWith("!")) {
                String unblocked = entry.substring(1);
                if (unblocked.equals(fullId) || unblocked.equals(namespaceWildcard)) {
                    hidden = false;
                }
            } else if (entry.equals(fullId) || entry.equals(namespaceWildcard)) {
                hidden = true;
            }
        }
        return hidden;
    }

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
