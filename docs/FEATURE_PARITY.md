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

| Upstream Feature | Status | Target Scope / Plan |
| :--- | :--- | :--- |
| **Spawner Stat Modifiers** | `investigated` | Modifier items (sugar, clock, ghast tear, blaze rod, etc.) targeting `BaseSpawner.serverTick`. |
| **Redstone Activation Control** | `investigated` | Toggle spawner execution via comparator/redstone power. |
| **Condition Ignore Modifiers** | `investigated` | Ignore light level, ignore nearby players, ignore spawn range checks. |
| **Silk Touch Capture & Drops** | `investigated` | Custom loot table condition + block drops mixin to preserve spawner stats. |
| **Spawner Stat Deltas in Tooltip** | `investigated` | `Item.appendHoverText` hooking into 26.3 `TooltipDisplay`. |
| **Spawn Egg Modification** | `investigated` | Adapt from legacy `setEntityId` to 26.3 `setEntityData(TypedEntityData)`. |

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
