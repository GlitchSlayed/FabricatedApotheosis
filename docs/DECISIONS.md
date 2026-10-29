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
