# Vanilla Overlap & Custom Attribute Audit

## 1. Overview & Methodology

This audit compares vanilla Minecraft (26.1.2, 26.2, and 26.3) against all custom attributes registered in upstream **Apothic Attributes 3.1.0** (`ALObjects.java`).

Each attribute is evaluated for:
1. Native vanilla equivalents in 26.3.
2. NeoForge-specific dependencies (`PercentageAttribute`, `BooleanAttribute`).
3. Mathematical semantics (ranges, operations, units).
4. Keep / Replace / Adapt decision.
5. Persistent migration and regression test requirements.

---

## 2. Complete Custom Attribute Audit Table

| Field Name | Registry Identifier | Type & Default | Range | Units / Semantics | Vanilla 26.3 Equivalent | Equivalence Finding | Action | Migration & Regression Test |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `ARMOR_PIERCE` | `apothic_attributes:armor_pierce` | Ranged (0.0) | [0.0, 1000.0] | Flat armor points ignored in damage | None | None in vanilla | **KEEP** | Persist as attribute; test damage vs armored targets |
| `ARMOR_SHRED` | `apothic_attributes:armor_shred` | Percentage (0.0) | [0.0, 2.0] | Percentage of target armor ignored | None | None in vanilla | **KEEP** | Persist as attribute; test proportional reduction |
| `ARROW_DAMAGE` | `apothic_attributes:arrow_damage` | Percentage (1.0) | [0.0, 10.0] | Multiplier on fired projectile base damage | None | Vanilla uses enchantment/item tags | **KEEP** | Modifies arrow entity on launch; test bow & crossbow |
| `ARROW_VELOCITY` | `apothic_attributes:arrow_velocity` | Percentage (1.0) | [0.0, 10.0] | Multiplier on arrow launch velocity | None | None in vanilla | **KEEP** | Hook projectile launch delta; test trajectory |
| `COLD_DAMAGE` | `apothic_attributes:cold_damage` | Ranged (0.0) | [0.0, 1000.0] | Flat magic damage with slow effect | None | None in vanilla | **KEEP** | Combat event injection; verify frost particles & slowness |
| `CRIT_CHANCE` | `apothic_attributes:crit_chance` | Percentage (0.05) | [0.0, 10.0] | % chance of critical strike (independent of jump) | None | Vanilla criticals are state-driven (falling) | **KEEP** | Player attack hook; test random roll vs jump critical |
| `CRIT_DAMAGE` | `apothic_attributes:crit_damage` | Percentage (1.5) | [1.0, 100.0] | Multiplier on critical strike damage | None | Vanilla jump crit is fixed 1.5x | **KEEP** | Hook attack damage calculation; test crit scaling |
| `CURRENT_HP_DAMAGE` | `apothic_attributes:current_hp_damage`| Percentage (0.0) | [0.0, 1.0] | % of target current health as bonus damage | None | None in vanilla | **KEEP** | Test diminishing return as mob health decreases |
| `DODGE_CHANCE` | `apothic_attributes:dodge_chance` | Percentage (0.0) | [0.0, 1.0] | % chance to negate incoming melee attack | None | None in vanilla | **KEEP** | LivingDamageEvent hook; test zero-damage cancellation |
| `DRAW_SPEED` | `apothic_attributes:draw_speed` | Percentage (1.0) | [0.0, 4.0] | Multiplier on bow/crossbow charge speed | None | Vanilla uses Quick Charge enchantment | **KEEP** | Hook item use tick; test charge completion duration |
| `EXPERIENCE_GAINED` | `apothic_attributes:experience_gained`| Percentage (1.0) | [0.0, 1000.0] | Multiplier on dropped experience orbs | None | None in vanilla | **KEEP** | Hook XP orb spawning on mob death & ore break |
| `FIRE_DAMAGE` | `apothic_attributes:fire_damage` | Ranged (0.0) | [0.0, 1000.0] | Flat magic damage that sets target ablaze | None | Vanilla Fire Aspect is fixed duration | **KEEP** | Hook damage dealing; test fire ticks + magic damage |
| `GHOST_HEALTH` | `apothic_attributes:ghost_health` | Ranged (0.0) | [0.0, 1000.0] | Temporary regenerating health pool | None | None in vanilla | **KEEP** | Custom health tracker; test regeneration out of combat |
| `HEALING_RECEIVED` | `apothic_attributes:healing_received` | Percentage (1.0) | [0.0, 1000.0] | Multiplier on all incoming healing | None | None in vanilla | **KEEP** | Hook `LivingEntity.heal()`; test potions, food, regen |
| `LIFE_STEAL` | `apothic_attributes:life_steal` | Percentage (0.0) | [0.0, 10.0] | % of dealt physical damage healed to attacker | None | None in vanilla | **KEEP** | Hook melee damage; test heal attacker on successful hit |
| `OVERHEAL` | `apothic_attributes:overheal` | Percentage (0.0) | [0.0, 10.0] | % of dealt damage converted to absorption hearts | None | Absorption is effect-driven | **KEEP** | Convert to absorption up to 50% max HP |
| `PROJECTILE_DAMAGE` | `apothic_attributes:projectile_damage`| Percentage (1.0) | [0.0, 10.0] | Multiplier on generic incoming projectile damage| None | None in vanilla | **KEEP** | Hook damage source checks for Projectile |
| `PROT_PIERCE` | `apothic_attributes:prot_pierce` | Ranged (0.0) | [0.0, 34.0] | Flat reduction to enchantment protection | None | None in vanilla | **KEEP** | Hook `CombatRules.getDamageAfterMagicAbsorb` |
| `PROT_SHRED` | `apothic_attributes:prot_shred` | Percentage (0.0) | [0.0, 0.90] | Proportional reduction to protection enchant | None | None in vanilla | **KEEP** | Hook protection calculation in `CombatRules` |
| `ELYTRA_FLIGHT` | `apothic_attributes:elytra_flight` | Boolean (false) | [0.0, 1.0] | Enables elytra gliding without wearing elytra | None | Requires chestplate with Elytra tag | **ADAPT** | Implement via standard `RangedAttribute(0.0, 0.0, 1.0)` |
| `COOLDOWN_REDUCTION`| `apothic_attributes:cooldown_reduction`| Percentage (0.0) | [-10.0, 0.95] | Multiplicative reduction to item/spell cooldowns| None | None in vanilla | **KEEP** | Hook item cooldown tracker; test cooldown duration |

