# Feature Parity Matrix — Apotheosis NeoForge (26.1.2) to Fabric (26.3)

## 1. Overview & Status Key

This matrix tracks the parity status of all subsystems across the Apotheosis ecosystem.

Status Definitions:
- **`not started`**: Feature identified in upstream, no implementation work started.
- **`investigated`**: Source, hooks, and porting surface audited; architecture established.
- **`implemented`**: Code written and integrated into the Fabric project skeleton.
- **`verified`**: Verified through compilation, automated tests, or in-game runtime launch.
- **`blocked`**: Dependent on an unresolved design decision or external dependency.

---

## 2. Infrastructure & Platform Skeleton

| Feature / Subsystem | Status | Verification & Evidence | Notes |
| :--- | :--- | :--- | :--- |
| **Combined 1-JAR Packaging** | `verified` | `./gradlew build` outputs single jar | `fabricated-apotheosis-1.0.0-alpha.1.jar` |
| **Fabric 26.3 Toolchain** | `verified` | Loom 1.17.21, Fabric Loader 0.19.5, Java 25 | Fully builds and packages |
| **Dedicated Server Launch** | `verified` | `./gradlew runServer --args="--nogui"` | 100% chunks loaded, clean stop, zero client leak |
| **Server-Safe Config Model** | `verified` | `ServerConfig.java`, atomic writes, schema v1 | Tested in `ServerConfigTest` & server startup |
| **Client Preferences Model** | `verified` | `ClientConfig.java`, persists in `apotheosis-client.json` | Tested in `ClientConfigTest` & `ApotheosisConfigTest` |
| **Schema & Legacy Migration** | `verified` | `ConfigMigration.java` | Tested in `ConfigMigrationTest` (v0->v1 and `.cfg`) |
| **Network Config Sync** | `verified` | `ApotheosisConfigPayload.java` | Tested in `ServerConfigTest.testNetworkStreamCodec` |
| **Remote Stale State Clearing** | `verified` | `ClientPlayConnectionEvents.DISCONNECT` hook | Tested in `ApotheosisConfigTest.testRemoteOverride` |
| **YACL Client Config Screen** | `verified` | `ApotheosisConfigScreen.java` | Segregated in client source set; read-only on remote |
| **Reference Trees Isolation** | `verified` | `.gitignore` and `REFERENCE_INDEX.md` | Reference files strictly read-only and unbundled |

---

## 3. Apothic Spawners Module

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **16 Spawner Statistics (`SpawnerStats`)** | `verified` | Ported 16 custom & vanilla spawner stats (`min_delay`, `max_delay`, `spawn_count`, `max_nearby_entities`, `req_player_range`, `spawn_range`, `initial_health`, `ignore_players`, `ignore_conditions`, `redstone_control`, `ignore_light`, `no_ai`, `silent`, `youthful`, `burning`, `echoing`). | Tested in `SpawnerTests.testStatClampingAndBounds` and dedicated server boot. |
| **32 Spawner Modifier Recipes** | `verified` | 16 base and 16 inverse recipes (`SpawnerModifier`, `PlacementInfo.NOT_PLACEABLE`) with quartz offhand. Bounds checking prevents item consumption when stats are at limit. | Tested in `SpawnerTests.testModifierRecipeSerialization` and item consumption checks. |
| **Silk Touch Capture & State Drops** | `verified` | Custom drop logic in `ApothSpawnerBlock.playerDestroy`: requires `spawnerSilkLevel` (1), damages tool by `spawnerSilkDamage` (100), drops `minecraft:spawner` with `DataComponents.BLOCK_ENTITY_DATA`. | Tested in `SpawnerTests.testSilkTouchHarvestLogic` and `testPlacementStateRestoration`. |
| **Redstone Activation Control** | `verified` | `redstone_control` stat requires neighbor signal $> 0$ for spawner ticking and client spin particles. | Tested in `SpawnerTests.testRedstoneActivationControl`. |
| **Condition Ignore & Light Simulation** | `verified` | `ignore_conditions` bypasses collision and placement rules. `ignore_light` simulates light levels $0 \dots 15$ via `LyingLevel` (`ServerLevelAccessor`). | Verified in `ApothSpawnerTile.serverTick` and `LyingLevel.java`. |
| **Explosion Instability & Detonation** | `verified` | `onExplosionHit` triggers 60-tick unstable countdown, snapshotting `nextSpawnData`, terminating in radius 8 blast, 12-mob cluster spawn, and `unstable_spawner` loot table drop. | Tested in `SpawnerTests.testInstabilityDetonationCountdown` and NBT persistence. |
| **Spawner-Origin Despawn Handling** | `verified` | Spawner-spawned mobs receive persistent tag `apothic_spawners:from_spawner`. `MobDespawnMixin` prevents despawning while `tickCount <= entityDespawnDelay` without altering natural mob spawns or peaceful checks. | Tested in `SpawnerTests.testSpawnerOriginDespawnTagging`. |
| **Special Entity Modifiers** | `verified` | `no_ai` mobs receive `apotheosis:movable` (physics travel in `MobTickMixin`, teleport cancel in `EntityTeleportMixin`, split cube inheritance in `SlimeSplitMixin`), `youthful` baby state, `burning` fire ticks, and `echoing` extra loot/XP. | Tested in `SpawnerTests.testSpecialEntityModifiers` and `LivingEntityMixin`. |
| **Capturing Enchantment & Blacklist** | `verified` | `CapturingEnchantmentEffect` (0.5% per level egg drop on kill). Entities in `#apothic_spawners:blacklisted_from_spawners` are rejected from spawner modification and egg drops. | Tested in `SpawnerTests.testBlacklistAndCapturingEnchantment` and `ApothicSpawners.init`. |
| **Spawner Item & Client Tooltips** | `verified` | `ApothSpawnerItem` provides category-colored item names (Monster, Creature, Water, Ambient) and Shift hover stat modification summary via client tooltip handlers. | Verified in `ApothSpawnerItem.java` and `ASClient.java`. |

