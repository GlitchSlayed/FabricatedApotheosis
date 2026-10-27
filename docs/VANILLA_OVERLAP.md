# Vanilla Overlap Analysis — 26.1.2 vs 26.3

## 1. Overview & Methodology

This document tracks overlap analysis between upstream Apotheosis (26.1.2 NeoForge) and vanilla Minecraft (26.1.2 vs 26.3), evaluated using the verified local reference trees (`reference/26.1.2client.jar` and `reference/26.3client.jar`).

Per `AGENTS.md` instructions, every ported feature is audited against vanilla 26.3 and Fabric API capabilities to prevent unnecessary duplication, prevent regressions, and enforce clean architecture.

---

## 2. Attributes System Overlap

### 2.1 Mining Speed Attribute
- **Upstream Implementation**: `apothic_attributes:mining_speed` provided a custom attribute modifier for block break speed.
- **Vanilla 26.1.2 & 26.3 Evaluation**:
  - `net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY` (`minecraft:mining_efficiency`) is a standard vanilla attribute.
  - `net.minecraft.world.entity.ai.attributes.Attributes.SUBMERGED_MINING_SPEED` (`minecraft:submerged_mining_speed`) natively handles underwater break speed.
- **Decision**: **REPLACE / EXCLUDE**.
  - Do NOT port `apothic_attributes:mining_speed`.
  - All upstream modifiers, gems, and enchants granting mining speed must be mapped directly to vanilla `minecraft:mining_efficiency`.

### 2.2 New Vanilla 26.3 Attributes
Vanilla 26.3 added 5 new attributes not present in 26.1.2:
1. `Attributes.AIR_DRAG_MODIFIER`
2. `Attributes.BELOW_NAME_DISTANCE`
3. `Attributes.BOUNCINESS`
4. `Attributes.FRICTION_MODIFIER`
5. `Attributes.NAME_TAG_DISTANCE`

- **Decision**: **ADAPT**.
  - Review any Apothic movement or physics modifiers (e.g. friction, bounce, or step height) to ensure they integrate with rather than duplicate these new vanilla 26.3 holders.

---

## 3. Spawners & Mob Mechanics Overlap

### 3.1 BaseSpawner & SpawnerBlockEntity API Changes
- **Vanilla Comparison Findings**:
  - In 26.1.2, `BaseSpawner` relied primarily on `setEntityId(EntityType<?>, Level, RandomSource, BlockPos)`.
  - In 26.3, `BaseSpawner` and `SpawnerBlockEntity` added:
    - `setEntityData(TypedEntityData<EntityType<?>>, Level, RandomSource, BlockPos)`
    - `public static final EntityProcessor SET_DISPLAY_ENTITY_ID`
    - `private static final int SPAWNER_ENTITY_ID`
- **Behavioral Impact**:
  - Vanilla 26.3 moved towards strictly typed entity data components (`TypedEntityData`).
- **Decision**: **ADAPT**.
  - Spawner modifiers that change the spawn egg or mob type must use `setEntityData` with `TypedEntityData` to maintain compatibility with 26.3 entity serialization.
  - Mixin targets on `BaseSpawner.serverTick` and `BaseSpawner.clientTick` must respect the new `SET_DISPLAY_ENTITY_ID` processor.

### 3.2 Spawner Silk Touch & Persistence
- **Vanilla 26.3 Behavior**:
  - Spawners drop experience points and break without dropping an item when mined, even with Silk Touch.
- **Decision**: **KEEP (MOD FEATURE)**.
  - Apothic Spawners' silk-touch capture and modifier persistence is a core gameplay feature not present in vanilla.
  - Implement silk-touch drop via loot table conditions (`minecraft:match_tool` with silk touch predicate) and block drops mixin.
  - Persist modifiers using vanilla `DataComponents.CUSTOM_DATA` / custom component registration.

---

## 4. Item Components & Tooltip Systems

### 4.1 Data Components Expansion
- **Vanilla 26.1.2**: 112 `DataComponentType` registrations.
- **Vanilla 26.3**: 124 `DataComponentType` registrations (+12 components).
- **New Components in 26.3**:
  - `COOKING_FUEL` & `BREWING_FUEL`: Natively define fuel items and burn durations without requiring custom registry hacks.
  - `COMPOSTABLE`: Replaces legacy ComposterBlock table injections.
  - `ATTACK_ANIMATION` & `INTERACT_ANIMATION`: Natively customize weapon/item swing animations.
  - `MOB_VISIBILITY`: Controls mob detection ranges on armor/items.
- **Decision**: **ADAPT / REPLACE**.
  - Replace any legacy Placebo custom fuel or composter hooks with the standard 26.3 `DataComponents.COOKING_FUEL` and `DataComponents.COMPOSTABLE`.
  - Use `ATTACK_ANIMATION` and `INTERACT_ANIMATION` for custom tool rendering instead of client tick hacks.

### 4.2 Tooltip Display
- **Vanilla 26.3**: Tooltips are governed by `net.minecraft.world.item.component.TooltipDisplay` and `Item.TooltipContext`.
- **Decision**: **ADAPT**.
  - Apothic tooltips (showing spawner stat deltas, enchantment info, attribute formatting) must hook into `Item.appendHoverText` using the new `TooltipDisplay` / `TooltipContext` parameters.

---

## 5. Summary Matrix of Keep / Replace / Adapt Decisions

| Component / Feature | Action | Vanilla / Platform Equivalent | Rationale |
| :--- | :--- | :--- | :--- |
| `apothic_attributes:mining_speed` | **REPLACE** | `Attributes.MINING_EFFICIENCY` | Redundant; already provided by vanilla |
| Spawner Mob Modification | **ADAPT** | `BaseSpawner.setEntityData` | Updated to 26.3 `TypedEntityData` API |
| Spawner Silk Touch Drops | **KEEP** | Custom Loot Table + Mixin | Core mod feature not in vanilla |
| Custom Fuels / Composting | **REPLACE** | `DataComponents.COOKING_FUEL` / `COMPOSTABLE` | Native in 26.3 |
| Spawner Tooltip Deltas | **ADAPT** | `Item.appendHoverText` with `TooltipDisplay` | Compatible with 26.3 tooltip system |
