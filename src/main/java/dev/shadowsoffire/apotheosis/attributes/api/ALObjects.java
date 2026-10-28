package dev.shadowsoffire.apotheosis.attributes.api;

import java.util.List;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.attributes.effects.BleedingEffect;
import dev.shadowsoffire.apotheosis.attributes.effects.DetonationEffect;
import dev.shadowsoffire.apotheosis.attributes.effects.FlyingEffect;
import dev.shadowsoffire.apotheosis.attributes.effects.GrievousEffect;
import dev.shadowsoffire.apotheosis.attributes.effects.KnowledgeEffect;
import dev.shadowsoffire.apotheosis.attributes.effects.SunderingEffect;
import dev.shadowsoffire.apotheosis.attributes.effects.VitalityEffect;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
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

    public static class MobEffects {
        public static Holder<MobEffect> BLEEDING;
        public static Holder<MobEffect> DETONATION;
        public static Holder<MobEffect> GRIEVOUS;
        public static Holder<MobEffect> KNOWLEDGE;
        public static Holder<MobEffect> SUNDERING;
        public static Holder<MobEffect> VITALITY;
        public static Holder<MobEffect> FLYING;

        public static void bootstrap() {
            if (BLEEDING == null) {
                BLEEDING = R.effect("bleeding", BleedingEffect::new);
                DETONATION = R.effect("detonation", DetonationEffect::new);
                GRIEVOUS = R.effect("grievous", GrievousEffect::new);
                KNOWLEDGE = R.effect("knowledge", KnowledgeEffect::new);
                SUNDERING = R.effect("sundering", SunderingEffect::new);
                VITALITY = R.effect("vitality", VitalityEffect::new);
                FLYING = R.effect("flying", FlyingEffect::new);
            }
        }
    }

    public static class Particles {
        public static Holder<SimpleParticleType> APOTH_CRIT;

        public static void bootstrap() {
            if (APOTH_CRIT == null) {
                APOTH_CRIT = R.particle("apoth_crit");
            }
        }
    }

    public static class Sounds {
        public static Holder<SoundEvent> DODGE;

        public static void bootstrap() {
            if (DODGE == null) {
                DODGE = R.sound("dodge");
            }
        }
    }

    public static class DamageTypes {
        public static final ResourceKey<DamageType> BLEEDING = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "bleeding"));
        public static final ResourceKey<DamageType> DETONATION = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "detonation"));
        public static final ResourceKey<DamageType> CURRENT_HP_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "current_hp_damage"));
        public static final ResourceKey<DamageType> FIRE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "fire_damage"));
        public static final ResourceKey<DamageType> COLD_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "cold_damage"));

        public static void bootstrap() {}
    }

    public static class Attachments {
        public static AttachmentType<Float> PRE_DAMAGE_HEALTH;
        public static AttachmentType<AuxDmgTracker> AUX_DMG_TRACKER;
        public static AttachmentType<CooldownTracker> COOLDOWNS;
        public static AttachmentType<Float> OVERHEAL_ABSORPTION;
        public static AttachmentType<CompoundTag> PERSISTENT_DATA;

        public static void bootstrap() {
            if (PRE_DAMAGE_HEALTH == null) {
                try {
                    PRE_DAMAGE_HEALTH = AttachmentRegistry.createDefaulted(
                        Identifier.fromNamespaceAndPath(MODID, "pre_damage_health"), () -> 0.0F);
                    AUX_DMG_TRACKER = AttachmentRegistry.<AuxDmgTracker>builder()
                        .initializer(AuxDmgTracker::new)
                        .persistent(AuxDmgTracker.CODEC)
                        .buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "aux_dmg_tracker"));
                    COOLDOWNS = AttachmentRegistry.<CooldownTracker>builder()
                        .initializer(CooldownTracker::new)
                        .persistent(CooldownTracker.CODEC.codec())
                        .copyOnDeath()
                        .buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "cooldowns"));
                    OVERHEAL_ABSORPTION = AttachmentRegistry.<Float>builder()
                        .initializer(() -> 0.0F)
                        .persistent(Codec.FLOAT)
                        .buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "overheal_absorption"));
                    PERSISTENT_DATA = AttachmentRegistry.<CompoundTag>builder()
                        .initializer(CompoundTag::new)
                        .persistent(CompoundTag.CODEC)
                        .buildAndRegister(Identifier.fromNamespaceAndPath(MODID, "persistent_data"));
                } catch (Throwable t) {
                    // Suppressed in headless unit tests where Loom mixin transformation is inactive
                }
            }
        }
    }

    public static class Tags {
        public static final TagKey<Attribute> DYNAMIC_BASE_ATTRIBUTES = TagKey.create(Registries.ATTRIBUTE, Identifier.fromNamespaceAndPath(MODID, "dynamic_base"));
        public static final TagKey<DamageType> CANNOT_CRITICALLY_STRIKE = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "cannot_critically_strike"));
        public static final TagKey<DamageType> IS_NON_PHYSICAL = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "is_non_physical"));
        public static final TagKey<DamageType> IS_PHYSICAL = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("c", "is_physical"));
    }

    public static void bootstrap() {
        Attributes.bootstrap();
        MobEffects.bootstrap();
        Particles.bootstrap();
        Sounds.bootstrap();
        DamageTypes.bootstrap();
        Attachments.bootstrap();
    }
}