---

## 4. Apothic Attributes Module

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **21 Custom Combat Attributes** | `verified` | Registered via `DeferredHelper` under `apothic_attributes` with `.setSyncable(true)`. Attached to all living entity types via `FabricDefaultAttributeRegistry.MODIFY`. | Tested in `AttributesTest.testAll21AttributesPresentAndSyncable` & `testAttributeBoundsAndDefaults`. |
| **Mining Speed Attribute Removal & Migration** | `verified` | Confirmed removed upstream; replaced with vanilla `MINING_EFFICIENCY`. Stored modifiers migrated via `AttributeHelper.migrateItemModifiers`. | Tested in `AttributesTest.testObsoleteMiningSpeedMigration`: obsolete ID removed and vanilla efficiency verified. |
| **Percentage & Boolean Attribute Formatting** | `verified` | Fabric mod-level formatting replacing NeoForge `PercentageAttribute` and `BooleanAttribute`. | Tested in `AttributesTest.testPercentageAndBooleanFormatting`. |
| **Modifier Accumulation Prevention** | `verified` | Enforces that updating/re-equipping modifiers with identical identifiers replaces rather than duplicates modifiers. | Tested in `AttributesTest.testModifierAccumulationPrevention`. |
| **Attribute Inspection UI (`AttributesGui`)** | `verified` | Scrollable inspection UI with sword toggle button on `InventoryScreen`, "Hide Unchanged" filter, and RenderPearl 26.3 GUI pipeline. | `AttributesGui.java`, `InventoryScreenMixin`, and exclusion zones for recipe viewers. |
| **Modifier Provenance & Slot Display** | `verified` | Visual provenance rendering in tooltips with equipment slot items and mob effect sprites via `Hud.getMobEffectSprite`. | `ModifierSource.java`, `ModifierSourceType.java`, `AttributeModifierComponent.java`. |
| **Hidden Attribute Filtering** | `verified` | Configurable client-side attribute filtering with namespace wildcards and negation (`!`) support. | Tested in `AttributesTest.testHiddenAttributeFiltering`. |
| **Item & Potion Tooltip Integration** | `verified` | Fabric `ItemTooltipCallback` hook providing potion descriptions and automatic obsolete attribute migration. | `AttributesClientTooltipHandler.java`. |
| **Accessory-Slot Extension Points** | `verified` | Open extension registry `ModifierSourceType.register()` allowing external accessory/curios mods to add custom modifier provenance. | `ModifierSourceType.java`. |
| **Combat Rules & Piercing Hooks** | `verified` | Custom armor and protection curves in `ALCombatRules.java`, toughness bypass resistance, negative effective armor scaling, armor shred/pierce, prot shred/pierce. Mixins: `CombatRulesMixin`, `LivingEntityMixin`. | Tested in `CombatRulesTest.testArmorReduction`, `testArmorPierceAndShred`, `testNegativeArmorAmplification`, `testProtectionPierceAndShred`. |
| **Critical Strikes & Multicrit** | `verified` | Multicrit tiered degradation loop ($0.85\times$ per tier), vanilla jump critical upgrade, S2C particle dispatch (`CritParticlePayload`). Mixins: `PlayerMixin`, `LivingEntityMixin`. | Tested in `CombatRulesTest.testMulticritCalculation`. |
| **Auxiliary Damage Pipeline** | `verified` | Non-reentrant auxiliary damage execution (`AuxDmgTracker`, `AttributeEvents`), invulnerability time preservation, sweeping ratio scaling, current-HP, fire, and cold damage with post-callbacks. | Tested in `CombatRulesTest.testAuxDmgTrackerNoRecursion` and dedicated server run. |
| **Dodge Mechanics** | `verified` | Deterministic per-tick hash dodge roll (`AttributeEvents.checkDodge`), melee attack reach bounds check, projectile impact dodge (`AbstractArrowMixin`), dodge sound event. | Tested in `CombatRulesTest.testDodgeChanceDeterminism`. |
| **Overheal, Life Steal & Ghost Health** | `verified` | Post-damage life steal and overheal conversion in `actuallyHurt`, overheal absorption cap (50% max HP) without destroying external absorption (`AttributesUtil`), ghost health restoration. | `LivingEntityMixin`, `AttributesUtil`, tested in `CombatRulesTest`. |
| **Healing Scaling & Mob Effects** | `verified` | `HEALING_RECEIVED` scaling in `heal()` with cancellation when $\le 0$. 7 mob effects (`BleedingEffect`, `DetonationEffect`, `FlyingEffect`, `GrievousEffect`, `KnowledgeEffect`, `SunderingEffect`, `VitalityEffect`). | `LivingEntityMixin`, effect classes, verified on dedicated server. |
| **Projectile & Arrow Scaling** | `verified` | First-tick projectile velocity and base damage scaling with reload loop guard (`apothic_attributes.arrow.done`), thrown trident quad-scaling. | `AbstractArrowMixin`, `ThrownTridentMixin`, `AbstractArrowAccessor`. |
| **Draw Speed & Ability Cooldowns** | `verified` | `DRAW_SPEED` use-duration tick modification in `updateUsingItem`, persistent cooldown tracking (`CooldownTracker`, `AbilityCooldowns`) with CDR formula. | Tested in `CombatRulesTest.testCooldownReductionMath`, `testCooldownTrackerPrune`. |
| **Flight Mechanics** | `verified` | Glide capability without glider item (`canGlide`, `canGlideUsing`), creative flight tracking (`had_flight_granted`) preserving external flight states. | `LivingEntityMixin`, `PlayerMixin`. |

