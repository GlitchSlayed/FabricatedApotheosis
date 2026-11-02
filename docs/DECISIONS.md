# Architecture Decision Records (ADR)

## ADR-001: Combined Ecosystem Distributable on Fabric

- **Status**: Accepted
- **Context**: Upstream Apotheosis in NeoForge was distributed as separate jars (Apotheosis, Placebo, Apothic Attributes, Apothic Enchanting, Apothic Spawners). Managing multi-jar dependencies on modern Fabric creates fragmentation and installation complexity.
- **Decision**: Deliver a single combined mod distributable (`fabricated-apotheosis`) on Minecraft 26.3 Fabric that bundles Apotheosis, approved Placebo subsets, Apothic Attributes, Apothic Enchanting, and Apothic Spawners into a single cohesive artifact under mod ID `apotheosis`.
- **Consequences**: Simplified player setup, streamlined classpaths, unified configuration screen via YACL.

---

## ADR-002: Reference Trees Read-Only & Excluded from Build Artifacts

- **Status**: Accepted
- **Context**: Minecraft 26.1.2 and 26.3 reference files are located locally in `Refrences/` (aliased as `reference/`).
- **Decision**:
  - Treat all local reference files as strictly read-only. Do not edit, format, rename, move, or delete them.
  - Strictly exclude them from Git (`.gitignore`), Gradle compile source sets, generated source archives (`sourcesJar`), and distributable jars (`jar`).
  - Provide a symlink `reference -> Refrences` to ensure standard cross-referencing without modifying reference files.
- **Consequences**: Prevents repository bloat and accidental distribution of copyrighted vanilla assets while preserving local inspection access via `javap` and search tools.

---

## ADR-003: Vanilla-First Principle & Removal of Obsolete Mining Speed

- **Status**: Accepted
- **Context**: Upstream Apotheosis implemented custom attributes like `apothic_attributes:mining_speed` when vanilla Minecraft lacked native attributes for block break speed.
- **Decision**: Check vanilla 26.3 and Fabric API before porting custom mechanisms. Upstream's `mining_speed` attribute is obsoleted by vanilla `minecraft:mining_efficiency` and `minecraft:submerged_mining_speed` and will not be reintroduced.
- **Consequences**: Minimizes mixin surface, avoids duplicate attribute application bugs, and improves vanilla compatibility.

---

## ADR-004: Server-Authoritative Logic & Safe Client Separation

- **Status**: Accepted
- **Context**: Modifying spawner parameters, applying modifiers, entity drops, and attribute modifications can cause desync or server crashes if client code is improperly referenced.
- **Decision**:
  - All gameplay logic, spawner ticks, item consumption, and NBT/component writes must be server-authoritative.
  - Client-side code (YACL config screens, tooltips, particle effects, RenderPearl GPU abstractions) must remain strictly segregated in the client source set and never be referenced by dedicated server runtime classes.
- **Consequences**: Guarantees dedicated server stability and multiplayer parity.

---

## ADR-005: Scope Boundary: Gateways to Eternity Completely Out of Scope

- **Status**: Accepted
- **Context**: Gateways to Eternity was historically an optional boss wave mod with custom entity lifecycles, wave progression state, and separate renderers.
- **Decision**: Gateways to Eternity is **completely out of scope**. Remove it from all planned work, dependency requirements, and release criteria.
- **Consequences**: Streamlines project scope, eliminates 43 unrelated Java classes and custom entity renderers from scope, and focuses the port solely on the core Apotheosis ecosystem.

---

## ADR-006: Chronicle of Shadows & Documentation Support Deferred (TBD)

- **Status**: Accepted
- **Context**: The Chronicle of Shadows is authored in Patchouli's book format. Patchouli is licensed under CC-BY-NC-SA 3.0.
- **Decision**: Chronicle of Shadows / Patchouli integration is **deferred (TBD)** and is not an initial release requirement. Do not plan a replacement book system. Book JSON assets remain untouched in `assets/apotheosis/patchouli_books/` without active runtime hooks.
- **Consequences**: Avoids license contamination across the MIT-licensed Apotheosis codebase and removes external book engine blockers from initial release readiness.

