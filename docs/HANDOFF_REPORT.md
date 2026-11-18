# Handoff Report — `1.0.0-alpha.5` Port Completion, Client & Mixin Verification

- **Date**: 2026-11-18
- **Target Version**: Minecraft 26.3 Fabric (Loom 1.17.21, Loader 0.19.5+, YACL 3.9.6, JEI 31.17.0.108, Jade 26.3.5+fabric)
- **Primary Artifact**: `build/libs/fabricated-apotheosis-1.0.0-alpha.5.jar`
- **Status**: **All 98/98 Mixin-Transformed Tests Passing; Server, OpenGL Dev & Vulkan Prism Client Verified (`1.0.0-alpha.5`).**

---

## 1. Scope & Audit Resolution Summary

Following the full static codebase audit and subsequent third-pass verification, all findings have been thoroughly resolved across all functional areas:

### 1. Critical Mob Death Crash & Marker Implementation
- **`IFestiveMarker` Implementation**: Implemented `IFestiveMarker` on `ItemStackMixin` with `@Unique boolean apoth$festiveMarked` and copy propagation via `@Inject` on `copy`.
- **Defensive Markers in `FestiveAffix`**: Added defensive `(Object) stack instanceof IFestiveMarker` checks in `markEquipment`, `modifyEntityLoot`, and `removeMarker`, completely preventing `ClassCastException` for ordinary equipped mobs (e.g. skeletons with bows) dying in all runtime and untransformed test environments.

### 2. Scoped Death-Drop Capture & Exception Safety
- **Per-Thread Context Stack**: Scoped `spawnAtLocation` capture in `EntityMixin` to a per-thread LIFO context stack (`DropCaptureContext(LivingEntity entity)`), ensuring drops are only captured when `this == ctx.entity`.
- **Safe Lifecycle Flushing**: Replaced injections in `LivingEntityMixin` with `@WrapMethod` on `dropAllDeathLoot`, wrapping drop capture in a strict `try / finally` block that flushes captured drops to the world on both success and exception recovery, preventing drop leaks or swallowing of unrelated drops.

### 3. Dynamic Registry Whole-Generation Validation & Atomic Reload
- **Whole-Generation Validation**: Added `validateGeneration(Map<Identifier, R> staged, ReloadType type)` in `DynamicRegistry` to validate entire replacement generations before any state changes are committed.
- **Atomic Coherent Reload in `RarityRegistry`**: Implemented `validateGeneration` (checks duplicate material collisions across all staged entries) and overridden `commitReload` to update `materialMap`, `sorted`, and the registry atomically together, ensuring reload failures preserve the coherent previous generation.

### 4. Tag Packet Bounds & Flight Contract
- **Bounded Tag Decoding**: Enforced `MAX_TAG_COUNT = 10,000`, `MAX_ENTRIES_PER_TAG = 10,000`, and `MAX_TOTAL_TAG_ENTRIES = 50,000` in `DynRegPayloads.Tags` with readable-bytes validation.
- **Flight Integration Contract**: Documented third-party integration contract for `AttributesUtil.EXTERNAL_FLIGHT_PREDICATES` detailing registration timing, thread safety, and interaction with flight revocation.

### 5. Menu & Table Inventory Safety
- **Gem Case Shift-Click**: Wrapped `quickMoveStack` with quick-moving tracking so `GemCaseSlot.onQuickMove` extracts the exact transferred count (`original.getCount() - remaining.getCount()`), eliminating gem loss on partial shift-clicks.
- **Salvaging Table Dirtiness**: Marked `SalvagingTableTile` dirty whenever output items are inserted, removed, or `salvageAll()` executes.
- **Gem Case Storage Views**: Delegated `StorageView.extract` to `GemCaseStorage.extract` so Fabric Transfer API extraction returns the actual extracted amount instead of 0.
- **Dynamic Gem Selection**: Converted raw `Gem` in `GemCaseMenu` to `DynamicHolder<Gem>`, updated `GemCaseScreen` to use `getSelectedGem()`, preventing stale references across datapack reloads.
- **Reforging Choices Invalidation**: Added listeners to `ReforgingTableTile` notifying menus to re-evaluate choices when another player alters table contents.
- **Gem Upgrade Capacity Limit**: Enforced `maxCount` capacity guards in `GemUpgradeMatch` and `GemCaseTile` before executing upgrades.
- **Block-Break State Capture**: Captured `BlockState` at HEAD in `ServerPlayerGameModeMixin` and passed pre-destruction state at RETURN.
- **Config Backup Move**: Enforced atomic move, standard move, and copy fallback in `ApotheosisConfig.backupInvalidFile` before overwriting malformed configs.
- **Gem-Cutting Slot Callback**: Eliminated duplicate callback in `GemCuttingMenu`.
- **Unknown Gem Overflow Safety**: Used `long` arithmetic in `GemCaseTile.reconcileUnresolvedGems()` and eliminated clamping of saved quantities.

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