---

## 5. Apothic Enchanting Module

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **Table Stats (Eterna, Quanta, Arcana, Rectification, Clues)** | `verified` | Step-ladder Eterna aggregation, Quanta $[1, 200]$ power clamping, stable floor preservation, Arcana threshold weight shifts. | Tested in `EnchantingTests` (Eterna step-ladder, Quanta variance, Rectification, Arcana tiers, slot cost progression scaling). |
| **Bookshelf Blocks (30 Shelves)** | `verified` | Wood shelves (`TypedShelfBlock`), Sculk shelves, Filtering, Geode, Treasure shelves with particle definitions. | Blocks and BlockItems registered in `Ench.java`; verified on dedicated server boot. |
| **Enchantment Library (Basic & Ender)** | `verified` | Transactional storage via Fabric Transfer API (`Storage<ItemVariant>`, `SnapshotParticipant`), $2^{N-1}$ point conversion math, max levels 16 and 31. Sided `ItemStorage` registered. | Tested in `EnchantingTests.testLevelToPoints`, `testLibraryTransactionalStorage` (insert, commit, abort rollback, nested transactions); zero duplication/deletion. |
| **Anvil Overhaul & Cap Removal** | `verified` | Level 40 cap removed (`ordinal = 2, require = 1`), optimal XP progression calculation, Tome combining limits, Prismatic Web curse removal, damaged anvil iron block restoration. | Tested in `EnchantingTests.testAnvilCostExperienceCalculations`, `testAnvilLevel40CapRemoval`, `testAnvilEvents`. |
| **Custom Tomes (6 Types)** | `verified` | Helm, Chestplate, Leggings, Boots, Weapon, Pickaxe, Fishing tomes limiting enchantment application to specific gear types. | Items registered in `Ench.Items.TYPED_BOOKS`; verified on server launch. |
| **Infusion & Keep-NBT Recipes** | `verified` | Custom recipe serializers for enchanting table infusion and stat-requirement validation. | Serializers registered, `InfusionRecipeCache` loaded from `RecipeManager`. |
| **Enchantment Info Loader & Datapack Merging** | `verified` | Staged atomic validation, datapack priority order, `"replace": true`, `"remove": [...]`, atomic publication (`INFO_MAP = Map.copyOf(staged)`), cache invalidation, S2C sync (`EnchantmentInfoPayload`), and disconnect reset. | Verified on dedicated server load (6 overrides published); unit tested. |
| **Coremod ASM Replacements** | `verified` | Full replacement of NeoForge ASM coremods: `AnvilMenuMixin`, `EnchantCommandMixin`, `FishingHookMixin`, `EnchantRandomlyFunctionMixin`, `SingleEnchantmentMixin`. | Bytecode matched to 26.3 signatures, verified with require=1, unit tests, and dedicated server. |
| **20 Custom Enchantments** | `verified` | Data-driven enchantments (`berserkers_fury`, `chainsaw`, `crescendo`, `endless_quiver`, `exploitation`, `growth_serum`, `icy_thorns`, `knowledge`, `miners_fervor`, `nature_blessing`, `obliteration`, `rebounding`, `reflective`, `scavenger`, `shield_bash`, `spearfishing`, `stable_footing`, `tethering`, `warden_curse`, `vitality`). | JSON definitions verified; loaded without unbound keys on dedicated server startup. |
| **14 Enchantment Effect Components** | `verified` | Custom data component effects registered under `apothic_enchanting` and executed via vanilla 26.3 effect pipelines. | Codecs registered in `Ench.Components`; effect implementations in `dev.shadowsoffire.apothic_enchanting.enchantments.effects`. |
| **Client Screens, UI & ModMenu** | `verified` | `ApothEnchantmentScreen`, `RavenEnchantmentScreen`, `EnchLibraryScreen`, `EnchantingInfoScreen` with RenderPearl GUI pipeline; ModMenu integration (`ModMenuIntegration`). | Client compilation passed; client launch verified (`runClient`); zero client classes referenced on dedicated server. |