---

## ADR-007: YACL Packaging without Shading

- **Status**: Accepted
- **Context**: YetAnotherConfigLib is licensed under LGPL-3.0 and must not be flattened or have its metadata stripped.
- **Decision**: Consume YACL as an external dependency or through Fabric's standard Jar-in-Jar (`include`) mechanism, preserving YACL's distinct `fabric.mod.json`, manifest, and license.
- **Consequences**: Ensures full compliance with LGPL-3.0 and avoids classloader conflicts with other mods using YACL.

---

## ADR-008: Loom Split Environment Source Sets

- **Status**: Accepted
- **Context**: Mixing client-only UI code (such as YACL screens and rendering) with common logic risks `ClassNotFoundException` on dedicated servers.
- **Decision**: Use Fabric Loom's `splitEnvironmentSourceSets()` with `src/main` (common) and `src/client` (client-only).
- **Consequences**: Compiler enforces that common code cannot import client classes, completely preventing dedicated-server crashes.

---

## ADR-009: Placebo Foundation Packaging, Atomic Dynamic Registries, and Interface-Segregated Networking

- **Status**: Accepted
- **Context**: Upstream modules depend on Placebo for dynamic data loading, registration helpers, math/codecs, and networking. Loom's `splitEnvironmentSourceSets()` strictly excludes client classes from `src/main`, and standalone Placebo or Apothic module JARs installed simultaneously would cause duplicate class collisions.
- **Decision**:
  - Produce one single ecosystem JAR under mod ID `apotheosis`, with explicit `"breaks"` in `fabric.mod.json` against conflicting standalone module IDs (`placebo`, `apothic_attributes`, `apothic_enchanting`, `apothic_spawners`).
  - Implement a dedicated Gradle task `verifyNoDuplicateClasses` and `DuplicatesStrategy.FAIL` on all archive tasks.
  - Port only the Placebo subset required by the audited ecosystem into `dev.shadowsoffire.placebo.*` without claiming public Placebo API compatibility via `provides`.
  - Segregate networking into server-safe `PayloadHelper` in `src/main` with `ClientPayloadHandler` implemented in `src/client`, registered upon client initialization.
  - Implement `DynamicRegistry` and `WeightedDynamicRegistry` with atomic reload staging, clear error logging without partial state, and automatic cache invalidation.
- **Consequences**: Dedicated servers boot cleanly without client class leakage, network channels register synchronously via Fabric Networking API v1, dynamic data reloads reliably in multiplayer, and conflicting standalone jars are rejected gracefully at load time.

---

## ADR-010: Configuration Architecture: Typed Schema Separation, Atomic Writes, and Server-Authoritative YACL UI

- **Status**: Accepted
- **Context**: Upstream Apotheosis had scattered configs across Placebo `.cfg` files, with mixed client visual options and server gameplay rules. In multiplayer, client-side configuration screens could mislead players or cause desync if server gameplay rules are editable or not synchronized.
- **Decision**:
  - Separate configuration into two distinct files and schemas:
    1. `apotheosis.json` (`ServerConfig`): Server-authoritative gameplay formulas, limits, and module toggles.
    2. `apotheosis-client.json` (`ClientConfig`): Purely client preferences for visuals, tooltip details, and screen positioning.
  - Implement schema versioning (`schemaVersion = 1`) and automatic migration for older unversioned JSONs and legacy `.cfg` files (`ConfigMigration`).
  - Implement atomic file writes using sibling `.tmp` files with `StandardCopyOption.ATOMIC_MOVE` to prevent corrupted configs on crash.
  - Synchronize authoritative `ServerConfig` over the network on player join (`ApotheosisConfigPayload`), and clear remote settings upon client disconnect (`ClientPlayConnectionEvents.DISCONNECT`) to prevent retaining stale state.
  - In YACL UI (`ApotheosisConfigScreen`), render server gameplay settings as read-only (`.available(false)`) when connected to a remote server, while keeping client preferences fully editable.
  - Tag every option with its impact: `LIVE`, `RELOAD_REQUIRED`, or `RESTART_REQUIRED`.
