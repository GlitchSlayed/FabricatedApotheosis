package dev.shadowsoffire.apotheosis.client.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.resources.Identifier;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Registry and base class for modifier provenance source types.
 * Provides an open extension point for optional accessory slot integrations (Trinkets / Accessories).
 */
public abstract class ModifierSourceType<T> {

    private static final List<ModifierSourceType<?>> SOURCE_TYPES = new ArrayList<>();

    public static final ModifierSourceType<ItemStack> EQUIPMENT = register(new ModifierSourceType<ItemStack>() {
        @Override
        public void extract(LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = entity.getItemBySlot(slot);
                if (!item.isEmpty()) {
                    item.forEachModifier(slot, (attr, modif) -> {
                        map.accept(modif, new ModifierSource.ItemModifierSource(item, slot));
                    });
                }
            }
        }

        @Override
        public int getPriority() {
            return 0;
        }
    });

    public static final ModifierSourceType<MobEffectInstance> MOB_EFFECT = register(new ModifierSourceType<MobEffectInstance>() {
        @Override
        public void extract(LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map) {
            for (MobEffectInstance effectInst : entity.getActiveEffects()) {
                effectInst.getEffect().value().createModifiers(effectInst.getAmplifier(), (attr, modif) -> {
                    map.accept(modif, new ModifierSource.EffectModifierSource(effectInst));
                });
            }
        }

        @Override
        public int getPriority() {
            return 100;
        }
    });

    public static synchronized <T extends ModifierSourceType<?>> T register(T type) {
        SOURCE_TYPES.add(type);
        return type;
    }

    public static Comparator<AttributeModifier> compareBySource(Map<Identifier, ModifierSource<?>> sources) {
        return (a1, a2) -> {
            var src1 = sources.get(a1.id());
            var src2 = sources.get(a2.id());
            if (src1 != null && src2 != null) {
                int p = Integer.compare(src1.getType().getPriority(), src2.getType().getPriority());
                if (p != 0) return p;
                int nameComp = src1.getDisplayName().getString().compareTo(src2.getDisplayName().getString());
                if (nameComp != 0) return nameComp;
                int amtComp = Double.compare(Math.abs(a2.amount()), Math.abs(a1.amount()));
                if (amtComp != 0) return amtComp;
                return a1.id().compareTo(a2.id());
            }
            return src1 != null ? -1 : src2 != null ? 1 : 0;
        };
    }

    public static List<ModifierSourceType<?>> getTypes() {
        return Collections.unmodifiableList(SOURCE_TYPES);
    }

    public abstract void extract(LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map);

    public abstract int getPriority();
}
