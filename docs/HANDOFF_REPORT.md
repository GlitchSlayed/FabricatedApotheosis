# Handoff Report — Second-Pass Audit Hardening & System Integrity

- **Date**: 2026-11-04
- **Target Version**: Minecraft 26.3 Fabric (Loom 1.17.21, Loader 0.20.1+, YACL 3.9.6, JEI 31.17.0.108, Jade 26.3.5+fabric)
- **Primary Artifact**: `build/libs/fabricated-apotheosis-1.0.0-alpha.5.jar`
- **Status**: **Second-Pass Audit Resolved; 83/83 Tests Passing; Full Build Green.**

---

## 1. Scope & Audit Resolution Summary

Following the full static codebase audit and subsequent second-pass verification, all findings have been thoroughly resolved across 5 functional areas:

### 1. Invoker Registration & Crash Prevention
- **`LivingEntityInvoker` Registration**: Added `"LivingEntityInvoker"` to `apotheosis.mixins.json` and validated all 4 target methods (`apoth$callDropCustomDeathLoot`, `apoth$callDropEquipment`, `apoth$callOnEffectUpdated`, `apoth$callCanRide`) against Minecraft 26.3 `LivingEntity`. Added automated regression test `testLivingEntityInvokerRegistered` in `SaveCompatibilityTest`.

### 2. Projectile Dispatch Timing & Re-entry Safety
- **Firing Dispatch Timing**: Shifted `ProjectileMixin` firing dispatch to `@Inject(at = @At("TAIL"), method = "shoot(DDDFF)V")` so arrow motion/delta vectors are fully initialized before affixes inspect velocity.
- **Spectral Arrow Recursion Guard**: Pre-tagged `apoth.generated` in `SpectralShotAffix` *before* firing the secondary arrow, and added `apoth$IN_SHOOT` thread-local re-entrancy protection in `ProjectileMixin`.

### 3. Attack Source Targeting & Exception Safety
- **Attack Source Disambiguation**: Targeted `@Nullable ItemStack itemSource` (with main-hand fallback) instead of iterating every equipped item in `EnchantmentHelperMixin.apoth_doPostAttackEffects`.
- **Invulnerability Timer Safety**: Wrapped `target.invulnerableTime` modification in `try { ... } finally { target.invulnerableTime = old; }`.

### 4. Gameplay Hooks & Network Synchronization
- **`onItemUse` Dispatch**: Wired from `ItemStackMixin.useOn(UseOnContext)` to socketed gems and affixes (`StoneformingAffix`, `EnlightenedAffix`).
- **`modifyIncomingDamageTags` Dispatch**: Wired at `HEAD` of `LivingEntity.hurtServer` for `MagicalArrowAffix`.
- **`modifyEntityLoot` Dispatch**: Wired in `LivingEntityMixin` with death-drop capture via `EntityMixin.spawnAtLocation` (`FestiveAffix`, `TelepathicAffix`).
- **Dynamic Tags Network Sync**: Implemented `DynRegPayloads.Tags` payload, sent before `End` in `DynamicRegistry.syncToPlayer`, staged and bound via `bindTags` on the client in `completeClientSync`. Registered payload provider in `DynamicRegistryManager` and hooked `EnchantmentInfoLoader.syncToPlayer` to `SYNC_DATA_PACK_CONTENTS`.
- **World Tier Maximum Unlocked Tier Sync**: Added `maxUnlockedTier` to `WorldTierPayload` and synchronized across join, dimension travel, respawn, and tier advancement.

