# Project Status & Roadmap

## 1. Project Milestones

### Milestone 1: Discovery, Architecture & Working Skeleton
- **Status**: **COMPLETE** (2026-10-27)
- **Key Achievements**:
  - [x] Cloned and verified upstream 26.1.2 source repositories (`Apotheosis`, `Placebo`, `Apothic-Attributes`, `Apothic-Enchanting`, `Apothic-Spawners`, `GatewaysToEternity`, `Patchouli`).
  - [x] Created full documentation suite: `UPSTREAM_LOCK`, `DEPENDENCY_MATRIX`, `PORTING_SURFACE`, `VANILLA_OVERLAP`, `ARCHITECTURE`, `DECISIONS`, `TEST_PLAN`.
  - [x] Initialized reproducible Gradle build targeting Minecraft 26.3, Fabric Loader 0.19.5, Loom 1.17.21, and YACL 3.9.6.
  - [x] Verified split-source skeleton (`main` and `client`) with clean module boundaries.

### Milestone 2: Shared Foundation & Placebo Ecosystem Subset
- **Status**: **COMPLETE** (2026-10-27)
- **Key Achievements**:
  - [x] Single-JAR packaging finalized: duplicate class defense task (`verifyNoDuplicateClasses`), `DuplicatesStrategy.FAIL` on all archives, and explicit `"breaks"` in `fabric.mod.json` against conflicting standalone module IDs (`placebo`, `apothic_attributes`, `apothic_enchanting`, `apothic_spawners`).
  - [x] Bundled license notices: MIT (`LICENSE`), All Rights Reserved (`LICENSE_ASSETS`), and third-party attribution (`NOTICE`).
  - [x] Ported required Placebo registration helper (`DeferredHelper`) with strict duplicate key prevention.
  - [x] Ported codecs and serialization helpers: `StepFunction`, `PlaceboCodecs`, `MapBackedCodec`, and `CodecMap`.
  - [x] Ported dynamic data loading infrastructure: `DynamicRegistry`, `WeightedDynamicRegistry`, and reload-safe `DynamicHolder`.
  - [x] Implemented atomic reload coordination: `DynamicRegistryManager` registered as server resource listener with sync payloads (`DynRegPayloads`) and cache invalidation.
  - [x] Implemented dedicated-server safe networking: `PayloadHelper` in `main` with interface-segregated `PlaceboClientPayloadHandler` in `client`.
  - [x] Ported utilities: `CachedObject` item caching and `EnchantmentUtils` vanilla XP conversion math.
  - [x] Verified with 5 automated unit tests (`./gradlew test`) covering math, codecs, duplicate registration rejection, atomic dynamic loading, invalid-data diagnostics, and cache invalidation.
  - [x] Verified aggregate build (`./gradlew build`) and dedicated server launch (`./gradlew runServer --args="--nogui"`) with clean startup and shutdown.

### Milestone 3: Configuration Architecture & YACL UI
- **Status**: **COMPLETE** (2026-10-27)
- **Key Achievements**:
  - [x] Separated configuration into `ServerConfig` (`apotheosis.json`) and `ClientConfig` (`apotheosis-client.json`).
  - [x] Implemented atomic file writes with sibling `.tmp` files and atomic replacement.
  - [x] Implemented schema versioning (`schemaVersion = 1`) and migration for v0 unversioned JSON and legacy `.cfg` files (`ConfigMigration`).
  - [x] Implemented server-to-client network synchronization (`ApotheosisConfigPayload`) via Fabric Networking API v1 on player join.
  - [x] Implemented automatic clearance of remote server settings upon client disconnect (`ClientPlayConnectionEvents.DISCONNECT`) to prevent retaining stale state.
  - [x] Implemented server validation: clamping bounds, constraint checking, and diagnostic warning logs on malformed/invalid inputs.
  - [x] Built complete YACL UI (`ApotheosisConfigScreen`): categorized into Client Preferences, Spawners, Enchanting, Attributes, and Module Toggles.
  - [x] Enforced read-only display of server gameplay options (`.available(false)`) when connected to remote servers.
  - [x] Tagged every option with runtime impact: `LIVE`, `RELOAD_REQUIRED`, or `RESTART_REQUIRED` (`OptionFlag.GAME_RESTART`).
  - [x] Verified with 14 automated unit tests (`./gradlew test`) and verified dedicated server launch without client UI classes.

