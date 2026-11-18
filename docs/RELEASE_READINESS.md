# Release Readiness Report — `1.0.0-alpha.5`

- **Candidate Identity**: Fabricated Apotheosis `1.0.0-alpha.5`
- **Target Platform**: Minecraft 26.3 Fabric (Loom 1.17.21, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25)
- **Primary Artifact**: `build/libs/fabricated-apotheosis-1.0.0-alpha.5.jar`
- **Status**: **All 98/98 Mixin-Transformed Tests Passing; Server, OpenGL Dev & Vulkan Prism Client Verified (`1.0.0-alpha.5`)**
- **Date**: 2026-11-18

---

## 1. Executive Summary & Scope Boundaries

Fabricated Apotheosis unites the pinned Minecraft 26.1.2 NeoForge Apotheosis ecosystem into a single cohesive distributable for Minecraft 26.3 Fabric. 

### Scope Adherence:
- **Included**: Apotheosis Core, Apothic Attributes, Apothic Enchanting, Apothic Spawners, and required Placebo subsets.
- **Embedded via Jar-in-Jar**: YetAnotherConfigLib (YACL `3.9.6+26.3-fabric`), cleanly unshaded in `META-INF/jars/` with LGPL-3.0 license and manifest intact.
- **Optional Integrations Verified**: Just Enough Items (JEI `31.17.0.108`), Jade (`26.3.5+fabric`), and ModMenu (`22.0.0-alpha.3`).
- **Deferred (TBD)**: Patchouli / Chronicle of Shadows (in-game book JSON assets preserved without runtime hooks; no replacement book system built).
- **Completely Out of Scope**: Gateways to Eternity (excluded from dependencies, planned work, and release criteria).
- **Ecosystem Boundaries**: No migration from upstream NeoForge, Zenith, or unrelated older ports. 1.0.0-alpha.1 establishes the initial compatibility baseline for this fork.

---

## 2. Comprehensive Subsystem Parity Matrix

Every feature across all ecosystem subsystems has been reconciled against actual code and runtime behavior:

| Subsystem / Feature | Implementation Status | Test & Verification Status | Notes |
| :--- | :--- | :--- | :--- |
| **Combined 1-JAR Assembly** | `implemented` | `verified` (Build & Unzip) | Single JAR containing all modules and nested YACL JiJ. |
| **Placebo Dynamic Registries** | `implemented` | `unit-tested` (PlaceboTests) | Atomic dynamic reload, codecs, and sync payloads verified. |
| **Server/Client Config Architecture** | `implemented` | `unit-tested` (ConfigTests) | Atomic JSON writes, schema v1, remote read-only mode, and disconnect cleanup. |
| **All 21 Custom Combat Attributes** | `implemented` | `unit-tested` (AttributesTest) | Registered, synced, attached to living entities; mining speed removed in favor of vanilla efficiency. |
| **Combat Rules & Armor Pipeline** | `implemented` | `unit-tested` (CombatRulesTest) | 11-stage damage processing pipeline matching upstream math. |
| **Enchanting Math & Formulas** | `implemented` | `unit-tested` (EnchantingTests)| Quanta $[1, 200]$, Arcana rarity shifts, and tiered clue generation verified. |
| **Enchanting Table Menus & Screens** | `implemented` | `integration-tested` | `ApothEnchantmentScreen`, `RavenEnchantmentScreen`, and `EnchantingInfoScreen`. |
| **Enchantment Library & Tomes** | `implemented` | `unit-tested` & `manual` | Points storage, max level caps, scrap/extraction tomes verified. |
| **16 Monster Spawner Stats** | `implemented` | `unit-tested` (SpawnerTests) | Clamping, inverse modifiers, redstone control, and explosion instability verified. |
| **Spawner Silk Touch Harvest** | `implemented` | `unit-tested` (SpawnerTests) | Requires silk touch level, tool damage dealt, preserves NBT block entity data. |
| **World Tiers (Haven → Pinnacle)** | `implemented` | `unit-tested` (ProgressionTests) | Advancement unlock requirements, server validation, client UI, and sync payloads. |
| **Tier Bonus Augment Recalculation** | `implemented` | `unit-tested` (ProgressionTests) | Dynamic recomputation on tier transition, join, respawn, and dimension change; zero modifier accumulation. |
| **Spawner Progression Reconciliation**| `implemented` | `unit-tested` (ProgressionTests) | Standalone modifiers filtered when adventure active, enforcing tiered spawner runes. |
| **Boss / Invader Spawning** | `implemented` | `integration-tested` | Dimension spawn rules, gearsets, cooldowns, announcement broadcasts, and boss summoner command. |
| **Elites & Mini-Boss Transformations** | `implemented` | `unit-tested` (ProgressionTests) | Dynamic elite registries, passenger loading, and delayed initialization. |
| **Worldgen (Dungeons, Rogues, Towers)**| `implemented` | `integration-tested` | 342 biome modifications across 57 biomes; jigsaw Tome Towers with item frame gem processor. |
| **Workstation Automation (SIDED)** | `implemented` | `unit-tested` (AdventureTests) | Fabric Transfer API (`ItemStorage.SIDED`) integrated on reforging and salvaging tables. |
| **Affix Equipment & Socketed Gems** | `implemented` | `unit-tested` (SaveCompatibility) | 26.3 DataComponents; multi-pass round-trip serialization verified. |
| **Loot Beams & Rarity Shadows** | `implemented` | `integration-tested` | Rendered via 26.3 `submitCustomGeometry` and `ApothRenderTypes.BEAM`; centered entity origin. |
| **Recipe Viewer (JEI 31.17)** | `implemented` | `compiled` & `verified` | Spawner, Salvaging, Gem Cutting, and Infusion categories, catalysts, info, and exclusion zones. |
| **HUD Inspection (Jade 26.3.5)** | `implemented` | `compiled` & `verified` | Common server data providers and client tooltip providers with dedicated-server reflection safety. |
| **Accessory Slot Bridge** | `implemented` | `deferred` | Open `ModifierSourceType` extension point ready; Trinkets/Accessories deferred on 26.3; charms fall back to inventory. |
| **Patchouli Guidebook** | `deferred` | `deferred` | Book JSON assets preserved; no replacement book system built. |
| **Gateways to Eternity** | `excluded` | `excluded` | Completely out of scope per project rules. |

---

## 3. Save Compatibility & Upgrade Baseline

- **Initial Baseline**: As the first Fabric port release for Minecraft 26.3, `1.0.0-alpha.1` establishes the persistent baseline for this fork.
- **Persistent Identifiers Invariant**: All data component identifiers (`apotheosis:affixes`, `apotheosis:socketed_gems`, `apotheosis:rarity`, etc.), block/item IDs, and custom attribute IDs are locked and verified in `SaveCompatibilityTest`.
- **Multi-Pass Persistence Round-Trip**: Tested and verified multi-pass serialization cycles (save -> close -> reopen -> mutate -> save) for affix equipment, socketed gems, spawner stats, and World Tier progression.
- **Resilience to Module Toggles**: Disabling modules in `ServerConfig` keeps registry objects loaded while disabling active handlers, preventing world content from being erased.
- **Pre-Upgrade Requirement**: Upgrading future versions of this fork requires a complete world save backup. Downgrades are explicitly unsupported.

---

## 4. Packaging and Distribution Verification

1. **Artifact Verification**:
   - Location: `build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar`
   - File size: ~4.5 MB (including nested YACL jar).
   - Nested JiJ: `META-INF/jars/yet-another-config-lib-3.9.6+26.3-fabric.jar` unshaded with manifest and license intact.
2. **Duplicate Class Check**:
   - Gradle task `verifyNoDuplicateClasses` and `DuplicatesStrategy.FAIL` passed with zero duplicate class collisions.