## 5. Final Port Completion, Runtime Mixin & Datapack Verification (`1.0.0-alpha.5`)

### 5.1 Completed Mixins, Gameplay Hooks & Client Polish
- **Common Mixins & Duck Interfaces**: Added `DamageSourceMixin` (`DamageSourceExtension`), `AttributeInstanceMixin` (`AttributeInstanceExt`), `SmithingMenuMixin`, `AnvilBlockMixin`, `AbstractSkeletonMixin` (crossbow skeletons), `EnderDragonFightMixin`, `WitherSkullBlockMixin`, `MHFMixinLivingEntity`, `WandererSpawnerMixin`, `EnchantmentMixin` (trident Sharpness/Looting/Piercing), and `CachedObjectSource` on `ItemStackMixin`; extracted `EntityDropCapture` outside `EntityMixin` so Mixin classloading rules are strictly satisfied.
- **Gameplay & Client Wiring**: Wired `Omnetic` (`PlayerMixin`), `Radial` (`ServerPlayerGameModeMixin` + `RadialProgressTracker` outlines/crumbling), `Telepathic` block drops (`BlockMixin`), `AntiGravityArrowBonus` / `apoth.burns_in_sun` / boss `IN_WALL` (`EntityMixin`), slot eligibility & cold damage (`LivingEntityMixin`, `EnchantmentHelperMixin`), boss golem despawn (`MobMixin`), cursor socketing (`GemItem.overrideStackedOnOther`), `/apotheosis debug` weight command (`DebugWeightCommand`), `ItemStackRenderStateMixin`/`GuiGraphicsExtractorMixin`/`GuiRendererMixin`/`HudMixin` (26.3 `Hud.setOverlayMessage` override), boss beams, and item comparison tooltips (`AdventureModuleClient`).
- **Compat & Resources**: Added Jade Adventure boss provider (`AdventureServerDataProvider` / `AdventureClientProvider`), JEI `StoneformingCategory` + 8 crafting/infusion extensions, `ApothDataMapsLoader` + `LootCategoryOverridesPayload`, 173 26.3 `minecraft:brewing` recipes, missing loot tables, paintings, villager trades, lang files, and tags.

### 5.2 Runtime & Reload Bugs Caught and Resolved
1. **`RarityRegistry.commitReload` BiMap Collision**: Calling `this.holder(r)` before `super.commitReload` returned `emptyHolder()` for every rarity during server startup; switched to `this.holder(entry.getKey())` and assigned `materialMap`/`sorted` before `super.commitReload`.
2. **`DynamicTagManager` Reload Ordering**: Added explicit `ResourceLoader.addListenerOrdering(reg.getId(), DynamicTagManager.ID)` during `DynamicRegistry.registerRegistry` and `getFabricDependencies()` on `DynamicTagManager` so all 10 `placebo:gear_sets` tags resolve cleanly after `placebo:gear_sets` commits.
3. **26.3 Loot Condition & Entity SubPredicate Schema**: Updated `gem_entity_drops.json` and `gem_entity_drops_from_real_players.json` from `"condition"` + `"type_specific"` to 26.3 `"type"` + direct `"apotheosis:is_monster": {}` map key.
4. **Atomic Reload Rollback & Hardening**: Preserved previous valid registry/data states on parse or decode errors in `DynamicRegistry` and `EnchantmentInfoLoader`; enabled unconditional module init with live `enable*` runtime gating; synced `ApotheosisConfig` consumers (`negativeArmorFactor`, `knowledgeMultiplier`, `maxBookshelfRange`, `sculkShelfParticles`, `detailedSpawnerTooltips`, spawner delay limits, attribute deltas); hardened `stillValid`, `GemCaseMenu` input slot capacity/validity, `EnchLibraryTile` unresolved enchantment preservation, and `try/finally` state guards.