---

## 6. Placebo Support (Required Subsets)

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **Registration Helpers (`DeferredHelper`)** | `verified` | Clean registration wrapper with explicit duplicate key detection. | Tested in `DeferredHelperTest`: duplicate registration throws clear `IllegalStateException`. |
| **Codecs & Serialization (`StepFunction`, `PlaceboCodecs`, `MapBackedCodec`, `CodecMap`)** | `verified` | Math step functions and polymorphic codec maps for reloadable elements. | Tested in `StepFunctionTest`: exact JSON/NBT round-trip and level-based calculations pass. |
| **Dynamic Data Loading (`DynamicRegistry`, `WeightedDynamicRegistry`, `DynamicHolder`)** | `verified` | Dynamic data loading from datapacks with atomic reloading and cache invalidation. | Tested in `DynamicRegistryTest`: load, reload, invalidation, and malformed JSON error handling verified. |
| **Dynamic Registry Coordination (`DynamicRegistryManager`)** | `verified` | Server resource reload listener and network synchronization coordinator. | Verified on dedicated server startup (`Placebo/DynRegManager: Dynamic registry manager initialized successfully`). |
| **Item Cache Utilities (`CachedObject`)** | `implemented` | Component-hash based item stack caching to prevent redundant calculations. | `CachedObject.java` in place with component hashing. |
| **Experience Math (`EnchantmentUtils`)** | `verified` | Exact vanilla XP-to-level and level-to-XP conversions. | Tested in `EnchantmentUtilsTest` against vanilla level curve formulas. |
| **Network Payload Registrar (`PayloadHelper`)** | `verified` | Dual clientbound/serverbound payload registration with dedicated-server safe client handler. | Server receivers registered; client separation verified via `PlaceboClientPayloadHandler`. |
| **Packaging & Standalone Module Defense** | `verified` | Duplicate class enforcement and explicit mod conflict blocks. | `verifyNoDuplicateClasses` Gradle task, `DuplicatesStrategy.FAIL`, and `"breaks"` in `fabric.mod.json`. |