### 5. Configuration Ownership & Persistence Integrity
- **Integrated Server Config Ownership**: Added `ServerLifecycleHooks.isIntegratedServerRunning()` and guarded `ApotheosisConfig.setRemoteServerConfig` and `isRemoteServer` so singleplayer integrated servers retain authoritative local settings without remote decoupling. Wired unconsumed server settings (`maxEnchantmentLevelLimit`, `critDamageModifierCap`, `spawnerRedstoneControl`).
- **Flight Cooperative Ownership**: Added `AttributesUtil.EXTERNAL_FLIGHT_PREDICATES` and protected `PlayerMixin` flight revocation against other active flight-granting mods.
- **Structure Placement Exception Safety**: Replaced head/return injection on `placeInWorld` with `@WrapOperation` on `placeEntities` with `try/finally` in `StructureTemplateMixin`.
- **Loot Modifier Pool Guards**: Added non-empty checks before `getRandomItem` in `AffixLootModifier` and `GemLootModifier`.
- **Unknown Gem Preservation & Reconciliation**: Stored unresolved gems across saves in `GemCaseTile` and added `reconcileUnresolvedGems()` to merge them back into storage when their definitions are registered. Tested in `SaveCompatibilityTest`.

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
│   ├── DECISIONS.md (ADRs 001 - 019)
│   ├── DEPENDENCY_MATRIX.md
│   ├── ENCHANTING_INVENTORY.md
│   ├── FEATURE_PARITY.md
│   ├── FUTURE_UPGRADE_GUIDE.md (NEW - Milestone 12 Upgrade Procedures)
│   ├── HANDOFF_REPORT.md (Current)
│   ├── PORTING_SURFACE.md
│   ├── REFERENCE_INDEX.md
│   ├── RELEASE_READINESS.md (NEW - Milestone 12 Release Candidate Audit)
│   ├── SAVE_COMPATIBILITY.md
│   ├── SPAWNERS_INVENTORY.md
│   ├── STATUS.md (All 12 Milestones Complete)
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
│   │   │   │   ├── compat/jade/
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
│   │       ├── fabric.mod.json
│   │       └── assets/
│   ├── client/
│   │   └── java/dev/shadowsoffire/
│   │       ├── apotheosis/client/
│   │       │   ├── ApotheosisClient.java
│   │       │   ├── ModMenuIntegration.java
│   │       │   ├── compat/jade/
│   │       │   ├── compat/jei/
│   │       │   └── config/ApotheosisConfigScreen.java
│   │       ├── apothic_attributes/client/
│   │       └── apothic_enchanting/
│   └── test/java/dev/shadowsoffire/ (72 automated unit tests across 16 test classes)
```

---

## 2. Milestone 12 Accomplishments & Deliverables

### 2.1 Subsystem Parity Review & Audit
- Reconciled every subsystem against upstream 26.1.2:
  - Audited `SPAWNERS_INVENTORY.md`, `ENCHANTING_INVENTORY.md`, and `ADVENTURE_INVENTORY.md`.
  - Confirmed zero active stubs, no-ops, or incomplete registrations.
  - Completed exhaustive classification in `docs/RELEASE_READINESS.md` Section 2: implemented, compiled, unit-tested, integration-tested, manually verified, blocked, or deferred.

### 2.2 Save Compatibility & Persistence Invariants
- Established `1.0.0-alpha.1` as the compatibility baseline for this fork.
- Created `SaveCompatibilityTest.java` validating:
  - Persistent registry identifier stability (Data Components, Blocks, Items, Attachments, Attributes).
  - Multi-pass serialization round-trip across save/close/reopen/mutate/save cycles for affix equipment, socketed gems, spawner parameters, Raven Table stats, and World Tier progression.
  - Unknown/recoverable data tag preservation without silent resets.
  - Module toggle resilience: disabling modules keeps registry objects intact so existing world saves do not lose data upon save/load.
- Documented supported upgrade policy and pre-upgrade backup requirements; explicit reminder that downgrades are not supported.

### 2.3 Future Minecraft Upgrades Preparation
- Authored `docs/FUTURE_UPGRADE_GUIDE.md`:
  - Detailed step-by-step procedures for future Minecraft target ports (e.g. 26.4).
  - Vanilla-first evaluation rules.
  - Persistent registry identifier stability.
  - Verification workflow using the automated persistence fixture test suite and representative world saves.
  - Explicit rule: **Do not write speculative adapters or claim 26.4 compatibility in advance.**

### 2.4 Packaging & Runtime Audit
- Verified build reproducibility and dependency resolution via Gradle.
- Tested and verified Jar-in-Jar packaging: `META-INF/jars/yet-another-config-lib-3.9.6+26.3-fabric.jar` unshaded with manifest and license metadata intact.
- Verified exclusion of Minecraft reference trees (`reference/`, `Refrences/`) from artifacts and Git.
- Verified license compliance: MIT (`LICENSE`), All Rights Reserved upstream asset copyright (`LICENSE_ASSETS`), and third-party attribution (`NOTICE`).

---

## 3. Verification & Test Summary

1. **Automated Unit Test Suite**:
   - Command: `./gradlew test --rerun-tasks`
   - Result: **68/68 passed**, 0 failures, 0 skipped across all modules.
2. **Dedicated Server Boot & Shutdown**:
   - Command: `./gradlew runServer --args="--nogui"`
   - Result: Booted in 0.263s, loaded 1910 advancements, applied 342 biome modifications, prepared 100% chunks across all dimensions, and stopped cleanly with zero client classes loaded.
3. **Artifact Assembly**:
   - Command: `./gradlew build`
   - Result: **BUILD SUCCESSFUL** producing `build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar`.
4. **Scope Boundaries**:
   - Gateways to Eternity is completely excluded.
   - Patchouli / Chronicle of Shadows remains deferred TBD (no replacement book system built).

---

## 4. In-Game & Dynamic Registry Hotfixes (1.0.0-alpha.3)

### 4.1 Identified In-Game Issues & Causes
1. **Enchanting Table Placement Crash**:
   - Exception: `IllegalStateException: Invalid block entity minecraft:enchanting_table state at ... got Block{apothic_enchanting:apothic_enchanting_table}`.
   - Root Cause: Custom tables were registered as blocks but not added to `BlockEntityTypes.ENCHANTING_TABLE` valid blocks set.
   - Fix: Added `FabricBlockEntityType.addValidBlock(...)` in `ApothicEnchanting.init()`.
2. **Network Packet Decoding Failure on Chat/System Messages**:
   - Exception: `DecoderException: Failed to decode packet 'clientbound/minecraft:system_chat': Invalid color: light_blue_flash`.
   - Root Cause: `GradientColor` emitted string name not recognized in vanilla's `TextColor.NAMED_COLORS`.
   - Fix: Exposed `TextColor.NAMED_COLORS` via accesswidener, registered in constructor, and overridden `serialize()` to always produce standard `#%06X` hex.
