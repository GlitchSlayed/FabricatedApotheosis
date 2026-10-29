# Handoff Report — Milestone 7: Apothic Spawners Core, Persistence & Instability

- **Date**: 2026-10-28
- **Target Version**: Minecraft 26.3 Fabric (Loom 1.17.21, Loader 0.19.5, YACL 3.9.6)

## 1. Current Repository Tree

```text
FabricatedApotheosis/
├── .github/workflows/build.yml
├── .gitignore
├── AGENTS.md
├── Agents.md -> AGENTS.md
├── LICENSE (MIT - Code)
├── LICENSE_ASSETS (All Rights Reserved - Upstream Assets)
├── NOTICE (Third-Party Attributions)
├── README.md
├── build.gradle (with verifyNoDuplicateClasses & DuplicatesStrategy.FAIL)
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DAMAGE_PIPELINE.md
│   ├── DECISIONS.md
│   ├── DEPENDENCY_MATRIX.md
│   ├── ENCHANTING_INVENTORY.md
│   ├── FEATURE_PARITY.md
│   ├── HANDOFF_REPORT.md
│   ├── PORTING_SURFACE.md
│   ├── REFERENCE_INDEX.md
│   ├── SAVE_COMPATIBILITY.md
│   ├── SPAWNERS_INVENTORY.md
│   ├── STATUS.md
│   ├── TEST_PLAN.md
│   ├── UPSTREAM_LOCK.md
│   └── VANILLA_OVERLAP.md
├── reference -> Refrences (symlink, excluded from git)
├── Refrences/ (read-only reference jars and classes, excluded from git)
├── upstream/ (cloned upstream 26.1.2 sources, excluded from git)
├── src/
│   ├── main/
│   │   ├── java/dev/shadowsoffire/
│   │   │   ├── apotheosis/
│   │   │   │   ├── Apotheosis.java
│   │   │   │   ├── adventure/AdventureModule.java
│   │   │   │   ├── attributes/
│   │   │   │   ├── config/
│   │   │   │   ├── enchanting/EnchantingModule.java
│   │   │   │   ├── integrations/
│   │   │   │   ├── placebo/
│   │   │   │   ├── platform/
│   │   │   │   └── spawners/SpawnersModule.java
│   │   │   ├── apothic_enchanting/
│   │   │   ├── apothic_spawners/
│   │   │   │   ├── ApothicSpawners.java
│   │   │   │   ├── ASConfig.java
│   │   │   │   ├── ASObjects.java
│   │   │   │   ├── advancements/
│   │   │   │   │   ├── ModifierTrigger.java
│   │   │   │   │   └── SpawnEggItemPredicate.java
│   │   │   │   ├── block/
│   │   │   │   │   ├── ApothSpawnerBlock.java
│   │   │   │   │   ├── ApothSpawnerItem.java
│   │   │   │   │   ├── ApothSpawnerTile.java
│   │   │   │   │   └── LyingLevel.java
│   │   │   │   ├── enchantments/
│   │   │   │   │   └── CapturingEnchantmentEffect.java
│   │   │   │   ├── mixin/
│   │   │   │   │   ├── BlockEntityTypesMixin.java
│   │   │   │   │   ├── BlocksMixin.java
│   │   │   │   │   ├── EntityTeleportMixin.java
│   │   │   │   │   ├── ItemStackMixin.java
│   │   │   │   │   ├── ItemsMixin.java
│   │   │   │   │   ├── MobDespawnMixin.java
│   │   │   │   │   ├── MobTickMixin.java
│   │   │   │   │   └── SlimeSplitMixin.java
│   │   │   │   ├── modifiers/
│   │   │   │   │   ├── SpawnerModifier.java
│   │   │   │   │   ├── SpawnerRecipeCache.java
│   │   │   │   │   └── StatModifier.java
│   │   │   │   └── stats/
│   │   │   │       ├── BooleanStat.java
│   │   │   │       ├── CustomStat.java
│   │   │   │       ├── LevelStat.java
│   │   │   │       ├── PercentageStat.java
│   │   │   │       ├── SpawnerStat.java
│   │   │   │       ├── SpawnerStats.java
│   │   │   │       └── VanillaStat.java
│   │   │   └── placebo/
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       ├── apotheosis.accesswidener
│   │       ├── apothic_enchanting.mixins.json
│   │       ├── apothic_spawners.mixins.json
│   │       ├── assets/apothic_spawners/
│   │       └── data/apothic_spawners/
│   ├── client/
│   │   ├── java/dev/shadowsoffire/
│   │   │   ├── apotheosis/client/
│   │   │   ├── apothic_enchanting/
│   │   │   ├── apothic_spawners/client/
│   │   │   │   └── ASClient.java
│   │   │   └── placebo/
│   │   └── resources/
│   └── test/
│       └── java/dev/shadowsoffire/
│           ├── apotheosis/
│           ├── apothic_enchanting/
│           └── apothic_spawners/
│               └── SpawnerTests.java
```

---

## 2. Milestone 7 Deliverables

1. **Feature Inventory & Specifications**:
   - Authored `docs/SPAWNERS_INVENTORY.md` covering all 16 spawner stats, 32 modifier recipes, instability mechanics, drops, tags, data, and World Tier integration points.