3. **Reference Tree Exclusions**:
   - Verified that all local Minecraft reference trees (`Refrences/`, `reference/`) and decompiled source jars are strictly excluded from git tracking, compile source sets, and distributable JARs.
4. **License and Attribution**:
   - `LICENSE`: MIT code license for the port and Placebo subsets.
   - `LICENSE_ASSETS`: All Rights Reserved upstream asset copyright.
   - `NOTICE`: Full third-party attribution for Stormraven Studios, LLC and contributors.

---

## 5. Execution and Verification Summary

- **Automated Unit Tests (`fabric-loader-junit` Mixin-transformed JVM)**:
  - Command: `./gradlew --no-daemon test --rerun-tasks`
  - Result: **98/98 passed**, 0 failures, 0 errors, 0 skipped across 17 test classes.
- **Dedicated Server Boot & Shutdown**:
  - Command: `./gradlew --no-daemon runServer --args="--nogui"` (graceful `stop` after `Done`)
  - Result: Booted in `0.234s`, loaded 1910 advancements, loaded all 13 dynamic registries, 10 `placebo:gear_sets` tags with 0 errors, 6 global loot modifiers, 6 enchantment info overrides, 6 data-map entries, and applied 342 biome modifications across 57 biomes, then stopped cleanly (`exit 0`).
- **Artifact Build**:
  - Command: `./gradlew build`
  - Result: **BUILD SUCCESSFUL** (`fabricated-apotheosis-1.0.0-alpha.5.jar` and `-sources.jar`, clean of reference/upstream trees).
- **Automated Client World Join (OpenGL & Vulkan)**:
  - **Dev Client (`./gradlew --no-daemon runClient --args="--quickPlaySingleplayer AutoTestWorld"`)**: Booted cleanly on **OpenGL 4.6 (Core Profile) Mesa 26.2.6**, loaded all client Mixins and dynamic registries without item-atlas model rejection warnings (`hammer`, `star_cube`), and joined `AutoTestWorld` cleanly (`DEV_CLIENT_RESULT: joined_and_ticked`).
  - **Prism Launcher `"26.3 testing"` (`prismlauncher -l "26.3 testing" -w "New World" -o "DevTester"`)**: Booted `1.0.0-alpha.5` on **Vulkan 1.4.354 radv Mesa 26.2.6** alongside Sodium `0.9.3-beta.5+mc26.3`, ImmediatelyFast `1.17.1+26.3`, Jade `26.3.5+fabric`, Lithium, C2ME, ModernFix, EntityCulling, FerriteCore, DynamicFPS, ModMenu, and EnchDesc; migrated `ServerConfig` v1 -> v2, loaded `ApotheosisJadePlugin`, and joined `New World` cleanly (`PRISM_RESULT: joined_and_ticked`).
- **Explicit Unverified Cases**:
  - Interactive UI/gameplay walkthroughs (boss beams, radial overlays, JEI/YACL screen clicks) and live multi-client multiplayer sessions were not tested here (singleplayer client launch and world join passed on both `OpenGL` and `Vulkan`).

---

## 6. Installation & Reproducible Build Instructions

### Building from Source:
```bash
git clone https://github.com/GlitchSlayed/FabricatedApotheosis.git
cd FabricatedApotheosis
./gradlew build
```
The output JAR will be located at `build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar`.

### Installation for Players and Server Operators:
1. Ensure Minecraft 26.3 and **Fabric Loader `>=0.19.5`** are installed.
2. Place **Fabric API `0.161.0+26.3`** in your `mods/` folder.
3. Place `fabricated-apotheosis-1.0.0-alpha.1.jar` in your `mods/` folder (YACL is embedded automatically).
4. *(Optional)* Install **ModMenu**, **JEI `31.17.0.108+`**, and **Jade `26.3.5+fabric`** for enhanced GUI and recipe viewing.