3. **Dynamic Registry Decoding & Datapack Reload Lookup**:
   - Root Cause: During reload, server registry access is not yet updated on the active server instance, causing dynamic registries like `apotheosis:the_nether/inferno` and `placebo:gear_sets` to fail with "Can't access registry minecraft:enchantment".
   - Fix: Captured `loadResult.lookupWithUpdatedTags()` from `ReloadableServerResources` constructor into `DynamicRegistry.activeLookup` via `ReloadableServerResourcesMixin`. Hooked client lookup provider via `AdventureModuleClient.init()`.
   - Result: 99 affixes, 21 gems, 30 gear sets, 23 invaders, 4 elites, 9 rogue spawners, 27 tier augments, and 5 rarities loaded with 0 errors.
4. **Missing Content & Creative Tabs**:
   - Registered `CreativeModeTabEvents.modifyOutputEvent` for `Apoth.Tabs.ADVENTURE` and `CreativeModeTabs.FOOD_AND_DRINKS`.
   - Added mixin redirect in `CreativeModeTabsMixin` directing `Enchantment.getMaxLevel()` to `EnchHooks.getMaxLevel(ench)` for uncapped Apotheosis levels.
   - Deduplicated enchanted book tooltips via `ItemStackMixin`.
5. **World Tier UI & Interaction**:
   - Added `Ctrl` requirement when bound to `T` in `AdventureKeys.OPEN_WORLD_TIER_SELECT`.
   - Added missing localization key `"key.category.apotheosis.keys": "Apotheosis"`.
   - Fixed `WorldTier.HAVEN` default unlock state and enabled live updating in `WorldTierSelectScreen`.
   - Delegated background rendering in `WorldTierTutorialScreen` to parent screen.

### 4.2 Verification & Build Artifacts
- **Automated Tests**: 83/83 passed (`./gradlew test`).
- **Dedicated Server Boot**: Loaded in 0.205s with 0 dynamic registry errors.
- **Release Candidate JAR**: `build/libs/fabricated-apotheosis-1.0.0-alpha.5.jar`

---

## 5. Final Release Posture

The project is at **1.0.0-alpha.5 Release Candidate** with all 32 primary audit items and second-pass runtime/timing defects repaired.
**Do NOT publish releases without explicit user instruction.**
