# Apothic Spawners: Feature Inventory & Specification

- **Module**: `apothic_spawners` (bundled into `fabricated-apotheosis` ecosystem distributable)
- **Target Platform**: Minecraft 26.3 Fabric (Fabric Loader 0.19.5, Loom 1.17.21, YACL 3.9.6)
- **Upstream Baseline**: Pinned `Apothic-Spawners` 26.1.2 commit `c89d20c`

---

## 1. Registry & Object Inventory

### 1.1 Blocks & Block Entities
| Registry Key | Class | Vanilla Target / Overlap | Purpose / Mechanics |
| :--- | :--- | :--- | :--- |
| `minecraft:spawner` | `ApothSpawnerBlock` | `Blocks.SPAWNER` (`SpawnerBlock`) | Subclasses `SpawnerBlock`. Handles item interaction (`useItemOn`) for modifiers/eggs, silk touch harvest drops, placement state restoration, and explosive instability triggering (`onExplosionHit`). |
| `minecraft:mob_spawner` | `ApothSpawnerTile` | `BlockEntityType.MOB_SPAWNER` (`SpawnerBlockEntity`) | Subclasses `SpawnerBlockEntity` with `SpawnerLogicExt`. Manages 16 custom spawner stats, instability countdown, entity spawning pipeline, and NBT serialization. |

### 1.2 Items
| Registry Key | Class | Vanilla Target / Overlap | Purpose / Mechanics |
| :--- | :--- | :--- | :--- |
| `minecraft:spawner` | `ApothSpawnerItem` | `Items.SPAWNER` (`BlockItem`) | Custom `BlockItem` for spawner. Dynamically formats item name based on contained entity (`SpawnData.entity.id`) with category coloring (Monster = Red, Creature = Dark Green, Water = Blue, etc.). Displays modifier stat summary on Shift tooltip via client handler. |

### 1.3 Data Components & Enchantments
| Registry Key | Type / Value | Target / Application | Description |
| :--- | :--- | :--- | :--- |
| `apothic_spawners:capturing` | `DataComponentType<Float>` | `EnchantmentEffect` | Enchantment effect component holding the capturing drop chance per level (default: 0.005 / 0.5% per level). |
| `apothic_spawners:capturing` | `ResourceKey<Enchantment>` | `#minecraft:enchantable/sharp_weapon` | Capturing enchantment (Max Level: 3, Min Cost: $15 + 10 \times (\text{lvl}-1)$, Max Cost: 200, Slots: Mainhand). Killing non-blacklisted mobs drops their spawn egg. |

### 1.4 Tags, Predicates & Loot Tables
| Registry Key | Registry | Value / Target | Description |
| :--- | :--- | :--- | :--- |
| `apothic_spawners:blacklisted_from_spawners` | `core:entity_type` | Tag | Entities forbidden from being inserted into spawners via spawn eggs, and excluded from the Capturing enchantment drop table (e.g. Ender Dragon, Wither, Elder Guardian, Warden). |
| `apothic_spawners:spawn_egg` | `advancements:predicate` | `DataComponentPredicate` | Matches items possessing `DataComponents.ENTITY_DATA` (spawn eggs) for advancement criteria. |
| `apothic_spawners:spawner_modifier` | `core:recipe_type` | `RecipeType<SpawnerModifier>` | Custom recipe type defining valid spawner modifications. |
| `apothic_spawners:spawner_modifier` | `core:recipe_serializer` | `RecipeSerializer<SpawnerModifier>` | Codec and StreamCodec serializer for spawner modifier recipes. |
| `apothic_spawners:spawner_stat` | `apothic_spawners:spawner_stat` | Custom Registry | Registry of 16 spawner stats (`SpawnerStats.REGISTRY`). |
| `apothic_spawners:spawner_modifier` | `core:trigger_type` | `ModifierTrigger` | Advancement criterion trigger firing when a player successfully modifies a spawner. |
| `apothic_spawners:gameplay/unstable_spawner` | `core:loot_table` | Loot Table | Drops scattered when an unstable spawner finishes its 60-tick countdown and detonates. |

---

## 2. Spawner Statistics Inventory (16 Stats)