- **Consequences**: Guarantees server authority in multiplayer, eliminates desync and stale state, ensures zero YACL classes on dedicated server classloaders, and provides a polished, intuitive configuration screen for players.

---

## ADR-011: Apothic Attributes Combat Pipeline & Targeted Damage Hooks

- **Status**: Accepted
- **Context**: Upstream Apothic Attributes in NeoForge relies on NeoForge-specific event hooks (`LivingIncomingDamageEvent`, `LivingDamageEvent.Pre`, `LivingDamageEvent.Post`, `CriticalHitEvent`, `ItemAttributeModifierEvent`) with priority ordering to apply damage scaling, penetration, auxiliary damage, overheal, and life steal. Naively porting this via a single generic damage callback leads to recursion, double application, and order-of-operation violations.
- **Decision**:
  - Implement a dedicated 9-phase damage processing pipeline with narrowly targeted Mixins:
    1. Pre-attack / Dodge (`LivingEntityMixin` `hurtServer` head injection).
    2. Attack scaling & cooldown penalties (`PlayerMixin` `attack` injection).
    3. Critical strikes and over-crit scaling (`PlayerMixin` `attack` critical calculation).
    4. Armor & protection penetration / shred (`CombatRulesMixin` `getDamageAfterAbsorb` & `ProtectionEnchantmentMixin`).
    5. Auxiliary damage (Current HP, Fire, Cold) executed via `DamagePipeline` with explicit recursion flags.
    6. Absorption handling (`LivingEntityMixin` `getDamageAfterMagicAbsorb`).
    7. Health damage (`LivingEntityMixin` post-absorption).
    8. Healing, Life Steal, Overheal, and Ghost Health applied only on confirmed damage dealt.
    9. Post-hit effects & experience scaling (`LivingEntityMixin` `dropExperience`).
  - Guard against: recursive auxiliary damage, healing on canceled damage, double application of armor pierce, and projectile stat re-scaling after reload.
- **Consequences**: Deterministic combat matching upstream formulas and priority ordering, with zero NeoForge event dependencies and guaranteed safety against infinite damage loops.

---

## ADR-012: Apothic Enchanting Architecture, Screen Navigation, and Data Component Alignment

- **Status**: Accepted
- **Context**: Upstream Apothic Enchanting relied on NeoForge Coremods/ASM to bypass hardcoded enchantment level caps, NeoForge-specific recipe ingredients (`neoforge:components`, `neoforge:or`), and older Minecraft GUI screen management APIs.
- **Decision**:
  - Replace Coremods and bytecode ASM with Minecraft 26.3 data-driven enchantment systems, custom data component effects (`dev.shadowsoffire.apothic_enchanting.compat`), and narrow Mixins (`AnvilMenuMixin`, `EnchantmentMixin`, `EnchantmentHelperMixin`).
  - Adopt Fabric Access Widener (`apotheosis.accesswidener`) for container screen layout (`leftPos`, `topPos`, `imageHeight`, `imageWidth`) and enchanting table book models.
  - Align screen navigation with Minecraft 26.3 GUI hierarchy (`Minecraft.getInstance().gui.screen()`, `Minecraft.getInstance().gui.setScreen(...)`).
  - Implement the Enchantment Library with transactional $2^{N-1}$ point conversion and standard Fabric Transfer API (`Storage<ItemVariant>`).
  - Migrate component-based recipe ingredients to `"fabric:type": "fabric:components"`.
  - Fix `DeferredHelper` registry collisions by indexing on `ResourceKey<?>` (`(RegistryKey, Identifier)`) instead of bare `Identifier`, safely supporting items and blocks sharing the same ID.
- **Consequences**: Zero legacy ASM/coremods, full compatibility with 26.3 data-driven enchantments and recipes, clean dedicated-server loading, and thread-safe dynamic registry reloading.

---

## ADR-013: Fork-Internal Save Compatibility & Upgrade Policy

