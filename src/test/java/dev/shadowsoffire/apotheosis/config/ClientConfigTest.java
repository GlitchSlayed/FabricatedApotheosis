package dev.shadowsoffire.apotheosis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ClientConfigTest {

    @Test
    public void testClientConfigDefaultsAndValidation() {
        ClientConfig config = new ClientConfig();
        assertEquals(ClientConfig.CURRENT_SCHEMA_VERSION, config.schemaVersion);
        assertTrue(config.bossHealthBarGlow);
        assertTrue(config.displayAttributeDeltas);
        assertTrue(config.enableAttributesGui);
        assertEquals(-3, config.attributesGuiOffsetX);
        assertEquals(3, config.attributesGuiOffsetY);

        // Test clamping of GUI offsets
        config.attributesGuiOffsetX = 5000;
        config.attributesGuiOffsetY = -3000;
        config.sculkShelfNoiseChance = -10;
        config.validate();

        assertEquals(1000, config.attributesGuiOffsetX);
        assertEquals(-1000, config.attributesGuiOffsetY);
        assertEquals(0, config.sculkShelfNoiseChance);

        // Verify v2 fields and anchor point normalization
        assertEquals(2, config.schemaVersion);
        assertEquals("TOP_RIGHT", config.attributesGuiAnchorPoint);
        assertTrue(config.enableEquipmentCompare);
        assertTrue(config.disableQuarkOnAffixItems);

        config.attributesGuiAnchorPoint = "bottom_left";
        config.validate();
        assertEquals("BOTTOM_LEFT", config.attributesGuiAnchorPoint);
        assertEquals(dev.shadowsoffire.placebo.util.Offset.AnchorPoint.BOTTOM_LEFT, config.getAnchorPoint());

        config.attributesGuiAnchorPoint = "INVALID_ANCHOR";
        config.hiddenAttributes = null;
        config.validate();
        assertEquals("TOP_RIGHT", config.attributesGuiAnchorPoint);
        assertTrue(config.hiddenAttributes.isEmpty());
    }
}