| Stat ID | Type | Default | Bounds / Units | Description & Gameplay Effect |
| :--- | :--- | :--- | :--- | :--- |
| `min_delay` | `VanillaStat` | 200 | [20, 1600] ticks | Minimum delay between spawn cycles. |
| `max_delay` | `VanillaStat` | 800 | [20, 1600] ticks | Maximum delay between spawn cycles. Clamped to $\ge \text{min\_delay}$. |
| `spawn_count` | `VanillaStat` | 4 | [1, 16] entities | Number of entities spawned per cycle. |
| `max_nearby_entities` | `VanillaStat` | 6 | [1, 32] entities | Spawn abort limit if matching entities within `spawn_range` reach this threshold. |
| `req_player_range` | `VanillaStat` | 16 | [1, 48] blocks | Activation distance for player proximity. Ignored if `ignore_players` is true. |
| `spawn_range` | `VanillaStat` | 4 | [1, 16] blocks | Horizontal bounding radius in which entities spawn around the block. |
| `initial_health` | `PercentageStat` | 1.0 (100%) | [0.05, 1.00] % | Health multiplier applied to living entities immediately upon spawning. |
| `ignore_players` | `BooleanStat` | `false` | Boolean | When true, spawner ticks and spawns entities even if no player is in range. |
| `ignore_conditions` | `BooleanStat` | `false` | Boolean | When true, ignores entity placement rules, daylight, dark requirements, and block collisions. |
| `redstone_control` | `BooleanStat` | `false` | Boolean | When true, spawner requires a redstone signal (neighbor signal $> 0$) to activate. |
| `ignore_light` | `BooleanStat` | `false` | Boolean | When true, spawner simulates all light levels $[0, 15]$ via `LyingLevel` to satisfy light rules. |
| `no_ai` | `BooleanStat` | `false` | Boolean | When true, spawned mobs have `NoAI = true` and `apotheosis:movable = true` (gravity/pushable without pathfinding). |
| `silent` | `BooleanStat` | `false` | Boolean | When true, spawned entities have `Silent = true`. |
| `youthful` | `BooleanStat` | `false` | Boolean | When true, ageable mobs spawn as babies (`setBaby(true)`). |
| `burning` | `BooleanStat` | `false` | Boolean | When true, non-fire-immune entities spawn permanently on fire (`Integer.MAX_VALUE` fire ticks). |
| `echoing` | `LevelStat` | 0 | [0, 3] levels | Extra loot table rolls and bonus experience drops ($1 + \text{level}$) on entity death. |

---

## 3. Spawner Modifier Recipes (32 Recipes: 16 Base + 16 Inverse)

All recipes belong to `apothic_spawners:spawner_modifier`. Inverse recipes require `minecraft:quartz` in the offhand.

| Modifying Item (Mainhand) | Offhand | Stat Target | Change Applied | Clamped Range |
| :--- | :--- | :--- | :--- | :--- |
| `minecraft:sugar` | _None_ | `min_delay` | -10 ticks | Min: 20 ticks |
| `minecraft:sugar` | `minecraft:quartz` | `min_delay` | +10 ticks | Max: 1600 ticks |
| `minecraft:clock` | _None_ | `max_delay` | -20 ticks | Min: 20 ticks |
| `minecraft:clock` | `minecraft:quartz` | `max_delay` | +20 ticks | Max: 1600 ticks |
| `minecraft:fermented_spider_eye` | _None_ | `spawn_count` | +1 entity | Max: 16 entities |
| `minecraft:fermented_spider_eye` | `minecraft:quartz` | `spawn_count` | -1 entity | Min: 1 entity |
| `minecraft:ghast_tear` | _None_ | `max_nearby_entities` | +2 entities | Max: 32 entities |
| `minecraft:ghast_tear` | `minecraft:quartz` | `max_nearby_entities` | -2 entities | Min: 1 entity |
| `minecraft:prismarine_crystals` | _None_ | `req_player_range` | +2 blocks | Max: 48 blocks |
| `minecraft:prismarine_crystals` | `minecraft:quartz` | `req_player_range` | -2 blocks | Min: 1 block |
| `minecraft:blaze_rod` | _None_ | `spawn_range` | +1 block | Max: 16 blocks |
| `minecraft:blaze_rod` | `minecraft:quartz` | `spawn_range` | -1 block | Min: 1 block |
| `minecraft:golden_carrot` | _None_ | `initial_health` | -5% ($-0.05$) | Min: 5% ($0.05$) |
| `minecraft:golden_carrot` | `minecraft:quartz` | `initial_health` | +5% ($+0.05$) | Max: 100% ($1.00$) |
| `minecraft:nether_star` | _None_ | `ignore_players` | `SET true` | N/A |
| `minecraft:nether_star` | `minecraft:quartz` | `ignore_players` | `SET false` | N/A |
| `minecraft:dragon_egg` | _None_ | `ignore_conditions` | `SET true` | N/A |
| `minecraft:dragon_egg` | `minecraft:quartz` | `ignore_conditions` | `SET false` | N/A |
| `minecraft:comparator` | _None_ | `redstone_control` | `SET true` | N/A |
| `minecraft:comparator` | `minecraft:quartz` | `redstone_control` | `SET false` | N/A |
| `minecraft:chorus_fruit` | _None_ | `no_ai` | `SET true` | N/A |
| `minecraft:chorus_fruit` | `minecraft:quartz` | `no_ai` | `SET false` | N/A |
| `minecraft:glowstone` | _None_ | `ignore_light` | `SET true` | N/A |
| `minecraft:glowstone` | `minecraft:quartz` | `ignore_light` | `SET false` | N/A |
| `#minecraft:wool` | _None_ | `silent` | `SET true` | N/A |
| `#minecraft:wool` | `minecraft:quartz` | `silent` | `SET false` | N/A |
| `minecraft:turtle_egg` | _None_ | `youthful` | `SET true` | N/A |
| `minecraft:turtle_egg` | `minecraft:quartz` | `youthful` | `SET false` | N/A |
| `minecraft:magma_cream` | _None_ | `burning` | `SET true` | N/A |
| `minecraft:magma_cream` | `minecraft:quartz` | `burning` | `SET false` | N/A |
| `minecraft:echo_shard` | _None_ | `echoing` | +1 level | Max: 3 levels |
| `minecraft:echo_shard` | `minecraft:quartz` | `echoing` | -1 level | Min: 0 levels |

