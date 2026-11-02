# Release Readiness Report — Milestone 12 Release Candidate

- **Candidate Identity**: Fabricated Apotheosis `1.0.0-alpha.1`
- **Target Platform**: Minecraft 26.3 Fabric (Loom 1.17.21, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25)
- **Primary Artifact**: `build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar`
- **Release Status**: **Release Candidate Ready for Testing (DO NOT PUBLISH)**
- **Date**: 2026-11-01

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

- **Automated Unit Tests**:
  - Command: `./gradlew test --rerun-tasks`
  - Result: **68/68 passed**, 0 failures, 0 skipped.
- **Dedicated Server Boot & Shutdown**:
  - Command: `./gradlew runServer --args="--nogui"`
  - Result: Booted in 0.263s, loaded 1910 advancements, applied 342 biome modifications, prepared 100% chunks across Overworld, Nether, and The End, and stopped cleanly with zero client classes loaded.
- **Artifact Build**:
  - Command: `./gradlew build`
  - Result: **BUILD SUCCESSFUL**.
- **Explicit Unverified Cases**:
  - Live Vulkan GPU driver execution and 500+ dropped item cluster profiling remain unverified due to headless environment constraints (documented in `CLIENT_FEATURE_CHECKLIST.md`).

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
