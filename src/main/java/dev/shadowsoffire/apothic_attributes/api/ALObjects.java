package dev.shadowsoffire.apothic_attributes.api;

import dev.shadowsoffire.apothic_attributes.modifiers.EntityEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import dev.shadowsoffire.apothic_attributes.modifiers.VanillaEquipmentSlot;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;

public class ALObjects extends dev.shadowsoffire.apotheosis.attributes.api.ALObjects {

    public static class BuiltInRegs {
        public static final ResourceKey<Registry<EntityEquipmentSlot>> ENTITY_EQUIPMENT_SLOT_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "entity_equipment_slot"));
        public static final ResourceKey<Registry<EntitySlotGroup>> ENTITY_SLOT_GROUP_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "entity_slot_group"));

        public static final Registry<EntityEquipmentSlot> ENTITY_EQUIPMENT_SLOT = createRegistry(ENTITY_EQUIPMENT_SLOT_KEY);
        public static final Registry<EntitySlotGroup> ENTITY_SLOT_GROUP = createRegistry(ENTITY_SLOT_GROUP_KEY);

        private static <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key) {
            try {
                return FabricRegistryBuilder.create(key).attribute(RegistryAttribute.SYNCED).buildAndRegister();
            }
            catch (UnsupportedOperationException e) {
                return new net.minecraft.core.MappedRegistry<>(key, com.mojang.serialization.Lifecycle.stable());
            }
        }

        public static void bootstrap() {}
    }

    public static class EquipmentSlots {
        public static final Holder<EntityEquipmentSlot> MAINHAND = slot(EquipmentSlot.MAINHAND);
        public static final Holder<EntityEquipmentSlot> OFFHAND = slot(EquipmentSlot.OFFHAND);
        public static final Holder<EntityEquipmentSlot> HEAD = slot(EquipmentSlot.HEAD);
        public static final Holder<EntityEquipmentSlot> CHEST = slot(EquipmentSlot.CHEST);
        public static final Holder<EntityEquipmentSlot> LEGS = slot(EquipmentSlot.LEGS);
        public static final Holder<EntityEquipmentSlot> FEET = slot(EquipmentSlot.FEET);
        public static final Holder<EntityEquipmentSlot> BODY = slot(EquipmentSlot.BODY);
        public static final Holder<EntityEquipmentSlot> SADDLE = slot(EquipmentSlot.SADDLE);

        private static Holder<EntityEquipmentSlot> slot(EquipmentSlot slot) {
            Identifier id = Identifier.fromNamespaceAndPath(MODID, slot.getSerializedName());
            return Registry.registerForHolder(BuiltInRegs.ENTITY_EQUIPMENT_SLOT, id, new VanillaEquipmentSlot(slot));
        }

        public static void bootstrap() {}
    }

    public static class EquipmentSlotGroups {
        public static final EntitySlotGroup ANY = group("any", new AnyHolderSet<>(BuiltInRegs.ENTITY_EQUIPMENT_SLOT));
        public static final EntitySlotGroup ANY_VANILLA = group("any_vanilla", HolderSet.direct(
            EquipmentSlots.MAINHAND, EquipmentSlots.OFFHAND, EquipmentSlots.HEAD,
            EquipmentSlots.CHEST, EquipmentSlots.LEGS, EquipmentSlots.FEET, EquipmentSlots.BODY));

        public static final EntitySlotGroup MAINHAND = group("mainhand", HolderSet.direct(EquipmentSlots.MAINHAND));
        public static final EntitySlotGroup OFFHAND = group("offhand", HolderSet.direct(EquipmentSlots.OFFHAND));
        public static final EntitySlotGroup HAND = group("hand", HolderSet.direct(EquipmentSlots.MAINHAND, EquipmentSlots.OFFHAND));
        public static final EntitySlotGroup HEAD = group("head", HolderSet.direct(EquipmentSlots.HEAD));
        public static final EntitySlotGroup CHEST = group("chest", HolderSet.direct(EquipmentSlots.CHEST));
        public static final EntitySlotGroup LEGS = group("legs", HolderSet.direct(EquipmentSlots.LEGS));
        public static final EntitySlotGroup FEET = group("feet", HolderSet.direct(EquipmentSlots.FEET));
        public static final EntitySlotGroup ARMOR = group("armor", HolderSet.direct(EquipmentSlots.HEAD, EquipmentSlots.CHEST, EquipmentSlots.LEGS, EquipmentSlots.FEET));
        public static final EntitySlotGroup BODY = group("body", HolderSet.direct(EquipmentSlots.BODY));
        public static final EntitySlotGroup SADDLE = group("saddle", HolderSet.direct(EquipmentSlots.SADDLE));

        private static EntitySlotGroup group(String path, HolderSet<EntityEquipmentSlot> slots) {
            Identifier id = Identifier.fromNamespaceAndPath(MODID, path);
            EntitySlotGroup group = new EntitySlotGroup(id, slots);
            return Registry.register(BuiltInRegs.ENTITY_SLOT_GROUP, id, group);
        }

        public static void bootstrap() {}
    }

    public static void bootstrap() {
        dev.shadowsoffire.apotheosis.attributes.api.ALObjects.bootstrap();
        BuiltInRegs.bootstrap();
        EquipmentSlots.bootstrap();
        EquipmentSlotGroups.bootstrap();
    }
}
