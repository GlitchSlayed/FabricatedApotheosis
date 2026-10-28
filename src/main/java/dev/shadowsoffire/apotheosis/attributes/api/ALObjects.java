package dev.shadowsoffire.apotheosis.attributes.api;

import java.util.List;

import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * Registry holders and constants for all Apothic Attributes.
 */
public class ALObjects {

    public static final String MODID = "apothic_attributes";
    private static final DeferredHelper R = DeferredHelper.create(MODID);

    public static class Attributes {

        public static final Holder<Attribute> ARMOR_PIERCE = R.attribute("armor_pierce",
            () -> new RangedAttribute("apothic_attributes:armor_pierce", 0.0D, 0.0D, 1000.0D).setSyncable(true));

        public static final Holder<Attribute> ARMOR_SHRED = R.attribute("armor_shred",
            () -> new RangedAttribute("apothic_attributes:armor_shred", 0.0D, 0.0D, 2.0D).setSyncable(true));

        public static final Holder<Attribute> ARROW_DAMAGE = R.attribute("arrow_damage",
            () -> new RangedAttribute("apothic_attributes:arrow_damage", 1.0D, 0.0D, 10.0D).setSyncable(true));

        public static final Holder<Attribute> ARROW_VELOCITY = R.attribute("arrow_velocity",
            () -> new RangedAttribute("apothic_attributes:arrow_velocity", 1.0D, 0.0D, 10.0D).setSyncable(true));

        public static final Holder<Attribute> COLD_DAMAGE = R.attribute("cold_damage",
            () -> new RangedAttribute("apothic_attributes:cold_damage", 0.0D, 0.0D, 1000.0D).setSyncable(true));

        public static final Holder<Attribute> CRIT_CHANCE = R.attribute("crit_chance",
            () -> new RangedAttribute("apothic_attributes:crit_chance", 0.05D, 0.0D, 10.0D).setSyncable(true));

        public static final Holder<Attribute> CRIT_DAMAGE = R.attribute("crit_damage",
            () -> new RangedAttribute("apothic_attributes:crit_damage", 1.5D, 1.0D, 100.0D).setSyncable(true));

        public static final Holder<Attribute> CURRENT_HP_DAMAGE = R.attribute("current_hp_damage",
            () -> new RangedAttribute("apothic_attributes:current_hp_damage", 0.0D, 0.0D, 1.0D).setSyncable(true));

        public static final Holder<Attribute> DODGE_CHANCE = R.attribute("dodge_chance",
            () -> new RangedAttribute("apothic_attributes:dodge_chance", 0.0D, 0.0D, 1.0D).setSyncable(true));

        public static final Holder<Attribute> DRAW_SPEED = R.attribute("draw_speed",
            () -> new RangedAttribute("apothic_attributes:draw_speed", 1.0D, 0.0D, 4.0D).setSyncable(true));

        public static final Holder<Attribute> EXPERIENCE_GAINED = R.attribute("experience_gained",
            () -> new RangedAttribute("apothic_attributes:experience_gained", 1.0D, 0.0D, 1000.0D).setSyncable(true));

        public static final Holder<Attribute> FIRE_DAMAGE = R.attribute("fire_damage",
            () -> new RangedAttribute("apothic_attributes:fire_damage", 0.0D, 0.0D, 1000.0D).setSyncable(true));

        public static final Holder<Attribute> GHOST_HEALTH = R.attribute("ghost_health",
            () -> new RangedAttribute("apothic_attributes:ghost_health", 0.0D, 0.0D, 1000.0D).setSyncable(true));

        public static final Holder<Attribute> HEALING_RECEIVED = R.attribute("healing_received",
            () -> new RangedAttribute("apothic_attributes:healing_received", 1.0D, 0.0D, 1000.0D).setSyncable(true));

        public static final Holder<Attribute> LIFE_STEAL = R.attribute("life_steal",
            () -> new RangedAttribute("apothic_attributes:life_steal", 0.0D, 0.0D, 10.0D).setSyncable(true));

        public static final Holder<Attribute> OVERHEAL = R.attribute("overheal",
            () -> new RangedAttribute("apothic_attributes:overheal", 0.0D, 0.0D, 10.0D).setSyncable(true));

        public static final Holder<Attribute> PROJECTILE_DAMAGE = R.attribute("projectile_damage",
            () -> new RangedAttribute("apothic_attributes:projectile_damage", 1.0D, 0.0D, 10.0D).setSyncable(true));

        public static final Holder<Attribute> PROT_PIERCE = R.attribute("prot_pierce",
            () -> new RangedAttribute("apothic_attributes:prot_pierce", 0.0D, 0.0D, 34.0D).setSyncable(true));

        public static final Holder<Attribute> PROT_SHRED = R.attribute("prot_shred",
            () -> new RangedAttribute("apothic_attributes:prot_shred", 0.0D, 0.0D, 0.90D).setSyncable(true));

        public static final Holder<Attribute> ELYTRA_FLIGHT = R.attribute("elytra_flight",
            () -> new RangedAttribute("apothic_attributes:elytra_flight", 0.0D, 0.0D, 1.0D).setSyncable(true));

        public static final Holder<Attribute> COOLDOWN_REDUCTION = R.attribute("cooldown_reduction",
            () -> new RangedAttribute("apothic_attributes:cooldown_reduction", 0.0D, -10.0D, 0.95D).setSyncable(true));

        public static final List<Holder<Attribute>> ALL = List.of(
            ARMOR_PIERCE, ARMOR_SHRED, ARROW_DAMAGE, ARROW_VELOCITY, COLD_DAMAGE,
            CRIT_CHANCE, CRIT_DAMAGE, CURRENT_HP_DAMAGE, DODGE_CHANCE, DRAW_SPEED,
            EXPERIENCE_GAINED, FIRE_DAMAGE, GHOST_HEALTH, HEALING_RECEIVED, LIFE_STEAL,
            OVERHEAL, PROJECTILE_DAMAGE, PROT_PIERCE, PROT_SHRED, ELYTRA_FLIGHT,
            COOLDOWN_REDUCTION
        );

        public static void bootstrap() {}
    }

    public static class Tags {
        public static final TagKey<Attribute> DYNAMIC_BASE_ATTRIBUTES = TagKey.create(Registries.ATTRIBUTE, Identifier.fromNamespaceAndPath(MODID, "dynamic_base"));
    }
}