### Milestone 4: Custom Attributes, Vanilla Overlap & Attribute UI
- **Status**: **COMPLETE** (2026-10-27)
- **Key Achievements**:
  - [x] Finalized `docs/VANILLA_OVERLAP.md`: Audited all 21 custom attributes individually with ranges, default values, units, synchronization, and modifier operations.
  - [x] Resolved obsolete `apothic_attributes:mining_speed`: confirmed removed upstream, mapped to vanilla `minecraft:mining_efficiency`, and implemented automatic migration in `AttributeHelper.migrateItemModifiers`.
  - [x] Registered all 21 custom attributes via `DeferredHelper` under `apothic_attributes` with `setSyncable(true)`.
  - [x] Attached all 21 attributes to all living entities via `FabricDefaultAttributeRegistry.MODIFY` (`context.modifyAll`).
  - [x] Implemented Fabric attribute formatting (`AttributeFormatting`) supporting `PERCENTAGE`, `BOOLEAN`, and `FLAT` attributes without NeoForge dependencies.
  - [x] Implemented attribute inspection UI (`AttributesGui`): sword toggle button on `InventoryScreen`, scrollable list, formula display, RenderPearl 26.3 GUI pipeline rendering (`GuiGraphicsExtractor`), and recipe viewer exclusion zones.
  - [x] Implemented modifier provenance display (`ModifierSource`, `ItemModifierSource`, `EffectModifierSource`, `AttributeModifierComponent`) displaying equipment slot items and mob effect sprites via `Hud.getMobEffectSprite`.
  - [x] Implemented hidden attribute filtering (`ClientConfig.isAttributeHidden`) supporting exact IDs, namespace wildcards (`apothic_attributes:*`), and negation entries (`!`).
  - [x] Implemented item tooltip integration (`AttributesClientTooltipHandler`) via `ItemTooltipCallback`.
  - [x] Provided open extension points for accessory slots (`ModifierSourceType.register()`).
  - [x] Verified with 20 automated unit tests (`./gradlew test`) covering registration, bounds, synchronization, formatting, accumulation prevention, and migration.
  - [x] Verified aggregate build (`./gradlew build`) and dedicated server launch (`./gradlew runServer --args="--nogui"`) with clean startup and shutdown.

### Milestone 5: Apothic Attributes Combat Pipeline & Gameplay
- **Status**: **COMPLETE** (2026-10-27)
- **Key Achievements**:
  - [x] Documented the authoritative 11-stage damage processing pipeline (`docs/DAMAGE_PIPELINE.md`) mapping every NeoForge event and priority to targeted Fabric mixins and hooks.
  - [x] Implemented custom combat rules (`ALCombatRules`): A-value calculation, toughness bypass resistance ($\min(0.02 \times \text{toughness}, 0.60)$), negative effective armor amplification ($0.015 \times (-\text{armor}) \times \text{damage}$), armor pierce/shred, protection pierce/shred, and protection cap (80%).
  - [x] Implemented critical strike mechanics: tiered multicrit degradation loop ($0.85\times$ per tier above 100%), vanilla jump critical upgrade ($\max(1.5\text{F}, \text{critDamage})$), and S2C crit particle network packet (`CritParticlePayload`).
  - [x] Implemented auxiliary damage pipeline (`AuxDmgTracker`, `AttributeEvents`): non-reentrant execution guard (`noRecurse`), invulnerability frames and last hurt preservation, sweeping attack ratio scaling, and post-hit fire/cold callbacks.
  - [x] Implemented dodge mechanics: deterministic per-tick hash dodge roll (`AttributeEvents.checkDodge`), melee reach bounds check, projectile impact dodge (`AbstractArrowMixin`), and dodge sound event.
  - [x] Implemented life steal and overheal: post-damage health capture and healing, overheal absorption cap ($50\%$ max HP) without destroying external absorption (`AttributesUtil`), and ghost health restoration.
  - [x] Implemented healing received scaling in `heal()` with cancellation when $\le 0$.
  - [x] Implemented projectile and arrow behavior: first-tick base damage and velocity scaling with loop prevention tag (`apothic_attributes.arrow.done`), and thrown trident quad-scaling (`ThrownTridentMixin`, `AbstractArrowAccessor`).
  - [x] Implemented draw speed item use-duration acceleration (`LivingEntityMixin.updateUsingItem`).
  - [x] Implemented shared ability cooldowns and cooldown reduction (`AbilityCooldowns`, `CooldownTracker`).
  - [x] Implemented flight mechanics: `canGlide` / `canGlideUsing` for `ELYTRA_FLIGHT`, and safe creative flight grant tracking (`had_flight_granted`) preserving external/creative states.
  - [x] Implemented experience scaling: mob experience in `getExperienceReward` and block experience in `popExperience` (`AttributesUtil.CURRENT_BREAKER`).
  - [x] Implemented 7 Mob Effects: `BleedingEffect`, `DetonationEffect`, `FlyingEffect`, `GrievousEffect`, `KnowledgeEffect`, `SunderingEffect`, `VitalityEffect`.
  - [x] Added datapack definitions: 5 damage types (`bleeding`, `detonation`, `current_hp_damage`, `fire_damage`, `cold_damage`) and 5 damage type tags (`cannot_critically_strike`, `is_non_physical`, `#c:is_physical`, `#c:is_magic`, `#minecraft:bypasses_armor`).
  - [x] Verified with 23 automated unit tests (`./gradlew test`) covering math formulas, negative armor, multicrit degradation, CDR, cooldown pruning, dodge determinism, and recursion safety.
  - [x] Verified dedicated server launch (`./gradlew runServer --args="--nogui"`) with 100% chunks loaded, clean datapack loading, zero client leakage, and clean shutdown.