---

## 3. Mining Speed Attribute Confirmation

- **Upstream Verification**: In earlier versions of Apotheosis, `apothic_attributes:mining_speed` existed as a custom attribute.
- **Audit Result**: Verified that `mining_speed` has been **completely removed** from upstream `Apothic-Attributes 3.1.0`.
- **Vanilla Comparison**: Both Minecraft 26.1.2 and 26.3 natively include:
  - `net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY` (`minecraft:mining_efficiency`)
  - `net.minecraft.world.entity.ai.attributes.Attributes.SUBMERGED_MINING_SPEED` (`minecraft:submerged_mining_speed`)
- **Strict Rule Enforcement**: `mining_speed` will NOT be created. Any references in loot, tooltips, or gems must target vanilla `minecraft:mining_efficiency`.

---

## 4. NeoForge Type Adaptations for Fabric

Upstream relies on NeoForge helper classes:
- `net.neoforged.neoforge.common.PercentageAttribute`
- `net.neoforged.neoforge.common.BooleanAttribute`
- `net.neoforged.neoforge.common.ItemAttributeModifierEvent`
- NeoForge merged attribute tooltip formatting (`NeoForgeMod.enableMergedAttributeTooltips()`)
- NeoForge equipment slot abstractions (`EntityEquipmentSlot`, `EntitySlotGroup`, `CurioEquipmentSlot`)

### Fabric Implementation Decisions:
1. **Attribute Class Architecture**:
   - Vanilla `RangedAttribute` handles min, max, and base values identically to NeoForge.
   - All 21 custom attributes are instantiated as standard `RangedAttribute` instances registered with `DeferredHelper` under `apothic_attributes:*` with `.setSyncable(true)`.
   - Percentage and boolean semantics are decoupled from entity calculation classes and encapsulated in [`AttributeFormatting`](file:///home/vansh/Documents/projects/FabricatedApotheosis/src/main/java/dev/shadowsoffire/apotheosis/attributes/format/AttributeFormatting.java).
2. **Formatting Semantics**:
   - `PERCENTAGE`: Formatted as percentage values (e.g. `+15%` or `150%`), scaled by `* 100.0` for addition modifiers.
   - `BOOLEAN`: Formatted as `Enabled` (`value >= 1.0`) or `Disabled` (`value < 1.0`).
   - `FLAT`: Formatted as plain numbers with up to 2 decimal places.
3. **Equipment Slot & Modifier Source Abstraction**:
   - Upstream's `EntityEquipmentSlot` was designed to extend beyond vanilla slots to support Curios.
   - On Fabric, we implement an open registry [`ModifierSourceType`](file:///home/vansh/Documents/projects/FabricatedApotheosis/src/client/java/dev/shadowsoffire/apotheosis/client/attributes/ModifierSourceType.java):
     - `EQUIPMENT` extracts from all 6 vanilla `EquipmentSlot` types using `ItemStack.forEachModifier()`.
     - `MOB_EFFECT` extracts from active entity potion effects.
     - Extension point `ModifierSourceType.register()` is provided for optional accessory mods (Trinkets / Accessories) without introducing compile-time hard dependencies.
4. **Modifier Provenance & Accumulation Prevention**:
   - `ItemStack` modifiers are managed strictly through Minecraft 26.3 `DataComponents.ATTRIBUTE_MODIFIERS`.
   - Equipping and unequipping recalculates effective modifiers through vanilla `LivingEntity.collectEquipmentChanges()` without retaining stateful modifier caches, preventing modifier leakage or accumulation bugs.

---

## 5. Persistent Data & Component Migration

1. **ItemStack Attribute Modifiers**:
   - Upstream uses Minecraft's data component `DataComponents.ATTRIBUTE_MODIFIERS`.
   - Modifiers saved under `apothic_attributes:*` namespace retain their persistent `Identifier` keys.
2. **Obsolete Mining Speed Migration**:
   - When loading item stacks with legacy `apothic_attributes:mining_speed` attribute modifiers, they are migrated directly to `minecraft:mining_efficiency`.
   - Explicit check in item modifier processing ensures that `apothic_attributes:mining_speed` is never registered in the attribute registry and never applied concurrently with vanilla mining efficiency.
3. **Entity Persistent Data**:
   - `Overheal` and `GhostHealth` store auxiliary damage data in entity components/attachments.
   - On Fabric, migrate entity auxiliary data to Fabric Data Attachment API for clean world save synchronization.