---

## 4. Spawning Mechanics & Runtime Behavior

### 4.1 Activation Rules
- A spawner activates if `(ignore_players || playerInRange)` **AND** `(!redstone_control || hasNeighborSignal)`.
- When inactive, client spin freezes (`oSpin = spin`) and particles cease.

### 4.2 Spawning Pipeline
1. Compute random delay in $[\text{min\_delay}, \text{max\_delay}]$.
2. For each count in $0 \dots \text{spawn\_count}$:
   - Calculate random candidate coordinate within $[\text{pos} \pm \text{spawn\_range}]$.
   - Verify bounding box collision (`level.noCollision(entityType.getSpawnAABB(x, y, z))`).
   - If `!ignore_conditions`:
     - If `ignore_light`: evaluate spawn rules across simulated light levels $0 \dots 15$ via `LyingLevel`.
     - Else: check standard light and placement conditions (`SpawnPlacements.checkSpawnRules`).
   - Count existing matching entities within $\text{spawn\_range} + 1$; abort if $\ge \text{max\_nearby\_entities}$.
   - Spawn entity with `EntitySpawnReason.SPAWNER`:
     - If `no_ai`: `mob.setNoAi(true)` and tag `apotheosis:movable = true`.
     - If `youthful`: `mob.setBaby(true)`.
     - If `silent`: `entity.setSilent(true)`.
     - If `initial_health < 1`: set health to `maxHealth * initial_health`.
     - If `burning` and not fire-immune: `setRemainingFireTicks(Integer.MAX_VALUE)`.
     - If `echoing > 0`: tag `apothic_spawners:echoing = level`.
   - Post-spawn: emit block event `2004` (particles/sound) and dispatch `GameEvent.ENTITY_PLACE`.

### 4.3 Despawn Rules
- Spawner-spawned mobs (`EntitySpawnReason.isSpawner`) are protected from immediate despawning for `entityDespawnDelay` ticks (default: 600 ticks = 30 seconds), unless the difficulty is Peaceful.
- Natural mob spawns are completely unaffected.

### 4.4 Instability & Explosions
- When an explosion hits `ApothSpawnerBlock`:
  - Does **not** break immediately into drops.
  - Snapshot the current `nextSpawnData` into `capturedSpawnData` and begin a 60-tick countdown (`unstableTicks = 60`).
  - Spawns intense smoke/flame particles on the client.
  - On tick 0: triggers radius 8 explosion, spawns a cluster of 12 mobs of the captured entity type, and drops `apothic_spawners:gameplay/unstable_spawner` loot.
  - `unstable_ticks` and `captured_spawn_data` are persisted to NBT so chunk unloads mid-countdown still detonate correctly.

---

## 5. Persistence, Silk Touch & Save Compatibility

### 5.1 Silk Touch Harvest & Placement
- Breaking a spawner with a tool meeting `spawnerSilkLevel` (default: 1) drops `minecraft:spawner` with `DataComponents.BLOCK_ENTITY_DATA`.
- Inflicts `spawnerSilkDamage - 1` additional durability damage on the tool (default: 100 damage total).
- If `spawnersDropEmpty` is true in config, the spawner's entity data is purged before drop.
- Placing the spawner (`setPlacedBy`) re-applies all custom stats and `SpawnData` into `ApothSpawnerTile`.

### 5.2 Persistence Structure
```text
Spawner Tile NBT:
├── id: "minecraft:mob_spawner"
├── stats: CompoundTag (keyed by SpawnerStat Identifier)
│   ├── "apothic_spawners:min_delay": Int
│   ├── "apothic_spawners:max_delay": Int
│   ├── "apothic_spawners:ignore_players": Byte/Bool
│   └── ...
├── modified: Boolean
├── unstable_ticks: Int (optional, if active)
└── captured_spawn_data: CompoundTag (optional, if unstable)
```
- Disabling the spawner module preserves the standard vanilla fields (`SpawnData`, `SpawnPotentials`, `Delay`, etc.), preventing data loss on disk.

---

## 6. World Tier & Runes Integration Points (Deferred to Milestones 8 & 9)

In the full combined ecosystem:
1. **Modifier Tier Restrictions**: Dragon Egg and Nether Star modifiers can be gated behind World Tiers (e.g. Haven, Frontier, Ascended).
2. **Spawner Chains**: The `unstable_spawner.json` loot table will drop `apotheosis:spawner_chains` when Adventure mode is active.
3. **Rune Integration**: Modifier recipes will allow Apothic Runes as alternate catalyst ingredients.