### Milestone 6: Apothic Enchanting Baseline & Anvil Overhaul
- **Status**: **COMPLETE** (2026-10-27)
- **Key Achievements**:
  - [x] Audited full feature inventory and NeoForge coremods documented in `docs/ENCHANTING_INVENTORY.md` (30 blocks, 3 block entities, 46 items, 20 enchantments, 14 enchantment effect components, menus, recipes).
  - [x] Ported Bookshelf statistics engine (`EnchantmentTableStats`, `EnchantingStatRegistry`, `TypedShelfBlock`, `SculkShelfBlock`, `FilteringShelfBlock`, `GeodeShelfBlock`, `TreasureShelfBlock`): Eterna, Quanta, Arcana, Rectification, Clues, and Filtering.
  - [x] Ported Enchanting Selection and Level Calculations (`ApothEnchantmentMenu`, `RavenEnchantmentMenu`, `ApothEnchantmentHelper`): full formula parity for Quanta power bounds $[1, 200]$, Arcana rarity weight shifts, and tiered clue generation.
  - [x] Ported Infusion and Keep-NBT Infusion recipes (`InfusionRecipe`, `KeepNBTInfusionRecipe`, `InfusionRecipeCache`).
  - [x] Ported Enchantment Library subsystem (`EnchLibraryBlock`, `EnchLibraryTile`, `BasicLibraryTile`, `EnderLibraryTile`, `EnchLibraryContainer`): Fabric Transfer API (`Storage<ItemVariant>`), transactional insertion with `SnapshotParticipant`, $2^{N-1}$ points conversion math, and full item safety across reload/disconnect/break.
  - [x] Ported Anvil Overhaul (`AnvilMenuMixin`, `AnvilEvents`, `TomeItem`): level 40 limit removed, optimal experience progression curve, Prismatic Web curse removal, and Tome combining limits.
  - [x] Ported 20 Custom Enchantments and 14 Data Component Effects to Minecraft 26.3 vanilla data-driven systems (`EndlessQuiverEffect`, `MinerFervorEffect`, `ReboundingEffect`, `ShieldBashEffect`, `SpearfishingEffect`, `StableFootingEffect`, `ScavengerLootModifier`, `WardenLootModifier`, etc.).
  - [x] Ported Client Screens (`ApothEnchantmentScreen`, `RavenEnchantmentScreen`, `EnchLibraryScreen`, `EnchantingInfoScreen`, `DrawsOnLeft`): RenderPearl GUI pipeline rendering, zero client classes on dedicated server.
  - [x] Fabric custom ingredient integration (`DefaultCustomIngredients.components`) for potions in shaped and infusion recipes.
  - [x] Verified with 37 automated unit tests (`./gradlew test`) covering Eterna step-ladder, Quanta power range, stable rectification, Arcana thresholds, Library points math, and XP progression.
  - [x] Verified dedicated server launch (`./gradlew runServer --args="--nogui"`) with clean datapack initialization, zero unbound registry keys, and clean shutdown.

### Milestone 6 Stabilization: Audit, Coremod Replacements & Persistence Verification
- **Status**: **COMPLETE** (2026-10-28)
- **Key Achievements**:
  - [x] Pinned immutable Loom release `loom_version=1.17.21` across `gradle.properties` and documentation.
  - [x] Re-architected `EnchantmentInfoLoader` with staged atomic validation, `listResourceStacks` priority merge semantics, `"replace": true`, `"remove": [...]`, atomic publication (`INFO_MAP = Map.copyOf(staged)`), cache invalidation, network sync (`EnchantmentInfoPayload`), and disconnect reset.
  - [x] Implemented robust holder lookup in `MiscUtil.findHolder` with server access bridge (`CURRENT_SERVER.registries().compositeAccess()`) and client bridge (`ClientMiscUtil`).
  - [x] Replaced all NeoForge Coremods / bytecode ASM transformers with targeted Fabric Mixins:
    - `AnvilMenuMixin`: targeted `createResult` constant 40 (`ordinal = 2`, `require = 1`) to lift the level 40 cap to `Integer.MAX_VALUE`, and redirected `getMaxLevel()` to `EnchHooks.getMaxAnvilCombineLevel(ench)`.
    - `EnchantCommandMixin`: redirected `/enchant` to `EnchHooks.getMaxLevel(ench)`.
    - `FishingHookMixin`: redirected lure delay to `EnchHooks.getTicksCaughtDelay(hook)`.
    - `EnchantRandomlyFunctionMixin` and `SingleEnchantmentMixin`: redirected loot generation to `EnchHooks.getMaxLootLevel(ench)`.
  - [x] Registered Fabric Transfer API automation (`ItemStorage.SIDED.registerForBlockEntity`) for `Ench.Tiles.LIBRARY` and `Ench.Tiles.ENDER_LIBRARY`.
  - [x] Optimized `EnchLibraryContainer.onButtonClick` to use O(1) integer raw ID lookup `reg.get(rawId)` with validation checks against menu state and level limits.
  - [x] Fixed `MiscUtil.breakExtraBlock` with player-breaking simulation, adventure mode & unbreakable checks, drop spawning, tool breaking events, and recursion guards (`IS_BREAKING_EXTRA`).
  - [x] Implemented optional `ModMenuApi` entrypoint (`ModMenuIntegration`) hooked to `ApotheosisConfigScreen::create`.
  - [x] Verified with 38 automated unit tests (`./gradlew test`) covering mathematical models, transactional storage commit/abort, anvil overrides, and data loading.
  - [x] Verified full aggregate build (`./gradlew build`), dedicated server run (`./gradlew runServer`), and client launch (`./gradlew runClient`) with clean startup, zero errors, and clean shutdown.

