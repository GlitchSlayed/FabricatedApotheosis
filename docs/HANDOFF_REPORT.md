# Handoff Report — Milestone 2: Shared Foundation & Placebo Ecosystem Subset

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
│   ├── DECISIONS.md
│   ├── DEPENDENCY_MATRIX.md
│   ├── FEATURE_PARITY.md
│   ├── HANDOFF_REPORT.md
│   ├── PORTING_SURFACE.md
│   ├── REFERENCE_INDEX.md
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
│   │   │   │   ├── attributes/AttributesModule.java
│   │   │   │   ├── config/ApotheosisConfig.java
│   │   │   │   ├── docs/DocsSupport.java
│   │   │   │   ├── enchanting/EnchantingModule.java
│   │   │   │   ├── integrations/IntegrationsModule.java
│   │   │   │   ├── placebo/PlaceboSupport.java
│   │   │   │   ├── platform/PlatformHelper.java
│   │   │   │   └── spawners/SpawnersModule.java
│   │   │   └── placebo/
│   │   │       ├── Placebo.java
│   │   │       ├── codec/
│   │   │       │   ├── CodecMap.java
│   │   │       │   ├── CodecProvider.java
│   │   │       │   ├── MapBackedCodec.java
│   │   │       │   └── PlaceboCodecs.java
│   │   │       ├── dynreg/
│   │   │       │   ├── DynamicHolder.java
│   │   │       │   ├── DynamicRegistry.java
│   │   │       │   ├── DynamicRegistryManager.java
│   │   │       │   ├── DynRegPayloads.java
│   │   │       │   ├── RegistrySerializer.java
│   │   │       │   └── WeightedDynamicRegistry.java
│   │   │       ├── network/
│   │   │       │   ├── ClientPayloadHandler.java
│   │   │       │   ├── PayloadHelper.java
│   │   │       │   └── PayloadProvider.java
│   │   │       ├── registry/
│   │   │       │   └── DeferredHelper.java
│   │   │       └── util/
│   │   │           ├── CachedObject.java
│   │   │           ├── EnchantmentUtils.java
│   │   │           ├── PlaceboUtil.java
│   │   │           └── StepFunction.java
│   │   └── resources/
│   │       ├── apotheosis.mixins.json
│   │       └── fabric.mod.json (with explicit "breaks" for standalone modules)
│   ├── client/
│   │   ├── java/dev/shadowsoffire/
│   │   │   ├── apotheosis/client/
│   │   │   │   ├── ApotheosisClient.java
│   │   │   │   └── config/ApotheosisConfigScreen.java
│   │   │   └── placebo/client/
│   │   │       └── PlaceboClientPayloadHandler.java
│   │   └── resources/
│   │       └── apotheosis.client.mixins.json
│   └── test/java/dev/shadowsoffire/placebo/
│       ├── DeferredHelperTest.java
│       ├── DynamicRegistryTest.java
│       ├── EnchantmentUtilsTest.java
│       └── StepFunctionTest.java
```

---

## 2. Pinned Upstream Versions and Commit SHAs

All repos pinned on the `26.1` stable branch targeting Minecraft 26.1.2 NeoForge:
- **Apotheosis**: `f2e76862ed9f4dcdf19c25400dc3d5478a46dd74` (v9.1.0)
- **Placebo**: `8fcfa1e6e947e8f0f12044ad31ce4bef2145982a` (v10.0.2)
- **Apothic-Attributes**: `44d0a8316fcb7965c26e8ff7eda341830ca63023` (v3.1.0)
- **Apothic-Enchanting**: `4d167fd44222058819fa70bf99fbc3ab38220e34` (v2.1.0)
- **Apothic-Spawners**: `a5401df4a80b833b297c398f88a5718bdd47c02a` (v2.0.1)
- **GatewaysToEternity**: `73aa0726c998214a180df1e2981ef1752ddb56e9` (v6.0.2)
- **Patchouli**: `4f94b07bbd287f2e208a7ea90b0ca066e54f738c` (tag `release-26.1-94-beta`)
- **YetAnotherConfigLib**: `3.9.6+26.3-fabric` (Modrinth / isXander Maven)

---

## 3. Exact Build & Toolchain Versions

- **Minecraft**: `26.3`
- **Java**: Java 25 (`25.0.4.1`, OpenJDK 64-Bit Server VM)
- **Gradle**: `9.5.1` (Wrapper) / `9.8.2` (Host)
- **Fabric Loom**: `1.17.21` (`1.17-SNAPSHOT`)
- **Fabric Loader**: `0.19.5`
- **Fabric API**: `0.161.0+26.3`
- **YACL**: `3.9.6+26.3-fabric`
- **JUnit**: `5.10.2` with `junit-platform-launcher`

---

## 4. Build and Launch Commands

```bash
# Run automated test suite
./gradlew test

