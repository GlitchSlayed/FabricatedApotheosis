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

---

## 2. Upcoming Milestones

| Milestone | Focus | Deliverables | Target |
| :--- | :--- | :--- | :--- |
| **Milestone 7** | Apothic Spawners Core | Port `BaseSpawner` hooks, modifier items, Silk Touch drop table, tooltip deltas | `NEXT` |
| **Milestone 8** | Apothic Adventure Core | Affixes, sockets, gems, boss spawning, RenderPearl overlays | `PENDING` |
| **Milestone 9** | Polish & Full Parity | ModMenu integration, Chronicle of Shadows verification, GameTests | `PENDING` |
