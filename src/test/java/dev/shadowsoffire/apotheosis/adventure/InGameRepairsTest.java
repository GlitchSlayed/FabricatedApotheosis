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

    @Test
    void testEquipmentSlotCompatGroupMatching() {
        // Verify that EquipmentSlotCompat maps groups 1:1 and does not map MAINHAND to ANY or HAND
        var mainHandGroup = dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat.toVanilla(dev.shadowsoffire.apothic_attributes.api.ALObjects.EquipmentSlotGroups.MAINHAND);
        assertEquals(net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND, mainHandGroup);
        assertNotEquals(net.minecraft.world.entity.EquipmentSlotGroup.ANY, mainHandGroup);
        assertNotEquals(net.minecraft.world.entity.EquipmentSlotGroup.HAND, mainHandGroup);

        var anyGroup = dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat.toVanilla(dev.shadowsoffire.apothic_attributes.api.ALObjects.EquipmentSlotGroups.ANY);
        assertEquals(net.minecraft.world.entity.EquipmentSlotGroup.ANY, anyGroup);
    }

    @Test
    void testSocketTooltipComponentCommonSafety() {
        dev.shadowsoffire.apotheosis.socket.SocketTooltipComponent comp = new dev.shadowsoffire.apotheosis.socket.SocketTooltipComponent(
            net.minecraft.world.item.ItemStack.EMPTY,
            dev.shadowsoffire.apotheosis.socket.SocketedGems.EMPTY
        );
        assertNotNull(comp);
        assertTrue(comp.socketed().isEmpty());
        assertTrue(comp.gems().isEmpty());
    }

    @Test
    void testEnchantingTableHierarchyForJade() {
        // Both Apothic and Raven enchanting table blocks extend EnchantingTableBlock
        assertTrue(net.minecraft.world.level.block.EnchantingTableBlock.class.isAssignableFrom(
            dev.shadowsoffire.apothic_enchanting.table.ApothicEnchantingTableBlock.class
        ));
        assertTrue(net.minecraft.world.level.block.EnchantingTableBlock.class.isAssignableFrom(
            dev.shadowsoffire.apothic_enchanting.table.RavenEnchantingTableBlock.class
        ));
    }

    @Test
    void testIntegratedConfigOwnership() {
        try {
            net.neoforged.neoforge.server.ServerLifecycleHooks.setIntegratedServerOverrideForTesting(true);
            dev.shadowsoffire.apotheosis.config.ServerConfig local = dev.shadowsoffire.apotheosis.config.ApotheosisConfig.getLocalServer();
            local.maxEnchantmentLevelLimit = 120;
            
            // Calling setRemoteServerConfig should NOT overwrite localServerConfig in singleplayer
            dev.shadowsoffire.apotheosis.config.ServerConfig dummyRemote = new dev.shadowsoffire.apotheosis.config.ServerConfig();
            dummyRemote.maxEnchantmentLevelLimit = 50;
            dev.shadowsoffire.apotheosis.config.ApotheosisConfig.setRemoteServerConfig(dummyRemote);

            // When not remote, getServer() must return localServerConfig
            assertFalse(dev.shadowsoffire.apotheosis.config.ApotheosisConfig.isRemoteServer());
            assertEquals(120, dev.shadowsoffire.apotheosis.config.ApotheosisConfig.getServer().maxEnchantmentLevelLimit);
        } finally {
            net.neoforged.neoforge.server.ServerLifecycleHooks.setIntegratedServerOverrideForTesting(null);
            dev.shadowsoffire.apotheosis.config.ApotheosisConfig.clearRemoteServerConfig();
        }
    }
}
