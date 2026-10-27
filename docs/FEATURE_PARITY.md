# Feature Parity Matrix — Apotheosis NeoForge (26.1.2) to Fabric (26.3)

## 1. Overview

This matrix tracks the parity status of features across the Apotheosis ecosystem as they are migrated from the pinned upstream Minecraft 26.1.2 NeoForge version to Minecraft 26.3 Fabric.

Status Indicators:
- `NOT_STARTED`: Feature identified in upstream, porting work not yet begun.
- `IN_PROGRESS`: Porting active, partial implementation or testing underway.
- `PARITY_VERIFIED`: Implemented, tested, and verified against vanilla and upstream behavior.
- `REPLACED_BY_VANILLA`: Feature superseded by vanilla 26.3; upstream code dropped.
- `DROPPED`: Explicitly excluded per architecture guidelines.

---

## 2. Apothic Spawners

| Upstream Feature | Status | Implementation Plan / Notes |
| :--- | :--- | :--- |
| **Spawner Stat Modifiers** | `NOT_STARTED` | Configurable modifier items (sugar, clock, ghast tear, blaze rod, etc.) modifying `minSpawnDelay`, `maxSpawnDelay`, `spawnCount`, `maxNearbyEntities`, `requiredPlayerRange`, `spawnRange`. |
| **Redstone Control** | `NOT_STARTED` | Comparator / redstone activation toggle on spawner block entity. |
| **Ignore Conditions** | `NOT_STARTED` | Ignore players, ignore light levels, ignore spawn conditions. |
| **Silk Touch Capture** | `NOT_STARTED` | Drop spawner block item on Silk Touch mine; preserve modifiers via custom data component. |
| **Spawner Item Tooltips** | `NOT_STARTED` | Display modified values and +/- deltas on hover using 26.3 `TooltipDisplay`. |
| **In-Game Configuration** | `NOT_STARTED` | YACL integration for spawner modifier values, limits, and items. |

---

## 3. Apothic Attributes

| Upstream Feature | Status | Implementation Plan / Notes |
| :--- | :--- | :--- |
| **Custom Mining Speed** | `REPLACED_BY_VANILLA` | Upstream `apothic_attributes:mining_speed` dropped in favor of vanilla `minecraft:mining_efficiency`. |
| **Combat / Utility Attributes** | `NOT_STARTED` | Life steal, overcure, armor shred, cold damage, etc. |
| **Attribute Tooltip Formatting** | `NOT_STARTED` | Clean visual formatting of attribute modifiers with coloring and operations. |

---

## 4. Apothic Enchanting

| Upstream Feature | Status | Implementation Plan / Notes |
| :--- | :--- | :--- |
| **Enchanting Stats (Eterna, Quanta, Arcana)** | `NOT_STARTED` | Bookshelf stats calculation, custom enchanting GUI / table integration. |
| **High-Tier Enchanting Levels** | `NOT_STARTED` | Enchantment level capping beyond vanilla 30 levels (up to level 100). |
| **Tomes of Extraction / Scrapping** | `NOT_STARTED` | Extracting enchantments from items onto books. |
| **Capturing Enchantment** | `NOT_STARTED` | Drops spawn eggs from killed mobs based on capturing level. |

---

## 5. Placebo Utilities (Required Subset)

| Upstream Feature | Status | Implementation Plan / Notes |
| :--- | :--- | :--- |
| **Dynamic Recipe Helpers** | `NOT_STARTED` | Data-driven / programmatic recipe builders for spawner modifiers. |
| **JSON Serialization Utilities** | `NOT_STARTED` | Gson codecs and codec bridges compatible with 26.3 Codec systems. |