### 5.3 Observed Verification Results (Post-Final Edit)
- **Automated Tests (`./gradlew test --rerun-tasks`)**: Added `net.fabricmc:fabric-loader-junit:0.19.5` so tests execute under transformed Knot/Mixin classloading; **92/92 tests passed** across all 17 suites (`0 failures, 0 errors, 0 skipped`).
- **Build & Artifact Check (`./gradlew build`)**: Succeeded cleanly (`verifyNoDuplicateClasses` and `validateAccessWidener` passed; `fabricated-apotheosis-1.0.0-alpha.5.jar` and `-sources.jar` contain zero reference or upstream leakage).
- **Dedicated Server Smoke Check (`./gradlew runServer --args="--nogui"`)**: Booted Minecraft 26.3 server to `Done (0.241s)!` with 0 Mixin errors, 13/13 dynamic registries loaded (`5` rarities, `50` affix loot entries, `99` affixes, `1` augment, `4` elites, `23` invaders, `30` gear sets, `21` gems, `1` purity weights, `1` rarity override, `9` rogue spawners, `27` tier augments, `26` enchanting stats), `10` `placebo:gear_sets` tags loaded with 0 missing references, `6` enchantment info overrides, `6` global loot modifiers, `4` invader spawn rules, `2` loot category overrides, `342` biome modifications, and clean `stop` exit (`0`).
- **Automated Client World-Join Verification**:
  - **Dev Client (`./gradlew --no-daemon runClient --args="--quickPlaySingleplayer AutoTestWorld"`)**: Launched OpenGL 4.6 (`Mesa 26.2.6`), loaded all client Mixins and dynamic registries, integrated-synced client state, logged player into world (`JOINED=True`, `CRASH_SEEN=False`), and ticked world/chunks live without Mixin or render crashes.
  - **Prism Launcher (`26.3 testing`)**: Replaced `alpha.3` with `fabricated-apotheosis-1.0.0-alpha.5.jar` and launched alongside `sodium 0.9.3-beta.5+mc26.3`, `immediatelyfast 1.17.1+26.3`, `jade 26.3.5+fabric`, `lithium`, `c2me`, `ferritecore`, `entityculling`, `modmenu`, and `enchdesc`; loaded `ApotheosisJadePlugin`, joined singleplayer test world as `DevTester` (`PRISM_JOINED=True`, `CRASH_SEEN=False`), loaded terrain & entities cleanly.
- **Remaining Unverified Areas**: Interactive in-game feature walkthroughs (boss beams, radial overlays, JEI screens) and multi-client multiplayer sessions remain unverified here (`OpenGL` dev client and `Vulkan` Prism client launches both joined and ticked cleanly).

---

## 6. Upstream 26.1.2 Full-Stack Parity Audit & Schema v2 Config Overhaul (`1.0.0-alpha.5`)

### 6.1 Upstream Parity Fixes
- **Adventure Worldgen & Torches (`AdventureConfig`)**: Fixed empty `DIM_WHITELIST` bug by defaulting to `minecraft:overworld`; added `resolveTorchItem(String)` with vanilla fallback.
- **Spawner Compatibility (`ApothicSpawnersCompat`)**: Implemented `applyStats(PresetSpawnerStats, SpawnerBlockEntity)` and `CAPTURING` enchantment key support.
- **Boss Inspector Tooltip (`CommonTooltipUtil.appendBossData`)**: Formatted `Invader.RARITY_KEY` and boss modifiers via `AttributeFormatting.toComponent` in IDE inspection mode.
- **Tile Entity Models & Renderers**: Implemented spinning reforging hammer (`ReforgingTableTileRenderer`, `HAMMER_MODEL`) and floating augmenting star cube (`AugmentingTableTileRenderer`, `STAR_CUBE_MODEL`) using `ExtraModelKey<BlockStateModel>` + `SimpleUnbakedExtraModel` item-atlas unfiltered baking (`createUnfilteredBlockStateModel`), `Sheets.cutoutItemSheet()` / `Sheets.translucentItemSheet()`, and 26.3 `PoseStack.rotate` / `rotateDegrees`.
- **Bonus Attribute Stack Component & Command**: Added `ALObjects.Components.BONUS_STACK_ATTRIBUTE_MODIFIERS` integrated into `ItemStackMixin` (`forEachModifier`), and ported `/apotheosis add_bonus_modifier` (`BonusModifierCommand`).
- **Network Sync, Placebo Commands & Effect Tooltips**: Broadcast `AdventureConfig.ConfigPayload` + `ApotheosisConfigPayload` on player join and datapack reload; ported `/placebo hand`, `/placebo serialize_loot_table`, and `/placebo get_dimension_type` (`PlaceboCommand`); added `EffectsInInventoryMixin` + `AttributesClientTooltipHandler.enrichEffectTooltip` for inventory mob effect modifier tooltips.

