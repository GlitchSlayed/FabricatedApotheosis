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
    }
}