- **Status**: Accepted
- **Context**: Upstream mod migrations often attempt broad backwards compatibility across different mod loaders, third-party forks (such as Zenith), and legacy `.cfg` configs. Attempting unverified migrations from external ecosystems introduces instability and unpredictable data loss.
- **Decision**:
  - Migration from upstream Apotheosis, Zenith, other ports, legacy configs, or unrelated older versions is **not required**.
  - Strict backward compatibility **is required** between future versions of **this Fabric fork** and worlds created using supported earlier versions of this fork.
  - Preparing for a future Minecraft 26.4 port means maintaining stable saved data and testing upgrades when the actual target is available. Compatibility with 26.4 will not be claimed in advance.
  - Existing migration code in the codebase remains untouched pending future review and removal.
  - Enforce explicit schema versioning, preserve all state across upgrades (items, affixes, gems, library points, spawners), prevent silent deletion/reset of unresolved data, and require world backups prior to testing. See `docs/SAVE_COMPATIBILITY.md`.
- **Consequences**: Eliminates fragile external legacy migration paths while guaranteeing world persistence integrity and reliable upgrade paths within this Fabric ecosystem.

---

## ADR-014: Enchantment Limits, Coremod Replacements, and Staged Atomic EnchantmentInfo Loading

- **Status**: Accepted
- **Context**: Upstream Apothic Enchanting relied on NeoForge Coremods (ASM) to patch enchantment maximum levels across vanilla commands, fishing, loot, and anvil combining. In addition, `EnchantmentInfoLoader` loaded datapack configurations by clearing live state before parsing, leading to corrupted or partial runtime state if a datapack contained syntax errors or failed during parsing.
- **Decision**:
  - Replace NeoForge coremods with targeted Fabric Mixins:
    1. `AnvilMenuMixin`: Modify constant `40` at `ordinal = 2` with `require = 1` to lift the vanilla "Too Expensive!" level cap to `Integer.MAX_VALUE` when `enableEnchanting` is active, and redirect `Enchantment.getMaxLevel()` to `EnchHooks.getMaxAnvilCombineLevel(ench)`.
    2. `EnchantCommandMixin`: Redirect `Enchantment.getMaxLevel()` in `/enchant` to `EnchHooks.getMaxLevel(ench)`.
    3. `FishingHookMixin`: Replace `FishingHookLureTransformer` by redirecting `timeUntilLured` to `EnchHooks.getTicksCaughtDelay(hook)`.
    4. `EnchantRandomlyFunctionMixin` & `SingleEnchantmentMixin`: Redirect `getMaxLevel()` during loot generation to `EnchHooks.getMaxLootLevel(ench)`.
  - Re-architect `EnchantmentInfoLoader` with staged validation and atomic publication:
    - Parse into an isolated `Map<ResourceLocation, EnchantmentInfo> staged` map without modifying the live `INFO_MAP`.
    - Support datapack stack semantics: `replace: true`, `remove: [...]`, and entry-level `remove: true`.
    - Publish atomically via `INFO_MAP = Map.copyOf(staged)` only upon full validation success; on error, log diagnostics and retain previous valid state.
    - Synchronize authoritative definitions to clients on join via `EnchantmentInfoPayload` and clean up on disconnect.
    - Invalidate enchantment caches via `EnchHooks.clearEnchantmentCaches()`.
- **Consequences**: Zero ASM or coremod bytecode manipulators, deterministic enchantment limits up to configured maximums, zero partial data corruptions during hot-reloads, and dedicated-server authority in multiplayer.

---

## ADR-015: Apothic Spawners: In-Place Vanilla Spawner Replacement, Instability Cascade, and Origin-Tagged Despawn Rules