### Milestone 7: Apothic Spawners Core, Persistence & Instability
- **Status**: **COMPLETE** (2026-10-28)
- **Key Achievements**:
  - [x] Audited full feature inventory documented in `docs/SPAWNERS_INVENTORY.md` covering 16 stats, 32 modifier recipes, instability mechanics, drops, tags, and data.
  - [x] Ported 16 Spawner Statistics (`SpawnerStats`): `min_delay`, `max_delay`, `spawn_count`, `max_nearby_entities`, `req_player_range`, `spawn_range`, `initial_health`, `ignore_players`, `ignore_conditions`, `redstone_control`, `ignore_light`, `no_ai`, `silent`, `youthful`, `burning`, `echoing`.
  - [x] Ported 32 Spawner Modifier Recipes: 16 base and 16 inverse (offhand `minecraft:quartz`), with exact bounds clamping, non-placeable placement info, and server-authoritative item consumption preventing waste.
  - [x] Replaced vanilla Spawner Block, BlockEntity, and Item via targeted clinit mixins (`BlocksMixin`, `BlockEntityTypesMixin`, `ItemsMixin`), keeping full vanilla identity (`minecraft:spawner`, `minecraft:mob_spawner`).
  - [x] Implemented Silk Touch harvest with tool damage penalty (`spawnerSilkDamage`), `spawnersDropEmpty` configuration, and block entity NBT state persistence across drop and placement.
  - [x] Implemented explosive instability and cascade detonation (`onExplosionHit`): snapshotting `nextSpawnData`, 60-tick countdown, radius 8 detonation, 12-mob cluster spawning, and `apothic_spawners:gameplay/unstable_spawner` loot drops.
  - [x] Implemented light-level simulation via `LyingLevel` across $[0, 15]$ for `ignore_light` condition bypass.
  - [x] Implemented entity behavior modifiers: `no_ai` with `apotheosis:movable` physics travel and teleport cancellation, slime split inheritance, `youthful` baby state, `burning` fire ticks, and `echoing` extra loot rolls and XP multiplier ($1 + \text{level}$).
  - [x] Implemented Spawner-origin despawn protection: mob tagging (`apothic_spawners:from_spawner`) and cancellable despawn check in `MobDespawnMixin` during `tickCount <= entityDespawnDelay` without altering natural mob spawns or peaceful checks.
  - [x] Ported Capturing enchantment, spawn egg item predicate, and entity blacklist tag (`#apothic_spawners:blacklisted_from_spawners`) preventing forbidden egg modifications or drops.
  - [x] Implemented Client tooltip integration: category-colored spawner item names (Monster, Creature, Water, Ambient) and Shift hover stat modification summary.
  - [x] Verified with 10 automated unit tests in `SpawnerTests.java` (49/49 total project unit tests passing).
  - [x] Verified dedicated server launch (`./gradlew runServer --args="--nogui"`): 100% chunks loaded, clean recipe/advancement loading, zero errors, and clean shutdown.