---

## 7. Integrations & Documentation

| Subsystem | Status | Target Scope / Plan |
| :--- | :--- | :--- |
| **Gateways to Eternity** | `out of scope` | Completely out of scope; removed from planned work and release requirements. |
| **Patchouli / Chronicle of Shadows**| `deferred` | Deferred (TBD); not an initial release requirement; no replacement book system planned. |
| **ModMenu Config Integration** | `investigated` | Entrypoint hook to open YACL config screen from ModMenu list. |
| **RenderPearl GPU Pipelines** | `investigated` | Reserved for boss health bars, gem render passes, and custom screen shaders. |

---

## 8. Apothic Adventure Module (Items & Economy)

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **Dynamic Registries** | `verified` | Rarities, Rarity Overrides, Affixes, Gems, Extra Gem Bonuses, Affix Loot Entries, Purity Weights, and Tier Augments loaded dynamically via Placebo. | Registered in `AdventureModule`; loaded and serialized via codecs and stream codecs. |
| **Data Components & Persistence** | `verified` | `ItemAffixes`, `DynamicHolder<LootRarity>`, `SocketedGems`, `Purity`, and `Component` (affix name) stored as 26.3 `DataComponents`. | Verified in `AdventureTests.testSocketingRoundTrip` and `testAffixExclusivityAndItemAffixesSerialization`. |
| **Attribute Modifier Derivation** | `verified` | In-place attribute derivation via `ItemStackMixin` injecting into `forEachModifier(EquipmentSlot, ...)` and `forEachModifier(EquipmentSlotGroup, ...)`. | Mixin verified with `require = 1`; dynamic modifiers update upon equip/unequip without duplicate accumulation. |
| **Loot Categories** | `verified` | Equipment categorization (`BOW`, `BREAKER`, `HELMET`, `CHESTPLATE`, `LEGGINGS`, `BOOTS`, `SHIELD`, `TRIDENT`, `MELEE_WEAPON`, `SHEARS`, `NONE`). | Verified in `AdventureTests.testLootCategories`. |
| **Purity System** | `verified` | Purity levels (`CRACKED`, `CHIPPED`, `FLAWED`, `NORMAL`, `FLAWLESS`, `PERFECT`) with upgrade weights and step thresholds. | Verified in `AdventureTests.testPurityWeightsAndProgression`. |
| **Reforging Table & Recipes** | `verified` | Reforging table workstation, menu, cost evaluation (rarity materials + gem dust + player levels), server-authoritative item conservation. | Tested in `AdventureTests.testItemConservationReforging`; automation registered via `ItemStorage.SIDED`. |
| **Salvaging Table & Recipes** | `verified` | Salvaging workstation, menu, drop rate output tables, non-destructive validation, and partial refund rules. | Tested in `AdventureTests.testItemConservationSalvaging`; automation registered via `ItemStorage.SIDED`. |
| **Augmenting & Gem Cutting** | `verified` | Augmenting workstation for tier modifiers and Gem Cutting workstation for gem purity upgrades. | Blocks, tiles, menus, and client screens implemented with server authority. |
| **Gem Case & Management** | `verified` | Gem Case storage block, tile entity, menu, and client selection interface. | Implemented with custom extraction and client GUI navigation. |
| **Sigils & Consumables** | `verified` | Sigil of Unnaming, Sigil of Withdrawal, Sigils of Socketing, Potion Charms, and Infusion recipes. | Recipes and item handlers registered and verified on server launch. |
| **Workstation Automation** | `verified` | Fabric Transfer API `ItemStorage.SIDED` integration for automated input/output on reforging and salvaging stations. | Registered in `AdventureModule`; unit tested inventory conservation. |
| **Client Screens & Tooltips** | `verified` | Functional container screens, radial state tracking, equipment comparison positioning, and custom socket/gem tooltips. | Client compilation passed; zero client leakage to dedicated server. |

---