### 6.2 Schema v2 Config Expansion (71 Options) & 6-Category YACL Overhaul
- **Server & Client Config Expansion**: Bumped `ServerConfig` and `ClientConfig` to `CURRENT_SCHEMA_VERSION = 2` with automatic v1 -> v2 migration in `ApotheosisConfig` and legacy key support in `ConfigMigration`. Expanded `ServerConfig` to **55 validated/clamped options** (Spawners instability/capturing, Enchanting anvil/trident/chainsaw, Attributes & Combat curves, Bosses & Invaders, Affixes & Augmenting costs, QoL tier/linking settings, Worldgen & Wanderer settings, and 4 module toggles) and `ClientConfig` to **16 options** (`attributesGuiAnchorPoint`, `enableEquipmentCompare`, `disableQuarkOnAffixItems`).
- **Consumer Wiring**: Wired all 14 gameplay/client consumers (`ALCombatRules`, `AttributesUtil`, `AttributeEvents`, `ApothEnchEvents`, `EnchantmentMixin`, `ChainsawTask`, `ApothSpawnerTile`, `LivingEntityMixin`, `WandererSpawnerMixin`, `AnvilBlockMixin`, `AbstractSkeletonMixin`, `EnderDragonFightMixin`, `WitherSkullBlockMixin`, `MobMixin`, `AttributesGui`) and rebroadcast config payloads on local save.
- **YACL Screen (`ApotheosisConfigScreen`)**: Rebuilt into 6 categories (`Client Preferences`, `Gameplay: Adventure & Bosses`, `Gameplay: Worldgen & Wanderer`, `Gameplay: Spawners & Enchanting`, `Gameplay: Attributes & Combat`, `Module Toggles`) exposing all 71 options with `OptionGroup`s, `ListOption<String>` editors, `EnumControllerBuilder`, `StringControllerBuilder`, and `.available(!isRemote)`.

### 6.3 Latest Post-Edit Verification Results
- **Automated Tests (`./gradlew --no-daemon test --rerun-tasks`)**: **98/98 tests passed** across 17 test classes under `fabric-loader-junit` (`0 failures, 0 errors, 0 skipped`).
- **Build & Packaging (`./gradlew build`)**: **BUILD SUCCESSFUL** producing `build/libs/fabricated-apotheosis-1.0.0-alpha.5.jar` and `-sources.jar` cleanly.
- **Dedicated Server (`./gradlew --no-daemon runServer --args="--nogui"`)**: Booted cleanly to `Done (0.234s)! For help, type "help"`, loaded all 13 dynamic registries, 1910 advancements, 10 gear-set tags, 6 enchantment info overrides, 6 global loot modifiers, 4 invader spawn rules, 2 loot category overrides, 342 biome modifications, and exited `0` on `stop`.
- **Dev Client (OpenGL 4.6 Mesa 26.2.6)**: `./gradlew --no-daemon runClient --args="--quickPlaySingleplayer AutoTestWorld"` joined and ticked (`DEV_CLIENT_RESULT: joined_and_ticked`) with zero item-atlas model warnings (`hammer`, `star_cube`) and zero Apotheosis/Mixin errors.
- **Prism Launcher `"26.3 testing"` (Vulkan 1.4.354 radv Mesa 26.2.6)**: Joined `New World` as `DevTester` (`PRISM_RESULT: joined_and_ticked`), migrated `ServerConfig` v1 -> v2, loaded `ApotheosisJadePlugin` alongside Sodium, ImmediatelyFast, Jade, Lithium, C2ME, ModernFix, EntityCulling, FerriteCore, DynamicFPS, ModMenu, and EnchDesc with zero Apotheosis/Mixin errors.

Release **`1.0.0-alpha.5`** is built, verified, and updated cleanly upon user request.