- **Status**: Accepted
- **Context**: In upstream NeoForge, Apothic Spawners replaced the spawner block and tile entity, patched `BaseSpawner` fields, and hooked into NeoForge's `EntityJoinLevelEvent` / `Mob.getSpawnType()` to manage despawning. In Minecraft 26.3 Fabric, `Blocks.SPAWNER`, `Items.SPAWNER`, and `BlockEntityTypes.MOB_SPAWNER` are statically initialized in `<clinit>`, and vanilla does not track `SpawnType` on `Mob` instances after spawning.
- **Decision**:
  - Replace vanilla `Blocks.SPAWNER`, `Items.SPAWNER`, and `BlockEntityTypes.MOB_SPAWNER` in-place during class initialization using targeted Mixins (`BlocksMixin`, `BlockEntityTypesMixin`, `ItemsMixin`), ensuring identical registry keys (`minecraft:spawner`, `minecraft:mob_spawner`) and zero item or tile ID migration issues.
  - Implement explosive instability via `BlockBehaviour.onExplosionHit` rather than block destruction: when an explosion affects an `ApothSpawnerBlock`, snapshot the current `nextSpawnData`, cancel immediate drop destruction, and initiate a 60-tick countdown (`unstableTicks`) that culminates in an 8-radius explosion, 12-entity cluster spawn, and `apothic_spawners:gameplay/unstable_spawner` loot drops.
  - Manage despawning by stamping spawner-spawned entities with a persistent data tag (`apothic_spawners:from_spawner`). Intercept `Mob.checkDespawn()` via `MobDespawnMixin` to cancel premature despawning while `tickCount <= entityDespawnDelay` (default: 600 ticks) without globally altering vanilla despawn behavior or bypassing Peaceful difficulty checks.
  - Simulate lighting conditions for the `ignore_light` stat via `LyingLevel` (`ServerLevelAccessor` decorator) that evaluates candidate positions across light levels $0 \dots 15$, rather than patching entity spawn rules directly.
  - Synchronize spawner configuration parameters dynamically through `ApotheosisConfig.getServer()` / `ServerConfig`, ensuring server-authoritative enforcement across multiplayer sessions.
- **Consequences**: Flawless save compatibility with vanilla spawners, deterministic upgrade consumption, safe explosion cascading with chunk reload persistence, and zero global despawn side effects on natural spawns.

---

## ADR-016: Apothic Adventure Item & Economy Systems Architecture

- **Status**: Accepted
- **Context**: In upstream NeoForge, the Apotheosis Adventure module relied on NeoForge-specific event busses (`ItemAttributeModifierEvent`), NeoForge capability-based inventory transfer handlers, custom item decorators, and Forge-specific ingredient abstractions. Porting to Minecraft 26.3 Fabric requires maintaining 100% item data persistence and mathematical behavior without NeoForge core libraries.
- **Decision**:
  - Implement dynamic attribute modifier derivation via `ItemStackMixin` targeting `forEachModifier(EquipmentSlot, BiConsumer)` and `forEachModifier(EquipmentSlotGroup, TriConsumer)`. This derives live modifiers from persistent affix (`ItemAffixes`) and socket (`SocketedGems`) data components during vanilla calculation passes, guaranteeing modifier recomputation on item changes and preventing persistent modifier duplication.
  - Store all affix, socket, purity, rarity, and custom affix naming metadata using vanilla Minecraft 26.3 `DataComponents` registered via `DeferredHelper` with strict stream codecs for network synchronization.
  - Implement server-authoritative item conservation across all workstations (Reforging, Salvaging, Augmenting, Gem Cutting): validate item recipes and input requirements before applying modifications, and integrate workstation automation using Fabric Transfer API (`ItemStorage.SIDED`).
  - Provide non-instrumented fallbacks in `CachedObject` and `DeferredHelper` so that headless unit tests run safely in standard test JVMs without requiring active Loom mixin bytecode instrumentation.
- **Consequences**: Deterministic item and socket persistence, full save compatibility with this fork's schema policy, clean multiplayer synchronization, and zero duplicate modifier accumulation bugs.

---

## ADR-017: Progression Pacing, Dynamic Augment Recalculation, and Fabric Biome Worldgen Integration