## 9. Apothic Progression & World Content (Milestone 9)

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **World Tiers** | `verified` | 5 World Tiers (Haven, Frontier, Ascent, Summit, Pinnacle) with advancement unlock requirements, server validation, client selection UI (`WorldTierSelectScreen`, `WorldTierDetailScreen`, `WorldTierTutorialScreen`), and S2C networking sync (`WorldTierPayload`). | Tested in `ProgressionAndWorldgenTests.testWorldTierOrderingAndProgression`; synced via `ServerPlayNetworking`. |
| **Tier Bonus Augments** | `verified` | Dynamic attribute bonuses for players and monsters (`TierAugmentRegistry`), reapplied on tier transition, join, respawn, and dimension change with clean removal of obsolete modifiers. | Tested in `ProgressionAndWorldgenTests.testTierBonusApplicationAndCleanRemoval`; zero modifier accumulation verified. |
| **Spawner Progression Reconciliation** | `verified` | Reconciles standalone `apothic_spawners` modifiers with tiered progression; filters out standalone modifier recipes when `enableAdventure` is active to enforce spawner rune progression. | Tested in `ProgressionAndWorldgenTests.testSpawnerRecipeCacheReconciliation`. |
| **Boss / Invader Spawning** | `verified` | Natural invader spawn rules per dimension (`InvaderSpawnRules`), boss gearsets, enchantments, cursed item rules, cooldown timers (`Attachments.INVADER_COOLDOWN`), and cluster announcement broadcasts (`BossSpawnPayload`). | Tested in `ProgressionAndWorldgenTests.testInvaderSpawnRules`; verified on dedicated server boot. |
| **Elites & Augmentations** | `verified` | `EliteRegistry` and `AugmentRegistry` for miniboss transformations, passenger loading, delayed elite initialization, and specialized combat augmentations. | Registries registered and loaded dynamically; unit tested codec round-trips. |
| **Boss Spawner Block & Summoner** | `verified` | `BossSpawnerBlock` and `BossSpawnerTile` detecting players within range, spawning boss with equipment, and replacing with air; `BossSummonerItem` and `/apotheosis spawn_boss` command. | Verified in dedicated server environment; command registered in `AdventureModule`. |
| **Boss Dungeons (Variants 1 & 2)** | `verified` | Procedural cobblestone/brick boss dungeon (`BossDungeonFeature`) and NBT template structure (`BossDungeonFeature2`, `boss_1.nbt`), double chest loot, and boss spawner block. | Tested in `ProgressionAndWorldgenTests.testFeatureMapCodecs`; worldgen definitions migrated to 26.3 format. |
| **Rogue Spawners** | `verified` | Underworld spawner features (`RogueSpawnerFeature`, `RogueSpawnerRegistry`) generating tiered spawner blocks with cover blocks and loot chests. | Tested in `ProgressionAndWorldgenTests.testFeatureMapCodecs`; verified in worldgen registry. |
| **Tome Towers** | `verified` | Jigsaw-based Tome Towers (`tower_main`, `tower_leaf`, `tower_sand`, `tower_spruce`) generating across biome tags; `ItemFrameGemsProcessor` populating item frames with random tiered gems and purity. | Structure templates, pool JSONs, and processor codecs registered and verified on server launch. |
| **Biome Modification** | `verified` | Fabric Biome API v1 (`BiomeModifications.addFeature`) replacing NeoForge BiomeModifiers, registering boss dungeons and rogue spawners across biome selectors. | 342 biome modifications applied across 57 biomes verified on dedicated server boot. |
| **Loot Tables & Injection** | `verified` | Boss drop tables, dungeon chests, and wandering trader loot tables migrated to 26.3 schema (`TagEntry.items`). | Server boot verified without loot table parse errors. |

---