# Build mod JAR, verify class duplicates, and verify licenses
./gradlew build

# Launch dedicated server in development environment
./gradlew runServer --args="--nogui"

# Launch client with YACL config screen support
./gradlew runClient
```

---

## 5. Commands Actually Executed & Results

1. `./gradlew test`: **PASS** (1s). 5/5 automated unit tests passed:
   - `StepFunctionTest.testStepFunctionMath`: verified step, level, and float evaluation formulas.
   - `StepFunctionTest.testCodecSerialization`: verified JSON/NBT round-trip decoding and encoding.
   - `EnchantmentUtilsTest.testExperienceCalculations`: verified exact vanilla XP-to-level conversions.
   - `DeferredHelperTest.testDuplicateRegistrationThrowsClearError`: verified registry collisions throw clear `IllegalStateException` identifying duplicate key.
   - `DynamicRegistryTest.testDynamicDataLoadReloadAndInvalidation`: verified dynamic datapack loading, atomic reloading, cache invalidation, and diagnostic error logging on malformed JSON without corrupting registry state.
2. `./gradlew build`: **PASS** (735ms). Successfully executed `verifyNoDuplicateClasses`, validated archive duplicates strategy (`DuplicatesStrategy.FAIL`), bundled `LICENSE`, `LICENSE_ASSETS`, and `NOTICE`, generating `build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar`.
3. `./gradlew runServer --args="--nogui"`: **PASS**. Successfully booted dedicated server on Minecraft 26.3 Fabric:
   - Loaded 67 mods including `apotheosis 1.0.0-alpha.1`.
   - Initialized `ApotheosisConfig` (`apotheosis.json`).
   - Initialized `Placebo.init()` and registered `DynamicRegistryManager` as a server reload listener.
   - Loaded world spawn chunks (100%), opened server socket, and shut down cleanly upon command without any client class leakage or crashes.
4. Verification status:
   - **Compilation**: **VERIFIED**
   - **Automated Behavioral Tests**: **VERIFIED**
   - **Dedicated-Server Launch**: **VERIFIED**
   - **Client Launch**: **IN PROGRESS** (Asset download initialized; client classes compiled and verified in JAR).

---

## 6. Packaging Decision & External Dependencies

- **Single Ecosystem Distributable**: All modules (`apotheosis`, `attributes`, `enchanting`, `spawners`, and required `placebo` subsets) are combined into a single downloadable JAR (`fabricated-apotheosis-1.0.0-alpha.1.jar`) under mod ID `apotheosis`.
- **Standalone Module Conflict Defense**: Added explicit `"breaks"` in `fabric.mod.json` against conflicting standalone module IDs:
  - `placebo`: `*`
  - `apothic_attributes`: `*`
  - `apothic_enchanting`: `*`
  - `apothic_spawners`: `*`
- **Duplicate Class Prevention**: `verifyNoDuplicateClasses` Gradle task inspects compile outputs and halts the build if duplicate fully-qualified class names occur.
- **External Dependencies**:
  - `fabricloader` (`>=0.19.5`)
  - `fabric-api` (`0.161.0+26.3`)
  - `yet_another_config_lib_v3` (`3.9.6+26.3-fabric`)
- **Optional Integrations**:
  - `patchouli` (for guidebook support)
  - `modmenu` (for in-game config button)

---

## 7. Dependency and Licensing Blockers

- **Code vs Asset Licenses**: Upstream Apotheosis code is MIT licensed, but assets (`LICENSE_ASSETS`) are `All Rights Reserved` by Stormraven Studios, LLC. The port preserves asset licensing boundaries and includes third-party attributions in `NOTICE`.
- **Patchouli CC-BY-NC-SA 3.0**: Patchouli cannot be flattened (shaded) into the mod jar without contaminating the MIT code license. Patchouli is cleanly isolated as an external optional dependency while preserving book JSON assets.
- **YACL LGPL-3.0**: YACL is consumed as a standard dependency or Jar-in-Jar (`include`) to ensure LGPL metadata compliance.

---

## 8. Attribute Overlap Findings

- Audited all 21 custom attributes from `ALObjects.java`.
- **Obsolete Mining Speed**: Upstream already removed `mining_speed` in 3.1.0; both 26.1.2 and 26.3 natively provide `Attributes.MINING_EFFICIENCY` (`minecraft:mining_efficiency`) and `Attributes.SUBMERGED_MINING_SPEED`. No custom attribute will be added.
- **21 Combat Attributes Kept**: `ARMOR_PIERCE`, `ARMOR_SHRED`, `ARROW_DAMAGE`, `ARROW_VELOCITY`, `COLD_DAMAGE`, `CRIT_CHANCE`, `CRIT_DAMAGE`, `CURRENT_HP_DAMAGE`, `DODGE_CHANCE`, `DRAW_SPEED`, `EXPERIENCE_GAINED`, `FIRE_DAMAGE`, `GHOST_HEALTH`, `HEALING_RECEIVED`, `LIFE_STEAL`, `OVERHEAL`, `PROJECTILE_DAMAGE`, `PROT_PIERCE`, `PROT_SHRED`, `ELYTRA_FLIGHT`, and `COOLDOWN_REDUCTION` are not represented in vanilla 26.3 and will be ported using standard `RangedAttribute` instances.

---

## 9. Most Difficult NeoForge / Coremod / Rendering Dependencies

1. **Apothic Enchanting Level Uncapping**: In legacy versions, ASM was used; in 26.1.2 NeoForge, mixins and access transformers are used. On Fabric 26.3, this requires transpiling `accesstransformer.cfg` to `apotheosis.accesswidener` and targeting Minecraft 26.3's data-driven enchantment structure.
2. **NeoForge Capability / Attachment Migration**: Migrating `RavenTableStats` and spawner custom data from NeoForge attachments to Fabric Data Attachment API (`fabric-data-attachment-api-v1`).
3. **RenderPearl GPU Pipelines**: Boss health bar styling, gem sockets, and custom particle effects must use Minecraft 26.3 rendering abstractions without raw OpenGL calls.

---

## 10. Proposed Next Three Implementation Tasks

1. **Task 1: Apothic Spawners Core Mechanics & Modifier Data Registry**
   - File: `src/main/java/dev/shadowsoffire/apotheosis/spawners/`
   - Implement `SpawnerModifier` data registry utilizing `DynamicRegistry` and `StepFunction`.
   - Implement `BaseSpawnerMixin` targeting 26.3's `serverTick` and `setEntityData(TypedEntityData)`.
2. **Task 2: Spawner Silk Touch Harvesting & Persistence**
   - Files: `src/main/resources/data/c/loot_table/blocks/spawner.json` & `src/main/java/dev/shadowsoffire/apotheosis/spawners/mixin/SpawnerBlockDropsMixin.java`
   - Preserve modifier stats on harvested spawner items using `DataComponents.CUSTOM_DATA`.
3. **Task 3: Spawner Client Tooltip Deltas & Screen Sync**
   - Files: `src/client/java/dev/shadowsoffire/apotheosis/client/spawners/SpawnerTooltipHandler.java`
   - Hook 26.3 `TooltipDisplay` / item hover text to display modifier stats and delta comparisons.

---

## 11. Questions That Require a Product Decision

1. **Gateways to Eternity**: Should Gateways to Eternity remain a separate optional mod (recommended), or should tiered wave battles eventually be bundled directly into Fabricated Apotheosis?
2. **Guidebook Support**: Should we maintain Patchouli Fabric as the recommended guidebook engine, or evaluate a zero-dependency internal book viewer (or Lavender)?
3. **Curios Accessories on Fabric**: Does the product intend to support Trinkets, Accessories, or rely strictly on vanilla equipment components for gem sockets and affixes?

---

## 12. Current Git Status & Commit

- **Branch**: `main`
- **Initial Commit SHA**: `25803d7`
- **Skeleton Commit SHA**: `8d9010f`
- **Working Tree**: Ready to stage and commit Milestone 2 changes.