### Milestone 8: Apothic Adventure Core Items & Economy Systems
- **Status**: **COMPLETE** (2026-10-29)
- **Key Achievements**:
  - [x] Audited full feature inventory documented in `docs/ADVENTURE_INVENTORY.md` covering dynamic registries, rarities, affixes, gems, sockets, purity weights, workstations, recipes, and UI screens.
  - [x] Ported dynamic data registries via Placebo: `RarityRegistry`, `RarityOverrideRegistry`, `AffixRegistry`, `ExtraGemBonusRegistry`, `GemRegistry`, `AffixLootRegistry`, `PurityWeightsRegistry`, and `TierAugmentRegistry`.
  - [x] Ported data components: `ItemAffixes` (`apotheosis:affixes`), `DynamicHolder<LootRarity>` (`apotheosis:rarity`), `SocketedGems` (`apotheosis:sockets`), `Purity` (`apotheosis:purity`), and `Component` (`apotheosis:affix_name`) with stream codecs.
  - [x] Implemented attribute derivation via targeted `ItemStackMixin` injecting into `forEachModifier(EquipmentSlot, ...)` and `forEachModifier(EquipmentSlotGroup, ...)` to cleanly supply attribute modifiers from affixes and socketed gems without persisting derived modifiers or causing duplicate applications.
  - [x] Ported full workstation suite: Reforging Table (`ReforgingTableBlock`, `ReforgingTableTile`, `ReforgingMenu`), Salvaging Table (`SalvagingTableBlock`, `SalvagingTableTile`, `SalvagingMenu`), Augmenting Table (`AugmentingTableBlock`, `AugmentingTableTile`, `AugmentingMenu`), Gem Cutting Table (`GemCuttingBlock`, `GemCuttingMenu`), and Gem Case (`GemCaseBlock`, `GemCaseTile`, `GemCaseMenu`).
  - [x] Implemented server-authoritative item conservation: strict cost and material checking before applying outputs or consuming resources; Fabric Transfer API (`ItemStorage.SIDED`) automation for workstations.
  - [x] Ported all specialized items and recipes: Sigil of Unnaming, Sigil of Withdrawal, Sigil of Socketing, Superior Sigil of Socketing, Sigil of Enhancement, Potion Charms, and Infusion recipes.
  - [x] Ported client screens and tooltips using vanilla 26.3 / RenderPearl pipeline abstractions: `ReforgingScreen`, `SalvagingScreen`, `AugmentingScreen`, `GemCuttingScreen`, `GemCaseScreen`, `WorldTierSelectScreen`, `WorldTierDetailScreen`, `WorldTierTutorialScreen`, `SocketTooltipRenderer`, and functional item comparison tooltips.
  - [x] Built non-instrumented resilience into `CachedObject` and `DeferredHelper` to allow headless JUnit tests to run without Loom mixin instrumentation.
  - [x] Verified with 7 automated unit tests in `AdventureTests.java` (56/56 total project unit tests passing) covering loot categories, item conservation, purity evaluation, socketing round-trip serialization, and affix exclusivity.
  - [x] Verified full build (`./gradlew build`), access widener validation, duplicate class check, and dedicated server startup (`./gradlew runServer --args="--nogui"`) with 100% chunks loaded, 1910 advancements parsed, and clean shutdown.

---

## 2. Upcoming Milestones

> [!NOTE]
> Roadmap format: One substantial prompt per numbered milestone (no A/B/C sub-prompts). Gateways to Eternity is completely out of scope; Patchouli / Chronicle of Shadows is deferred (TBD, not an initial release requirement). External legacy migrations from upstream/Zenith/older configs are not required; persistence tasks focus strictly on this fork's own save compatibility across supported upgrades per `docs/SAVE_COMPATIBILITY.md`.

### Milestone 9: Apotheosis Progression, Worldgen & Ecosystem Integration
- **Status**: **COMPLETE** (2026-10-30)
- **Key Achievements**:
  - [x] Implemented World Tiers (Haven, Frontier, Ascent, Summit, Pinnacle): advancement-based unlock requirements (`Apoth.Advancements`), server-authoritative persistence (`Attachments.WORLD_TIER`), multiplayer synchronization (`WorldTierPayload`), `/apotheosis world_tier` command, and full UI screens (`WorldTierSelectScreen`, `WorldTierDetailScreen`, `WorldTierTutorialScreen`).
  - [x] Implemented Tier Bonus Augment recalculation: `TierAugmentRegistry` dynamically applying player and monster attribute augments across tier transitions, deaths, respawns, dimension changes, and player joins without modifier accumulation.
  - [x] Reconciled standalone and Apotheosis spawner modifier recipes in `SpawnerRecipeCache` and `SpawnerModifier`: filtered out standalone `apothic_spawners` modifiers when `enableAdventure` is active, enforcing tiered spawner runes and progression pacing.
  - [x] Ported Invaders, Elites, and Augmentations: dynamic registries (`InvaderRegistry`, `EliteRegistry`, `AugmentRegistry`, `GearSetRegistry`), spawn rules per dimension (`InvaderSpawnRules`), delayed elite initialization, and boss announcement broadcasting.
  - [x] Ported Boss Spawner block & tile (`BossSpawnerBlock`, `BossSpawnerTile`): proximity player detection, cluster/mount spawning, and block replacement.
  - [x] Ported Boss Dungeon features (`BossDungeonFeature` cobblestone/brick variant, `BossDungeonFeature2` NBT template variant `boss_1.nbt`) and Rogue Spawners (`RogueSpawnerFeature`, `RogueSpawnerRegistry`).
  - [x] Ported Tome Towers jigsaw structures (`tower_main`, `tower_leaf`, `tower_sand`, `tower_spruce`) with `ItemFrameGemsProcessor` populating item frames with tiered gems and purity levels.
  - [x] Integrated worldgen features via Fabric Biome API v1 (`BiomeModifications.addFeature`) replacing NeoForge BiomeModifiers.
  - [x] Ported boss drops and dungeon chest loot tables with 26.3 `TagEntry` schema migration.
  - [x] Verified with 62 automated unit tests (`./gradlew test`) covering tier transitions, modifier removal, spawner progression reconciliation, spawn rules, and codecs.
  - [x] Verified dedicated server startup (`./gradlew runServer --args="--nogui"`): 1910 advancements loaded, 342 biome modifications applied, 100% chunks loaded across Overworld, Nether, and The End, and clean shutdown.