## 10. Client-Facing Behavior & Visual Rendering (Milestone 10)

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **Client Feature Checklist** | `verified` | Complete inventory and interaction checklist documented in `docs/CLIENT_FEATURE_CHECKLIST.md`. | Verified all 16 client subsystem components, UI controls, and text formatting. |
| **Workstation UI Interactions** | `verified` | Menus, buttons, text inputs, search filtering, and scroll dragging across Reforging, Salvaging, Augmenting, Gem Cutting, and Gem Case. | Code audit and boundary fix in `SalvagingScreen` (`i < 12`); keyboard focus suppression verified in `EnchLibraryScreen` and `GemCaseScreen`. |
| **Screen Navigation & Focus** | `verified` | Keyboard focus, recipe-book coexistence, and ESC key handling. | Added `onClose()` overrides to `WorldTierDetailScreen` and `WorldTierTutorialScreen`; verified attributes GUI repositioning via `InventoryScreenMixin`. |
| **Loot Beams & Item Effects** | `verified` | Dropped affix item beacon beams and colored shadows rendered in-world with rarity tinting and grow-in animations. | Implemented in `AffixItemEffectRenderer`, `ItemEntityRenderStateMixin`, and `ItemEntityRendererMixin` via 26.3 `submitCustomGeometry` and `ApothRenderTypes.BEAM`; origin centered without block offset. |
| **Rarity Particles** | `verified` | Ambient sparkle particles emitted from grounded affix items. | Client tick handler in `AdventureModuleClient` rate-limits emission per item; particles clear cleanly on disconnect. |
| **Localization Completeness** | `verified` | All client screens and tooltips mapped to Fabric-native translatable strings in JSON language files. | Replaced NeoForge translation key in `ApothEnchantmentScreen`; added missing `title.apotheosis.world_tier_tutorial` in `en_us.json`. |
| **Config UI (YACL & ModMenu)** | `verified` | YetAnotherConfigLib configuration screen accessible via ModMenu with Client Preferences and server rule separation. | Tested `ApotheosisConfigScreen`; added "Enable Affix Item Effects" toggle; verified atomic saving and remote server read-only rules. |
| **Zero Raw GL / Vulkan Calls** | `verified` | Mod rendering uses exclusively Mojang high-level abstractions (`SubmitNodeCollector`, `RenderPipelines`, `GuiGraphicsExtractor`). | Codebase audited for zero raw OpenGL/Vulkan driver calls; dedicated-server isolation preserved. |

---

## 11. Optional Integrations & Packaging Polish (Milestone 11)

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **Recipe Viewer (JEI)** | `verified` | Recipe viewer integration via `ApotheosisJeiPlugin` (`IModPlugin`): custom categories (`SpawnerCategory`, `SalvagingCategory`, `GemCuttingCategory` with purity and basic extensions, `InfusionRecipeCategory`), catalysts, and ingredient info. | Verified with JEI `31.17.0.108` on 26.3; compiles cleanly with zero dedicated server leakage. |
| **Recipe Transfer Handler** | `verified` | `RavenTransferHandler` (`IRecipeTransferHandler<RavenEnchantmentMenu, InfusionRecipe>`): transfers inputs to slot 0 and syncs server-validated `SetRavenStatsPayload` clamping against surrounding bookcases. | Tested transfer bounds (0, 1, 2, 36); server validation in `SetRavenStatsPayload.handleServer`. |
| **GUI Exclusion Zones** | `verified` | Registered JEI exclusion zones for `InventoryScreen` (`AttributesGui` sheet and sword toggle button) and `ApothEnchantmentScreen` (upper info tab). | Tested in `ApotheosisJeiPlugin.registerGuiHandlers`. |
| **HUD Inspection (Jade)** | `verified` | Jade inspection via `ApotheosisJadePlugin` (`IWailaPlugin`): `SpawnerServerDataProvider` and `EnchantingServerDataProvider` on common side; `SpawnerClientProvider` and `EnchantingClientProvider` on client side. | Verified with Jade `26.3.5+fabric` on 26.3; reflection delegation ensures zero client classes loaded on dedicated server. |
| **Accessory Slot Bridge** | `verified` | Open extension point `ModifierSourceType` prevents duplicate attribute applications. Trinkets and Accessories have 0 published 26.3 releases (reported as deferred). `PotionCharmItem` falls back to inventory ticking when absent. | Audited in `ModifierSourceType.java` and `PotionCharmItem.java`; tests pass cleanly. |
| **Tooltip Cooperation** | `verified` | Inline enchantment descriptions in `ApothEnchClient` controlled by `enableInlineEnchDescs` config toggle; yields automatically when standalone description mods (`enchdesc`, `idwtialsimmofg`, `enchantmentdescriptions`) are detected. | Tested in `ApothEnchClient` and `ApotheosisConfigScreen`. |
| **YACL Packaging (JiJ)** | `verified` | Nested YetAnotherConfigLib (YACL `3.9.6+26.3-fabric`) via Jar-in-Jar (`include`) in `build.gradle`. Bundled in `META-INF/jars/` unshaded with manifest and LGPL-3.0 license intact. | Verified via `unzip -l build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar`. |
| **Config Audit & Remote Safety**| `verified` | Audited `ServerConfig`, `ClientConfig`, and `AdventureConfig`. Enforced read-only display on remote servers (`.available(!isRemote)`); cleared remote state on disconnect (`clearRemoteServerConfig`). | Tested in `ApotheosisConfigTest` (all 62 tests pass). |




