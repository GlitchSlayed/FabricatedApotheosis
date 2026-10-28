package dev.shadowsoffire.apotheosis.attributes;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributeHelper;
import dev.shadowsoffire.apotheosis.attributes.format.AttributeFormatting;
import dev.shadowsoffire.apotheosis.config.ClientConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class AttributesTest {

    @BeforeAll
    public static void setup() throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();

        // Unfreeze registries for test environment
        java.lang.reflect.Field frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
        frozenField.setAccessible(true);
        frozenField.set(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE, false);
        frozenField.set(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT, false);
        frozenField.set(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT, false);
        frozenField.set(net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE, false);

        ALObjects.bootstrap();

        // Bind default item components for diamond pickaxe if in unit test environment
        try {
            Items.DIAMOND_PICKAXE.builtInRegistryHolder().components();
        } catch (NullPointerException e) {
            Items.DIAMOND_PICKAXE.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.EMPTY);
        }
    }

    @Test
    public void testAll21AttributesPresentAndSyncable() {
        assertEquals(21, ALObjects.Attributes.ALL.size(), "Must have exactly 21 registered custom attributes");

        for (Holder<Attribute> holder : ALObjects.Attributes.ALL) {
            Attribute attr = holder.value();
            assertTrue(attr.isClientSyncable(), "Attribute " + attr.getDescriptionId() + " must have client synchronization enabled");
            assertTrue(attr.getDescriptionId().startsWith("apothic_attributes:"), "Attribute must use apothic_attributes namespace");
            assertTrue(attr instanceof RangedAttribute, "All 21 attributes should be RangedAttribute instances");
        }
    }

    @Test
    public void testAttributeBoundsAndDefaults() {
        RangedAttribute armorPierce = (RangedAttribute) ALObjects.Attributes.ARMOR_PIERCE.value();
        assertEquals(0.0D, armorPierce.getDefaultValue());
        assertEquals(0.0D, armorPierce.getMinValue());
        assertEquals(1000.0D, armorPierce.getMaxValue());

        RangedAttribute armorShred = (RangedAttribute) ALObjects.Attributes.ARMOR_SHRED.value();
        assertEquals(0.0D, armorShred.getDefaultValue());
        assertEquals(0.0D, armorShred.getMinValue());
        assertEquals(2.0D, armorShred.getMaxValue());

        RangedAttribute critChance = (RangedAttribute) ALObjects.Attributes.CRIT_CHANCE.value();
        assertEquals(0.05D, critChance.getDefaultValue(), 1e-6);
        assertEquals(0.0D, critChance.getMinValue());
        assertEquals(10.0D, critChance.getMaxValue());

        RangedAttribute critDamage = (RangedAttribute) ALObjects.Attributes.CRIT_DAMAGE.value();
        assertEquals(1.5D, critDamage.getDefaultValue(), 1e-6);
        assertEquals(1.0D, critDamage.getMinValue());
        assertEquals(100.0D, critDamage.getMaxValue());

        RangedAttribute cooldownReduction = (RangedAttribute) ALObjects.Attributes.COOLDOWN_REDUCTION.value();
        assertEquals(0.0D, cooldownReduction.getDefaultValue());
        assertEquals(-10.0D, cooldownReduction.getMinValue());
        assertEquals(0.95D, cooldownReduction.getMaxValue(), 1e-6);
    }

    @Test
    public void testPercentageAndBooleanFormatting() {
        // Percentage attribute formatting
        assertEquals(AttributeFormatting.FormatType.PERCENTAGE, AttributeFormatting.getFormatType(ALObjects.Attributes.ARMOR_SHRED));
        assertEquals("25%", AttributeFormatting.formatValue(ALObjects.Attributes.ARMOR_SHRED, 0.25D));

        AttributeModifier shredMod = new AttributeModifier(
            Identifier.fromNamespaceAndPath("apothic_attributes", "test_shred"),
            0.15D,
            AttributeModifier.Operation.ADD_VALUE
        );
        assertEquals("+15%", AttributeFormatting.formatBonus(ALObjects.Attributes.ARMOR_SHRED, shredMod));

        // Boolean attribute formatting
        assertEquals(AttributeFormatting.FormatType.BOOLEAN, AttributeFormatting.getFormatType(ALObjects.Attributes.ELYTRA_FLIGHT));
        assertEquals("Enabled", AttributeFormatting.formatValue(ALObjects.Attributes.ELYTRA_FLIGHT, 1.0D));
        assertEquals("Disabled", AttributeFormatting.formatValue(ALObjects.Attributes.ELYTRA_FLIGHT, 0.0D));

        // Flat attribute formatting
        assertEquals(AttributeFormatting.FormatType.FLAT, AttributeFormatting.getFormatType(ALObjects.Attributes.ARMOR_PIERCE));
        assertEquals("10.5", AttributeFormatting.formatValue(ALObjects.Attributes.ARMOR_PIERCE, 10.5D));
    }

    @Test
    public void testModifierAccumulationPrevention() {
        RangedAttribute attr = (RangedAttribute) ALObjects.Attributes.ARMOR_PIERCE.value();
        AttributeInstance instance = new AttributeInstance(ALObjects.Attributes.ARMOR_PIERCE, i -> {});

        Identifier modId = Identifier.fromNamespaceAndPath("apothic_attributes", "helmet_bonus");

        AttributeModifier mod1 = new AttributeModifier(modId, 5.0D, AttributeModifier.Operation.ADD_VALUE);
        instance.addTransientModifier(mod1);
        assertEquals(5.0D, instance.getValue(), 1e-6);
        assertEquals(1, instance.getModifiers().size());

        // Re-applying or updating modifier with same ID replaces it, preventing accumulation
        AttributeModifier mod2 = new AttributeModifier(modId, 10.0D, AttributeModifier.Operation.ADD_VALUE);
        instance.addOrUpdateTransientModifier(mod2);
        assertEquals(10.0D, instance.getValue(), 1e-6);
        assertEquals(1, instance.getModifiers().size(), "Modifier count must remain 1 after re-applying same modifier ID");

        // Removing modifier removes it cleanly
        instance.removeModifier(modId);
        assertEquals(0.0D, instance.getValue(), 1e-6);
        assertEquals(0, instance.getModifiers().size());
    }

    @Test
    public void testObsoleteMiningSpeedMigration() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        Identifier obsoleteId = Identifier.fromNamespaceAndPath("apothic_attributes", "mining_speed");

        AttributeModifier obsoleteMod = new AttributeModifier(obsoleteId, 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        RangedAttribute dummyObsoleteAttr = new RangedAttribute("apothic_attributes:mining_speed", 0.0D, 0.0D, 1000.0D);
        Holder<Attribute> dummyHolder = Holder.direct(dummyObsoleteAttr);

        ItemAttributeModifiers initialModifiers = ItemAttributeModifiers.builder()
            .add(dummyHolder, obsoleteMod, EquipmentSlotGroup.MAINHAND)
            .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, initialModifiers);

        // Run migration
        AttributeHelper.migrateItemModifiers(stack);

        ItemAttributeModifiers migrated = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        assertNotNull(migrated);

        // Verify apothic_attributes:mining_speed was replaced with minecraft:mining_efficiency
        boolean foundObsolete = false;
        boolean foundVanillaEfficiency = false;

        for (ItemAttributeModifiers.Entry entry : migrated.modifiers()) {
            Identifier attrId = entry.attribute().unwrapKey().map(k -> k.identifier()).orElse(null);
            if (obsoleteId.equals(attrId)) {
                foundObsolete = true;
            }
            if (Identifier.parse("minecraft:mining_efficiency").equals(attrId)) {
                foundVanillaEfficiency = true;
            }
        }

        assertFalse(foundObsolete, "Obsolete apothic_attributes:mining_speed must not remain");
        assertTrue(foundVanillaEfficiency, "Must be migrated to vanilla minecraft:mining_efficiency");
    }

    @Test
    public void testHiddenAttributeFiltering() {
        ClientConfig config = new ClientConfig();

        assertTrue(config.isAttributeHidden(Identifier.parse("apothic_attributes:ghost_health")),
            "Ghost health should be hidden by default");
        assertTrue(config.isAttributeHidden(Identifier.parse("minecraft:mining_efficiency")),
            "Mining efficiency should be hidden by default");
        assertFalse(config.isAttributeHidden(Identifier.parse("apothic_attributes:crit_chance")),
            "Crit chance should not be hidden");

        // Test negation
        config.hiddenAttributes.add("!apothic_attributes:ghost_health");
        assertFalse(config.isAttributeHidden(Identifier.parse("apothic_attributes:ghost_health")),
            "Negated entry should unblock ghost health");
    }
}