- **Status**: Accepted
- **Context**: In upstream NeoForge, Apotheosis progression integrated across multiple standalone modules and used NeoForge-specific BiomeModifiers (`BlacklistModifier`), NeoForge `EntityJoinLevelEvent`, and NeoForge network payloads to manage World Tiers, boss spawning, and structure generation. In combined mode, cheaper standalone spawner upgrade recipes risked bypassing tier progression if not reconciled, and biome generation required adapting to Minecraft 26.3 `Registries.FEATURE` and Fabric Biome API v1.
- **Decision**:
  - Implement World Tiers (`WorldTier`) as a server-authoritative attachment (`Attachments.WORLD_TIER`), governed by vanilla advancements (`Apoth.Advancements`), synchronized to clients via `WorldTierPayload`, and managed by `/apotheosis world_tier`.
  - Reconcile standalone and progression spawner recipes via `SpawnerRecipeCache` and `SpawnerModifier`: when `enableAdventure` is active, suppress standalone `apothic_spawners` modifier recipes, enforcing tiered spawner runes and preventing cheaper recipes from bypassing tier gating.
  - Recalculate tier augments dynamically via `TierAugmentRegistry`: on player tier changes, deaths, respawns (`ServerPlayerEvents.COPY_FROM`), joins (`ServerPlayConnectionEvents.JOIN`), and dimension changes (`ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL`), remove previous tier modifiers and apply the current tier modifiers to avoid modifier accumulation.
  - Implement Minecraft 26.3 `Feature` architecture for `BossDungeonFeature`, `BossDungeonFeature2`, and `RogueSpawnerFeature`, registering their `MapCodec` entries in `BuiltInRegistries.FEATURE_TYPE` and placing features via Fabric Biome API v1 (`BiomeModifications.addFeature`), fully replacing NeoForge `BiomeModifier` JSONs.
  - Migrate structure template entity processing via `StructureTemplateMixin` to run `ItemFrameGemsProcessor`, decorating Tome Tower item frames with tiered gems and purity without NeoForge structure modifiers.
  - Update loot table definitions to Minecraft 26.3 schema where `TagEntry` uses `"items": "#tag"` rather than `"name": "tag"`.
- **Consequences**: Seamless end-to-end progression across all modules, zero modifier stacking on tier transitions, clean world generation in compliance with 26.3 registry structures, and full save compatibility.

---

## ADR-018: Client UI Interactions, Rendering Decoupling, and Custom Geometry Architecture

- **Status**: Accepted
- **Context**: In upstream NeoForge 26.1, client rendering for screens and entity flair (dropped affix item beams and colored shadows) utilized Forge/NeoForge render stage events (`RenderLevelStageEvent`), raw OpenGL state manipulation, and direct Forge localization keys. In Minecraft 26.3 Fabric, rendering has been strictly decoupled into state extraction (`extractRenderState`) and geometry submission (`submit`), and standard rendering must use Mojang high-level abstractions (`SubmitNodeCollector`, `RenderPipelines`) without raw GL or Vulkan driver calls.
- **Decision**:
  - Implement dropped affix item visual effects (beacon beams and shadows) by extending `ItemEntityRenderState` with interface `AffixItemRenderState` via `ItemEntityRenderStateMixin`. Extract `RarityRenderData`, color, and ground state in `ItemEntityRenderer.extractRenderState`, and submit custom geometry during `ItemEntityRenderer.submit` using 26.3 `SubmitNodeCollector.submitCustomGeometry` with `ApothRenderTypes.BEAM` (backed by `RenderTypes.eyes(...)`).
  - Align beacon beam coordinate origins by eliminating legacy block-level $(0.5, 0.5)$ translations in `BeamRenderer`, ensuring vertical loot beams center perfectly on floating and dropped item entities.
  - Rate-limit particle emissions per item entity via entity attachment (`AFFIX_EFFECT_NEXT_PARTICLE_TIME`) within client tick events (`ClientTickEvents.END_CLIENT_TICK`), preventing particle spam and ensuring clean clearance on chunk unload or disconnect.
  - Standardize all client translations on Fabric-native namespace keys (`info.apothic_enchanting.limited_enchantability`, `title.apotheosis.world_tier_tutorial`), removing remaining NeoForge translation references.
  - Enforce hierarchical screen navigation: override `onClose()` on modal and sub-screens (`WorldTierDetailScreen`, `WorldTierTutorialScreen`, `EnchantingInfoScreen`) to cleanly restore the parent menu upon ESC keypress.
  - Protect container interactions: fix input slot loop bounds in `SalvagingScreen` ($i < 12$), suppress inventory key closes when text filter boxes have focus, and provide dynamic recipe-viewer exclusion bounds (`getExclusionAreas()`) for non-standard panels like the Gem Case upgrade drawer.
  - Add client configuration toggle ("Enable Affix Item Effects") to `ClientConfig` and YACL `ApotheosisConfigScreen` with live runtime updates.