2. **16 Spawner Statistics (`SpawnerStats`)**:
   - `min_delay` [20, 1600], `max_delay` [20, 1600], `spawn_count` [1, 16], `max_nearby_entities` [1, 32], `req_player_range` [1, 48], `spawn_range` [1, 16], `initial_health` [0.05, 1.00], `ignore_players`, `ignore_conditions`, `redstone_control`, `ignore_light`, `no_ai`, `silent`, `youthful`, `burning`, `echoing` [0, 3].
3. **32 Spawner Modifier Recipes**:
   - 16 base and 16 inverse recipes (quartz in offhand) with exact clamping bounds and server-authoritative item consumption preventing waste.
4. **Vanilla In-Place Replacement**:
   - Subclassed `SpawnerBlock`, `SpawnerBlockEntity`, and `BlockItem` replacing vanilla constants via `BlocksMixin`, `BlockEntityTypesMixin`, and `ItemsMixin` while maintaining vanilla identifiers (`minecraft:spawner`, `minecraft:mob_spawner`).
5. **Silk Touch Harvest & Placement State Restoration**:
   - `ApothSpawnerBlock.playerDestroy`: requires `spawnerSilkLevel` (1), inflicts `spawnerSilkDamage` (100) tool damage, and drops `minecraft:spawner` with full `DataComponents.BLOCK_ENTITY_DATA`.
   - `setPlacedBy`: restores all custom stats, delay, and entity spawn potentials.
6. **Explosive Instability & Cascade Detonation**:
   - Intercepted `onExplosionHit` to snapshot `nextSpawnData`, cancel instant destruction, and initiate a 60-tick countdown (`unstableTicks`) culminating in an 8-radius explosion, 12-entity cluster spawn, and `apothic_spawners:gameplay/unstable_spawner` loot drops.
7. **LyingLevel Light Simulation**:
   - Simulated light levels $0 \dots 15$ for `ignore_light` condition checks using `LyingLevel` (`ServerLevelAccessor` decorator).
8. **Entity Behavior Modifiers**:
   - `no_ai` mobs receive `apotheosis:movable` (physics velocity in `MobTickMixin`, teleport cancellation in `EntityTeleportMixin`, split cube inheritance in `SlimeSplitMixin`).
   - `youthful` baby state, `silent`, `initial_health` scaling, `burning` fire ticks, and `echoing` bonus loot drops and XP multiplier ($1 + \text{level}$).
9. **Spawner-Origin Despawn Protection**:
   - Spawner-spawned entities tagged with `apothic_spawners:from_spawner`. `MobDespawnMixin` prevents premature despawn during `tickCount <= entityDespawnDelay` (600 ticks) without altering natural mob spawns or Peaceful checks.
10. **Capturing Enchantment & Blacklist Protection**:
    - `CapturingEnchantmentEffect` (0.5% per level egg drop chance).
    - Entities in `#apothic_spawners:blacklisted_from_spawners` (e.g. Ender Dragon, Wither, Elder Guardian, Warden) cannot be dropped via Capturing or inserted into spawners via spawn eggs.
11. **Client Tooltip Integration**:
    - Category-colored spawner item names (Monster, Creature, Water, Ambient) and Shift hover stat modification summary via client tooltip handlers.

---

## 3. Verification & Test Evidence

1. **Automated Unit Tests**:
   - Executed `./gradlew test --rerun-tasks`: 49 tests executed across 12 test classes, 49 passed in 4s.
   - 10 comprehensive tests in `SpawnerTests.java`:
     - `testStatClampingAndBounds`: Verified clamping across min/max bounds and quartz inverse modifiers.
     - `testModifierRecipeSerialization`: Verified JSON and StreamCodec serialization for modifier recipes.
     - `testSilkTouchHarvestLogic`: Verified tool damage, silk touch requirement, and empty spawner config.
     - `testPlacementStateRestoration`: Verified stat and nextSpawnData restoration on placement.
     - `testRedstoneActivationControl`: Verified neighbor signal requirement.
     - `testInstabilityDetonationCountdown`: Verified 60-tick countdown, cluster spawning, and loot table drop.
     - `testSpawnerOriginDespawnTagging`: Verified persistent tag and despawn delay threshold.
     - `testSpecialEntityModifiers`: Verified movable, youthful, burning, and echoing modifiers.
     - `testBlacklistAndCapturingEnchantment`: Verified blacklist rejection and egg drop calculations.
     - `testBooleanAndPercentageStatMath`: Verified boolean toggles and percentage scaling formulas.

2. **Compilation & Packaging**:
   - `./gradlew compileJava compileClientJava`: Succeeded.
   - `./gradlew build`: Succeeded (including `validateAccessWidener` and `verifyNoDuplicateClasses`).

3. **Dedicated Server Launch**:
   - Executed `./gradlew runServer --args="--nogui"` with live shutdown handling.
   - Output evidence:
     `[Apotheosis/Spawners] Apothic Spawners module initialized successfully`
     `Loaded 1904 advancements`
     `Done (0.198s)! For help, type "help"`
     `Stopping the server... Saving worlds... Saving chunks`
   - Clean shutdown with exit code 0; zero errors, zero unbound registry keys, zero client class leakage.

---

## 4. Next Concrete Implementation Task

Proceed to **Milestone 8: Apothic Adventure Core & Affixes/Gems**:
1. Implement the Affix data system, Affix types, categories, and rarity definitions.
2. Implement Gem data system, gem definitions, socketing mechanics, and gem cutting/upgrading.
3. Implement forging, salvage, and reforging tables and menus.
4. Implement custom Adventure item data components and serialization.
5. Implement adventure item persistence adhering to `docs/SAVE_COMPATIBILITY.md`.
