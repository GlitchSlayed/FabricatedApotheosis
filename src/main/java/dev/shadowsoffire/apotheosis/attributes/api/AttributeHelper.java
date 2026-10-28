package dev.shadowsoffire.apotheosis.attributes.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Common utilities for applying, inspecting, and migrating attribute modifiers.
 */
public class AttributeHelper {

    public static final Identifier OBSOLETE_MINING_SPEED = Identifier.fromNamespaceAndPath("apothic_attributes", "mining_speed");

    /**
     * Applies a permanent modifier to the given attribute on an entity.
     * Replaces any existing modifier with the same identifier, preventing duplication.
     */
    public static void modify(LivingEntity entity, Holder<Attribute> attribute, Identifier id, double value, Operation operation) {
        AttributeInstance inst = entity.getAttribute(attribute);
        if (inst != null) {
            inst.removeModifier(id);
            inst.addPermanentModifier(new AttributeModifier(id, value, operation));
        }
    }

    public static void addToBase(LivingEntity entity, Holder<Attribute> attribute, Identifier id, double modifier) {
        modify(entity, attribute, id, modifier, Operation.ADD_VALUE);
    }

    public static void addXTimesNewBase(LivingEntity entity, Holder<Attribute> attribute, Identifier id, double modifier) {
        modify(entity, attribute, id, modifier, Operation.ADD_MULTIPLIED_BASE);
    }

    public static void multiplyFinal(LivingEntity entity, Holder<Attribute> attribute, Identifier id, double modifier) {
        modify(entity, attribute, id, modifier, Operation.ADD_MULTIPLIED_TOTAL);
    }

    /**
     * Inspects the item's attribute modifiers and migrates any obsolete attributes
     * (such as apothic_attributes:mining_speed) to their modern vanilla equivalent
     * (minecraft:mining_efficiency), preventing duplicate application.
     *
     * @param stack The item stack to inspect and update.
     * @return True if any modifier was migrated.
     */
    public static boolean migrateItemModifiers(ItemStack stack) {
        ItemAttributeModifiers modifiersComponent = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiersComponent == null || modifiersComponent.modifiers().isEmpty()) {
            return false;
        }

        boolean modified = false;
        List<ItemAttributeModifiers.Entry> newModifiers = new ArrayList<>();

        for (ItemAttributeModifiers.Entry entry : modifiersComponent.modifiers()) {
            Holder<Attribute> attr = entry.attribute();
            Identifier attrId = attr.unwrapKey().map(k -> k.identifier()).orElse(null);
            if (attrId == null) {
                try {
                    attrId = Identifier.tryParse(attr.value().getDescriptionId());
                } catch (Exception ignored) {}
            }

            if (OBSOLETE_MINING_SPEED.equals(attrId)) {
                // Migrate to vanilla MINING_EFFICIENCY
                newModifiers.add(new ItemAttributeModifiers.Entry(
                    Attributes.MINING_EFFICIENCY,
                    entry.modifier(),
                    entry.slot()
                ));
                modified = true;
            } else {
                newModifiers.add(entry);
            }
        }

        if (modified) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(newModifiers));
        }

        return modified;
    }
}