- **Consequences**: Flawless visual coherence across all GUI scales and window sizes, zero raw GPU/driver calls, strict adherence to 26.3 render state extraction lifecycles, and zero client leakage into dedicated server execution.

---

## ADR-019: Optional Integrations (JEI, Jade, ModMenu) and Packaging Strategy

- **Status**: Accepted
- **Context**: In upstream NeoForge 26.1, optional integrations with recipe viewers (JEI/EMI), block inspection HUDs (Jade/Waila), accessory slots (Curios), and configuration libraries (YACL) relied on NeoForge-specific plugin annotations and loose jar distribution. On Minecraft 26.3 Fabric, third-party libraries require strict handling: YACL must preserve LGPL licensing without shading; JEI and Jade must be optional compile-only dependencies with zero dedicated server classloading leakage; and absent accessory/recipe mods must degrade gracefully without crashing or duplicating attribute modifiers.
- **Decision**:
  - **Single JAR Packaging**: Include YetAnotherConfigLib (YACL) `3.9.6+26.3-fabric` via Fabric Loom's `include` (Jar-in-Jar) configuration. YACL is bundled inside `META-INF/jars/` completely unshaded, with its original `fabric.mod.json`, JAR manifest, and LGPL-3.0 license metadata intact.
  - **Recipe Viewer Integration (JEI)**: Implement custom recipe categories (`SpawnerCategory`, `SalvagingCategory`, `GemCuttingCategory`, `InfusionRecipeCategory`) implementing `IRecipeCategory`. Register custom catalysts, recipe caches, ingredient info (spawner silk touch behavior, gems, sigils, libraries), and GUI exclusion zones (`AttributesGui` on `InventoryScreen`, info tab on `ApothEnchantmentScreen`).
  - **Server-Authoritative Recipe Transfer**: Implement `RavenTransferHandler` (`IRecipeTransferHandler<RavenEnchantmentMenu, InfusionRecipe>`) to transfer infusion recipe items to slot 0 and dispatch `SetRavenStatsPayload` to the server, which validates and clamps slider stats against surrounding bookcases before applying.
  - **Jade Inspection Providers**: Register `SpawnerServerDataProvider` and `EnchantingServerDataProvider` on the common side (`src/main`) and delegate client component rendering to `SpawnerClientProvider` and `EnchantingClientProvider` on the client side (`src/client`) using dynamic reflection across environments to guarantee zero client classloading on dedicated servers.
  - **Accessory & Equipment Safety**: Maintain `ModifierSourceType` as an open decoupled extension point. Because Trinkets and Accessories have 0 releases published for Fabric 26.3, mark them as deferred. Ensure `PotionCharmItem` falls back to inventory ticking when accessory mods are absent, and ensure equipment bonuses cannot be duplicated when items are inspected across multiple slots.
  - **Tooltip Cooperation**: Implement inline enchantment descriptions in `ApothEnchClient` controlled by `enableInlineEnchDescs` config toggle, automatically yielding if standalone description mods (`enchdesc`, `idwtialsimmofg`, `enchantmentdescriptions`) are detected.
  - **Configuration Polish**: Expose all client preferences and server gameplay rules via YACL with clear descriptions and impact tags. Disable server-authoritative options with `.available(!isRemote)` when connected to remote servers, and restore local singleplayer configuration cleanly on `ClientPlayConnectionEvents.DISCONNECT`.
- **Consequences**: Robust optional integrations that never crash when dependencies are absent, full compliance with Fabric packaging standards, verified server-side validation on all recipe transfers, and pristine dedicated server safety.



