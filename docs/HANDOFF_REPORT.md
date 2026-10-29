# Handoff Report — Milestone 6 Stabilization: Apothic Enchanting Audit & Hardening

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
│   │   │   │   └── spawners/
│   │   │   ├── apothic_enchanting/
│   │   │   │   ├── ApothicEnchanting.java
│   │   │   │   ├── Ench.java
│   │   │   │   ├── api/
│   │   │   │   ├── asm/EnchHooks.java
│   │   │   │   ├── blocks/
│   │   │   │   ├── enchantments/
│   │   │   │   ├── event/
│   │   │   │   ├── library/
│   │   │   │   ├── mixin/
│   │   │   │   │   ├── AnvilMenuMixin.java
│   │   │   │   │   ├── EnchantCommandMixin.java
│   │   │   │   │   ├── EnchantRandomlyFunctionMixin.java
│   │   │   │   │   ├── FishingHookMixin.java
│   │   │   │   │   └── SingleEnchantmentMixin.java
│   │   │   │   ├── payloads/
│   │   │   │   │   └── EnchantmentInfoPayload.java
│   │   │   │   ├── table/
│   │   │   │   │   ├── EnchantmentInfo.java
│   │   │   │   │   └── EnchantmentInfoLoader.java
│   │   │   │   └── util/
│   │   │   │       └── MiscUtil.java
│   │   │   └── placebo/
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       ├── apotheosis.accesswidener
│   │       ├── apothic_enchanting.mixins.json
│   │       └── data/
│   ├── client/
│   │   ├── java/dev/shadowsoffire/
│   │   │   ├── apotheosis/client/
│   │   │   │   └── ModMenuIntegration.java
│   │   │   ├── apothic_enchanting/
│   │   │   │   ├── ApothEnchClient.java
│   │   │   │   ├── library/
│   │   │   │   ├── table/
│   │   │   │   └── util/ClientMiscUtil.java
│   │   │   └── placebo/util/DrawsOnLeft.java
│   │   └── resources/
│   │       └── apothic_enchanting.client.mixins.json
│   └── test/
│       └── java/dev/shadowsoffire/
│           ├── apotheosis/
│           ├── apothic_enchanting/EnchantingTests.java
│           └── placebo/
```

---

## 2. Milestone 6 Stabilization Deliverables

1. **Toolchain Pinning**:
   - Pinned immutable `loom_version=1.17.21` across `gradle.properties` and documentation.
2. **Staged Atomic Data Loading & Merging**:
   - `EnchantmentInfoLoader` re-architected with `listResourceStacks`, datapack priority merge order, `"replace": true`, `"remove": [...]`, and entry `"remove": true`.
   - Staged in temporary map; published atomically via `INFO_MAP = Map.copyOf(staged)` only upon successful validation.
   - Synchronized to clients via `EnchantmentInfoPayload` and cleared upon disconnect.
   - Full cache invalidation via `EnchHooks.clearEnchantmentCaches()`.
3. **Coremod Replacements**:
   - `AnvilMenuMixin`: targeted `createResult` constant 40 (`ordinal = 2, require = 1`) to lift the "Too Expensive!" level cap to `Integer.MAX_VALUE`, and redirected `Enchantment.getMaxLevel()` to `EnchHooks.getMaxAnvilCombineLevel(ench)`.
   - `EnchantCommandMixin`: redirected `/enchant` to `EnchHooks.getMaxLevel(ench)`.
   - `FishingHookMixin`: redirected lure delay to `EnchHooks.getTicksCaughtDelay(hook)`.
   - `EnchantRandomlyFunctionMixin` & `SingleEnchantmentMixin`: redirected loot generation to `EnchHooks.getMaxLootLevel(ench)`.
4. **Library & Menu Safety**:
   - Registered Fabric Transfer API `ItemStorage.SIDED` for `Ench.Tiles.LIBRARY` and `Ench.Tiles.ENDER_LIBRARY`.
   - Replaced O(N) stream skip in `EnchLibraryContainer.onButtonClick` with O(1) integer raw ID lookup `reg.get(rawId)` and level-capping validations.
   - Replaced bare `destroyBlock` in `MiscUtil.breakExtraBlock` with player-breaking simulation, adventure mode & unbreakable checks, drop spawning, tool breaking events, and recursion guards (`IS_BREAKING_EXTRA`).
5. **ModMenu Integration**:
   - Added `ModMenuApi` client entrypoint `ModMenuIntegration` hooked to `ApotheosisConfigScreen::create`.
6. **Zero Client Leakage to Dedicated Server**:
   - Segregated client holder lookup in `ClientMiscUtil`.
   - Zero client UI/rendering classes imported or referenced on dedicated server.

---

## 3. Verification & Test Evidence

1. **Automated Unit Tests**:
   - `EnchantingTests.testLibraryTransactionalStorage`: Verified transactional storage `insert`, `commit`, rollback on `abort`, and nested transaction rollback.
   - `EnchantingTests.testAnvilLevel40CapRemoval`: Verified cap removal when `enableEnchanting` is true and vanilla 40 cap preservation when false.
   - `EnchantingTests.testAnvilEvents`: Verified damaged anvil iron block repair to pristine anvil (5 levels) and Prismatic Web curse removal (30 levels).
   - `EnchantingTests.testProductionEnchantmentCostScaling`: Verified production slot cost scaling.
   - `EnchantingTests.testQuantaFactorProductionAlgorithm`: Verified bidirectional quanta power variance $[0.50, 1.50]$.
   - `EnchantingTests.testLevelToPoints`, `testEternaStepLadderCalculation`, `testQuantaRangeCalculation`, `testRectificationAndStableBehavior`, `testArcanaTiers`, `testAnvilCostExperienceCalculations`, `testStepFunctionEvaluation`: Passed.
   - **Total test suite**: 38 tests executed across 11 test classes, 38 passed (`./gradlew test`).

2. **Compilation & Packaging**:
   - `./gradlew compileJava`: Succeeded.
   - `./gradlew compileClientJava`: Succeeded.
   - `./gradlew build`: Succeeded (including `validateAccessWidener` and `verifyNoDuplicateClasses`).

3. **Dedicated Server Launch**:
   - Executed `./gradlew runServer --args="--nogui"` with live shutdown handling.
   - Output evidence:
     `[Apotheosis/Enchanting] Apothic Enchanting module initialized successfully`
     `[Apotheosis/EnchantmentInfo] Loaded and published 6 enchantment info override(s)`
     `Done (0.255s)! For help, type "help"`
     `Stopping the server... Saving worlds... Saving chunks`
   - Server loaded 100% of spawn chunks, staged and published enchantment info, registered Transfer API automation, and shut down cleanly with exit code 0.

4. **Client Launch**:
   - Executed `./gradlew runClient`.
   - Verified clean client initialization with RenderPearl GUI pipeline and `Setting user: Player574`.

---

## 4. Next Concrete Implementation Task

Proceed to **Milestone 7: Apothic Spawners Core & Persistence**:
1. Implement spawner stat modifications hooking into Minecraft 26.3 `BaseSpawner` (min/max spawn delay, spawn count, max nearby entities, required player range, spawn range).
2. Implement spawner modifier items and data map definitions (sugar, clock, ghast tear, blaze rod, fermented spider eye, dragon egg, etc.).
3. Implement Silk Touch spawner capture and custom drops loot table handling.
4. Implement redstone control and condition-ignoring spawner modifiers.
5. Implement spawner persistence adhering to `docs/SAVE_COMPATIBILITY.md`.
