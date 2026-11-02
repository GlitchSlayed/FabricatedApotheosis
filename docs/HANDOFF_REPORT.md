# Handoff Report — Milestone 11: Approved Optional Integrations & Configuration Polish

- **Date**: 2026-11-01
- **Target Version**: Minecraft 26.3 Fabric (Loom 1.17.21, Loader 0.19.5, YACL 3.9.6, JEI 31.17.0.108, Jade 26.3.5+fabric)
- **Status**: Milestone 11 Complete; proceeding to Milestone 12. Do not publish releases.

---

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
├── build.gradle (with verifyNoDuplicateClasses, DuplicatesStrategy.FAIL & YACL JiJ include)
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── docs/
│   ├── ADVENTURE_INVENTORY.md
│   ├── ARCHITECTURE.md
│   ├── CLIENT_FEATURE_CHECKLIST.md
│   ├── DAMAGE_PIPELINE.md
│   ├── DECISIONS.md (Updated with ADR-019)
│   ├── DEPENDENCY_MATRIX.md (Updated with actual install set & 26.3 integration surface)
│   ├── ENCHANTING_INVENTORY.md
│   ├── FEATURE_PARITY.md (Updated with Milestone 11 parity entries)
│   ├── HANDOFF_REPORT.md (Current)
│   ├── PORTING_SURFACE.md
│   ├── REFERENCE_INDEX.md
│   ├── SAVE_COMPATIBILITY.md
│   ├── SPAWNERS_INVENTORY.md
│   ├── STATUS.md (Updated with Milestone 11 completion)
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
│   │   │   │   ├── Apoth.java
│   │   │   │   ├── AdventureConfig.java
│   │   │   │   ├── adventure/AdventureModule.java
│   │   │   │   ├── affix/
│   │   │   │   ├── attachments/
│   │   │   │   ├── commands/
│   │   │   │   ├── compat/jade/ (NEW - Jade server data providers)
│   │   │   │   │   ├── ApotheosisJadeCommon.java
│   │   │   │   │   ├── ApotheosisJadePlugin.java
│   │   │   │   │   ├── EnchantingServerDataProvider.java
│   │   │   │   │   └── SpawnerServerDataProvider.java
│   │   │   │   ├── config/ (ServerConfig, ClientConfig, ApotheosisConfig)
│   │   │   │   ├── integrations/IntegrationsModule.java
│   │   │   │   ├── item/PotionCharmItem.java
│   │   │   │   ├── mobs/
│   │   │   │   ├── socket/
│   │   │   │   └── util/
│   │   │   ├── apothic_attributes/
│   │   │   ├── apothic_enchanting/
│   │   │   ├── apothic_spawners/
│   │   │   └── placebo/
│   │   └── resources/
│   │       ├── fabric.mod.json (with jei_mod_plugin & jade entrypoints)
│   │       └── assets/
│   ├── client/
│   │   └── java/dev/shadowsoffire/
│   │       ├── apotheosis/client/
│   │       │   ├── ApotheosisClient.java
│   │       │   ├── ModMenuIntegration.java
│   │       │   ├── compat/jade/ (NEW - Jade client tooltip providers)
│   │       │   │   ├── ApotheosisJadeClient.java
│   │       │   │   ├── EnchantingClientProvider.java
│   │       │   │   └── SpawnerClientProvider.java
│   │       │   ├── compat/jei/ (NEW - JEI recipe viewer & transfer)
│   │       │   │   ├── ApotheosisJeiPlugin.java
│   │       │   │   ├── GemCuttingCategory.java
│   │       │   │   ├── InfusionRecipeCategory.java
│   │       │   │   ├── RavenTransferHandler.java
│   │       │   │   ├── SalvagingCategory.java
│   │       │   │   └── SpawnerCategory.java
│   │       │   └── config/ApotheosisConfigScreen.java
│   │       ├── apothic_attributes/client/
│   │       └── apothic_enchanting/
│   └── test/java/dev/shadowsoffire/ (62 automated unit tests)
```

---

## 2. Milestone 11 Accomplishments & Key Implementations

### 2.1 Recipe Viewer Support (JEI 31.17.0.108)
- **Custom Recipe Categories**:
  - `SpawnerCategory`: previews monster spawner stat modifiers, quartz offhand inversions, bounds, and stat names.
  - `SalvagingCategory`: displays input materials and probable output ranges using custom JEI output count widgets.
  - `GemCuttingCategory`: supports `BasicGemCuttingRecipe` and dynamic `PurityUpgradeRecipe` previews for all purity tiers.
  - `InfusionRecipeCategory`: renders enchanting table infusion recipes with interactive Eterna, Quanta, and Arcana requirement bars.
- **Workstation Catalysts**:
  - Registered catalysts for Spawners (`Blocks.SPAWNER`), Salvaging Table (`SALVAGING_TABLE`), Gem Cutting Table (`GEM_CUTTING_TABLE`), and Enchanting Tables (`ENCHANTING_TABLE`, `RAVEN_ENCHANTING_TABLE`).
- **Ingredient Information Pages**:
  - Registered in-game ingredient info pages for Monster Spawners (explaining silk touch and durability costs), Gem Dust, Sigils (Socketing, Enhancement, Unnaming, Withdrawal), Enchantment Library, and Enchanting Tables.
- **Recipe Transfer Handler (`RavenTransferHandler`)**:
  - Implemented `IRecipeTransferHandler<RavenEnchantmentMenu, InfusionRecipe>`: transfers input stacks into slot 0 and dispatches `SetRavenStatsPayload` to the server.
  - Server validates and clamps slider stats against surrounding bookcase limits before applying (`SetRavenStatsPayload.handleServer`), preventing any client-side stat spoofing.
- **GUI Exclusion Areas & Click Areas**:
  - Registered exclusion zones for `InventoryScreen` (AttributesGui sheet and sword toggle button) and `ApothEnchantmentScreen` (upper info tab).
  - Registered recipe click areas for Infusion recipes on the Enchanting Table info tab and Salvaging recipes on the Salvaging Table hammer icon.

### 2.2 HUD Inspection Integration (Jade 26.3.5+fabric)
- **Common Server Data Providers (`dev.shadowsoffire.apotheosis.compat.jade`)**:
  - `SpawnerServerDataProvider`: serializes `ApothSpawnerTile` custom and vanilla stat parameters via `saveCustomOnly` into network compound tags.
  - `EnchantingServerDataProvider`: serializes bookshelf stats (Eterna, Quanta, Arcana, Clues, Stability, Treasure) and blacklist filters for `FilteringShelfBlock`.
- **Client Tooltip Providers (`dev.shadowsoffire.apotheosis.client.compat.jade`)**:
  - `SpawnerClientProvider`: formats spawner stat modifications dynamically via `SpawnerStat.getTooltip()`.
  - `EnchantingClientProvider`: displays bookshelf contributions and filtering shelf enchantment blacklists via `TooltipUtil.appendBlockStats`.
- **Dedicated-Server Isolation**:
  - Implemented dynamic reflection delegation in `ApotheosisJadePlugin.registerClient` checking `FabricLoader.getEnvironmentType() == CLIENT` before referencing client classes, ensuring 100% safety on dedicated servers.

### 2.3 Accessory / Equipment Slot Integration & Safety
- **Dependency Surface on Fabric 26.3**:
  - Audited ecosystem: Trinkets and Accessories have 0 published releases for Fabric 26.3. Both are reported as deferred without inventing pseudo-APIs.
- **Decoupled Extension Point**:
  - Maintained `ModifierSourceType` as an open registry, isolating accessory slot modifiers and preventing duplicate attribute application when items are visible through multiple paths.
- **Potion Charm Fallback**:
  - Updated `PotionCharmItem.java` (`isCharmsInCuriosOnly`): when accessory mods are absent, potion charms automatically fall back to ticking from normal player inventory slots, preventing them from becoming permanently inoperable.

### 2.4 Enchantment Description & Tooltip Cooperation
- Added `enableInlineEnchDescs` config toggle to `ClientConfig` and exposed it in the YACL configuration screen.
- Implemented automatic yielding in `ApothEnchClient`: if a dedicated enchantment description mod (`enchdesc`, `idwtialsimmofg`, or `enchantmentdescriptions`) is present, Apotheosis automatically suppresses its inline descriptions to avoid duplicate tooltip spam.

### 2.5 YACL Packaging & Installation Set
- Resolved documentation ambiguity regarding packaging: YetAnotherConfigLib (YACL `3.9.6+26.3-fabric`) is nested via Fabric Loom's Jar-in-Jar (`include`) mechanism in `build.gradle`.
- Verified built JAR structure: `META-INF/jars/yet-another-config-lib-3.9.6+26.3-fabric.jar` sits intact unshaded, preserving its manifest, `fabric.mod.json`, and LGPL-3.0 license metadata.
- End-user installation requires only:
  1. Fabric Loader `>=0.19.5`
  2. Fabric API `0.161.0+26.3`
  3. `fabricated-apotheosis-1.0.0-alpha.1.jar`

### 2.6 Configuration Audit & Remote Server Safety
- Verified all exposed options across `ServerConfig`, `ClientConfig`, and `AdventureConfig` have validation bounds, units, and clear descriptions.
- Server-authoritative options are rendered read-only (`.available(!isRemote)`) with clear yellow notice tags when connected to remote servers.
- Client disconnect listener (`ClientPlayConnectionEvents.DISCONNECT`) calls `ApotheosisConfig.clearRemoteServerConfig()`, preventing remote gameplay rules from leaking into subsequent local singleplayer sessions.

---

## 3. Verification & Test Results

1. **Automated Unit Test Suite**:
   - Executed: `./gradlew test --rerun-tasks`
   - Result: **62/62 tests passing** cleanly across all modules (Placebo, Attributes, Spawners, Adventure, Progression, Config).
2. **Compilation**:
   - Executed: `./gradlew compileJava compileClientJava`
   - Result: **BUILD SUCCESSFUL** with 0 errors across main and client source sets.
3. **Artifact Assembly & Jar-in-Jar Verification**:
   - Executed: `./gradlew build`
   - Verified via `unzip -l build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar`:
     - Contains `META-INF/jars/yet-another-config-lib-3.9.6+26.3-fabric.jar` intact.
     - Contains all JEI categories and Jade providers.
     - Contains `fabric.mod.json` with entrypoints `jei_mod_plugin` and `jade`.
4. **Scope Boundaries**:
   - Gateways to Eternity is completely excluded.
   - Patchouli / Chronicle of Shadows remains deferred TBD (no replacement book system built).

---

## 4. Next Concrete Implementation Task

Proceed to **Milestone 12**:
- Multi-player verification and dedicated-server round-trip testing.
- Save-compatibility upgrade testing with copied representative world fixtures.
- Final ecosystem packaging, documentation review, and release readiness.
- Do NOT publish releases without explicit user instruction.
