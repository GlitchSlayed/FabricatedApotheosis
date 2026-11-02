package dev.shadowsoffire.apotheosis.adventure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.color.GradientColor;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.RandomSource;

public class InGameRepairsTest {

    @Test
    void testGradientColorSerializationResilience() {
        int[] gradient = { 0x55FFFF, 0x00AAAA };
        GradientColor color = new GradientColor(gradient, "test_flash");

        String serialized = color.serialize();
        assertNotNull(serialized);
        assertTrue(serialized.startsWith("#"), "Serialized gradient color must start with # for network safety");

        // Verify vanilla TextColor.parseColor can parse it without failing
        var parseResult = TextColor.parseColor(serialized);
        assertTrue(parseResult.isSuccess(), "TextColor.parseColor must succeed on serialized string: " + serialized);
    }

    @Test
    void testEmptyRandomElementReturnsNull() {
        RandomSource rand = RandomSource.create(42L);
        assertNull(ApothMiscUtil.getRandomElement(Collections.emptyList(), rand), "Empty collection must return null without throwing exception");
        assertNull(ApothMiscUtil.getRandomElement(null, rand), "Null collection must return null without throwing exception");

        List<String> single = List.of("hello");
        assertEquals("hello", ApothMiscUtil.getRandomElement(single, rand));
    }

    @Test
    void testHavenUnlockedByDefault() {
        // Haven is the base tier and must always be unlocked
        assertTrue(WorldTier.isUnlocked(null, WorldTier.HAVEN), "Haven tier must be unlocked even with null/fresh player");
    }
}