### Milestone 10: Complete & Verify Client-Facing Behavior & Visual Rendering
- **Status**: **COMPLETE** (2026-11-01)
- **Key Achievements**:
  - [x] Audited full client-facing feature surface across Attributes, Enchanting, Spawners, and Adventure modules.
  - [x] Authored comprehensive `docs/CLIENT_FEATURE_CHECKLIST.md` auditing menus, buttons, sliders, text search, focus, recipe-book coexistence, tooltips, comparisons, beams, particles, YACL options, and missing textures/localizations.
  - [x] Fixed NeoForge translation key in `ApothEnchantmentScreen.java` (`info.apothic_enchanting.limited_enchantability`) and added definition to `src/main/resources/assets/apothic_enchanting/lang/en_us.json`.
  - [x] Added missing localization key `title.apotheosis.world_tier_tutorial` in `src/main/resources/assets/apotheosis/lang/en_us.json`.
  - [x] Corrected `SalvagingScreen.java` input slot loop limit (`i < 12`) to prevent treating output slots as recipe inputs.
  - [x] Added `onClose()` overrides to `WorldTierDetailScreen.java` and `WorldTierTutorialScreen.java` to ensure ESC key cleanly navigates back to parent screens rather than dumping users to the world.
  - [x] Implemented dropped affix item beacon beam and shadow rendering in `AffixItemEffectRenderer.java`, `ItemEntityRenderStateMixin.java`, and `ItemEntityRendererMixin.java` using 26.3 `submitCustomGeometry` and `ApothRenderTypes.BEAM`, strictly honoring extraction/submission separation.
  - [x] Corrected beam origin coordinates in `BeamRenderer.java` removing block-level $(0.5, 0.5)$ translation for centered entity alignment.
  - [x] Added "Enable Affix Item Effects" toggle to `ClientConfig.java` and the YACL `ApotheosisConfigScreen.java`.
  - [x] Verified zero raw GL/Vulkan calls, strict dedicated-server isolation, and memoized render type resource lifetimes.
  - [x] Verified 62/62 automated tests pass cleanly (`./gradlew test`).

### Milestone 11: Approved Optional Integrations & Configuration Polish
- **Status**: **COMPLETE** (2026-11-01)
- **Key Achievements**:
  - [x] Implemented Just Enough Items (JEI `31.17.0.108`) recipe viewer integration (`ApotheosisJeiPlugin`):
    - Custom recipe categories: `SpawnerCategory`, `SalvagingCategory`, `GemCuttingCategory` (with `PurityUpgradeExtension` and `BasicGemCuttingExtension`), and `InfusionRecipeCategory`.
    - Registered workstation catalysts: `Blocks.SPAWNER`, `Apoth.Blocks.SALVAGING_TABLE`, `Apoth.Blocks.GEM_CUTTING_TABLE`, `Blocks.ENCHANTING_TABLE`, `Ench.Blocks.RAVEN_ENCHANTING_TABLE`.
    - Registered ingredient information pages: Monster Spawners (silk touch mechanics), Gem Dust, Sigils (Socketing, Enhancement, Unnaming, Withdrawal), Enchantment Library, and Enchanting Tables.
    - Registered GUI exclusion zones: `InventoryScreen` (`AttributesGui` sheet and sword toggle button) and `ApothEnchantmentScreen` (info tab extending above container).
    - Registered GUI recipe click areas for Infusion and Salvaging.
  - [x] Implemented server-authoritative recipe transfer: `RavenTransferHandler` transferring infusion recipe inputs into slot 0 and dispatching `SetRavenStatsPayload` to the server, which validates and clamps slider stats against surrounding bookcases before applying.
  - [x] Implemented Jade (`26.3.5+fabric`) block and spawner inspection providers:
    - Server data providers (`SpawnerServerDataProvider`, `EnchantingServerDataProvider`) in `dev.shadowsoffire.apotheosis.compat.jade`.
    - Client tooltip providers (`SpawnerClientProvider`, `EnchantingClientProvider`) in `dev.shadowsoffire.apotheosis.client.compat.jade`.
    - Dynamic reflection delegation ensuring zero client classes are loaded on dedicated servers.
  - [x] Accessory slot integration & safety:
    - Evaluated Fabric 26.3 ecosystem: Trinkets and Accessories have 0 releases published for 26.3; reported as deferred.
    - Preserved decoupled `ModifierSourceType` extension point preventing duplicate attribute application.
    - Updated `PotionCharmItem` to fall back to inventory ticking when accessory mods are absent.
  - [x] Enchantment description & tooltip cooperation:
    - Added `enableInlineEnchDescs` config toggle to `ClientConfig` and YACL screen.
    - Implemented automatic yielding in `ApothEnchClient` when external description mods (`enchdesc`, `idwtialsimmofg`, `enchantmentdescriptions`) are present.
  - [x] Resolved packaging documentation conflicts:
    - Nested YetAnotherConfigLib (YACL `3.9.6+26.3-fabric`) via Jar-in-Jar (`include`) in `build.gradle`.
    - Verified JAR contents: `META-INF/jars/yet-another-config-lib-3.9.6+26.3-fabric.jar` intact with original manifest, `fabric.mod.json`, and LGPL-3.0 license preserved without shading.
  - [x] Configuration audit & remote server safety:
    - Enforced read-only mode (`.available(!isRemote)`) on server-authoritative options when connected to remote servers.
    - Cleaned up remote state on client disconnect (`ClientPlayConnectionEvents.DISCONNECT -> clearRemoteServerConfig`).
  - [x] Scope boundaries preserved: Gateways completely excluded; Patchouli deferred.
  - [x] Verified full clean build (`./gradlew build`), all 62 unit tests passing (`./gradlew test`), and verified jar-in-jar packaging.

### Milestone 12: Final Verification & Release-Candidate Preparation
- **Status**: **COMPLETE** (2026-11-01)
- **Key Achievements**:
  - [x] Full parity review conducted across all subsystems: reconciled every inventory document (`SPAWNERS_INVENTORY.md`, `ENCHANTING_INVENTORY.md`, `ADVENTURE_INVENTORY.md`) against actual code. Audited and verified zero active stubs, no-ops, or incomplete registrations.
  - [x] Established `1.0.0-alpha.1` as the fork's persistence and save-compatibility baseline:
    - Authored `SaveCompatibilityTest.java` validating persistent registry identifier stability for all data components, blocks, items, attachments, and custom attributes.
    - Verified multi-pass serialization round-tripping for affix equipment, socketed gems, spawner parameters, Raven Table stats, and World Tier progression.
    - Verified recoverable/unknown data tag preservation (no silent resets).
    - Verified that module toggling does not erase world content.
  - [x] Authored `docs/FUTURE_UPGRADE_GUIDE.md`: prepared reusable upgrade testing procedures and documentation for future Minecraft releases (e.g. 26.4) with an explicit policy against speculative adapters or premature compatibility claims.
  - [x] Packaging and runtime audit:
    - Built release candidate artifact `build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar` with nested YACL JiJ unshaded.
    - Passed all 68 automated unit tests (`./gradlew test --rerun-tasks`).
    - Verified dedicated server startup and clean shutdown (`./gradlew runServer --args="--nogui"`) with 1910 advancements, 342 biome modifications across 57 biomes, and 100% chunks loaded.
    - Verified zero duplicate classes and exclusion of reference trees from artifacts.
  - [x] Authored `docs/RELEASE_READINESS.md` containing completed parity matrix, installation instructions, reproducible build guide, and explicit limitations.
  - [x] Release posture: Release Candidate ready for testing. Do not publish.

### Post-Milestone 12 Hotfix 1: Client Startup & Resource Loading Repairs
- **Status**: **COMPLETE** (2026-11-01)
- **Primary Artifact**: `build/libs/fabricated-apotheosis-1.0.0-alpha.2.jar`
- **Key Fixes**:
  - [x] **EnchantTableRendererMixin Injection Repair**: Corrected `@At(value = "INVOKE")` descriptor for `SubmitNodeCollector.submitModel(...)` to match Minecraft 26.3 runtime bytecode (9 arguments instead of 10). Added safe crumbling overlay render type override for custom book textures.
  - [x] **Gem Model Dynamic Dispatch**: Registered `apotheosis:gem_dispatch` (`GemModel.Unbaked.MAP_CODEC`) into `ItemModels.ID_MAPPER` via `ItemModelsMixin` and accesswidener. Implemented per-gem model dispatch resolving all 23 built-in gem variants.
  - [x] **Atlas & Sprite Resource Resolution**: Added `blocks` and `items` directory sources to `assets/minecraft/atlases/blocks.json` and `items.json`, resolving all 293 model texture references. Corrected namespaced model paths for stray model duplicates.
  - [x] **Automated Verification**: Authored `ClientResourceValidationTest` (4 tests). Total test suite: 72/72 tests passing. Dedicated server launch and clean stop verified.

### Post-Milestone 12 Hotfix 2: In-Game Mechanics, UI & Dynamic Data Loading Repairs
- **Status**: **COMPLETE** (2026-11-01)
- **Primary Artifact**: `build/libs/fabricated-apotheosis-1.0.0-alpha.3.jar`
- **SHA-256**: `94f4841d226200fa263c288cf896f1f339e9d7a9743dc6e8399d7d68b64ddbd6`
- **Key Fixes**:
  - [x] **Table Placement Crash (`Invalid block entity`)**: Added `FabricBlockEntityType.addValidBlock(...)` in `ApothicEnchanting.java` for `APOTHIC_ENCHANTING_TABLE` and `RAVEN_ENCHANTING_TABLE` so vanilla enchanting table block entity accepts custom tables.
  - [x] **Dynamic Registry Server Data Decoding & Datapack Lookup**:
    - Captured `loadResult.lookupWithUpdatedTags()` from `ReloadableServerResources` constructor into `DynamicRegistry.activeLookup` via `ReloadableServerResourcesMixin`.
    - Wired `createOps()` to use `getActiveLookup()` and added decoupled client lookup provider via `AdventureModuleClient.init()`.
    - Initialized `LootRule.initCodecs()` and `GemBonus.initCodecs()` in `AdventureModule.init()`.
    - Added DAG dependency edges (`AffixLootRegistry -> AffixRegistry`, `ExtraGemBonusRegistry -> GemRegistry`, `InvaderRegistry -> AffixRegistry`).
    - Added `CHARM` category to `Apoth.LootCategories` and `CREATIVE_FLIGHT` attribute to `ALObjects.Attributes` (with flight tick integration in `PlayerMixin`).
    - Fixed `ContextAwarePredicate` to handle both `"condition"` and `"type"` JSON fields.
    - Fixed `RarityOverrideRegistry` category lookup and reload mapping.
    - Verified 100% of dynamic registry entries loaded cleanly: 99 affixes, 21 gems, 30 gear sets, 23 invaders, 4 elites, 9 rogue spawners, 27 tier augments, 5 rarities, 1 rarity override with 0 errors.
  - [x] **Loot & Empty Pool Crash Guard**: Added null-safe empty check in `ApothMiscUtil.getRandomElement(...)` and guarded `TieredDynamicRegistry.getRandomItem(...)` against empty candidate sets.
  - [x] **Gradient Color Network Packet Decoder**: Overrode `GradientColor.serialize()` to format as hex `#%06X` and made `TextColor.NAMED_COLORS` accessible in `apotheosis.accesswidener`, preventing chat/system packet decoder crashes.
  - [x] **Creative Tabs & Missing Module Content**:
    - Registered `CreativeModeTabEvents.modifyOutputEvent` for `Apoth.Tabs.ADVENTURE` (scrap, fabric, sigils, workstations, templates, music discs, spawner runes, gems via `ITabFiller`, boss summoners).
    - Registered `CreativeModeTabs.FOOD_AND_DRINKS` for `POTION_CHARM`.
    - Applied mixin redirect in `CreativeModeTabsMixin` directing `Enchantment.getMaxLevel()` to `EnchHooks.getMaxLevel(ench)` so creative search indexes uncapped Apotheosis levels.
    - Fixed thread-safety in `ApothEnchClient` search indexing by using `block.defaultBlockState()` instead of placement contexts.
  - [x] **Enchanted Book Tooltip Deduplication**: Rebuilt `ItemStackMixin` tooltip generation with `seen` set and `level > 0` guard to eliminate duplicate lines and uncapped enchantment display discrepancies.
  - [x] **World Tier UI & Keybind**:
    - Changed default keybind check in `AdventureKeys.OPEN_WORLD_TIER_SELECT` to require `Ctrl` when bound to `T` to prevent chat opening conflicts.
    - Added `"key.category.apotheosis.keys": "Apotheosis"` in `en_us.json`.
    - Fixed `WorldTier.HAVEN` unlock state (`isUnlocked` returns true for Haven and for unlocked progression tiers).
    - Added `WorldTierSelectScreen.updateButtonStatus()` in `tick()` so UI updates live.
    - Fixed `WorldTierTutorialScreen` background rendering by delegating `extractBackground`, `extractRenderState`, and `resize` to `parent`.
  - [x] **Automated Verification**: Authored `InGameRepairsTest`, updated `AttributesTest` and `SaveCompatibilityTest`. 75/75 tests passing. Dedicated server launch cleanly verified in 0.205s with 0 errors.

---

## 2. Milestone Summary

All 12 project milestones are now **COMPLETE**. The codebase is updated to Release Candidate (`1.0.0-alpha.3`) with client startup, in-world gameplay, and dynamic registry repairs verified. Releases will not be published without explicit user authorization.
