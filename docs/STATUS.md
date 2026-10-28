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

---

## 2. Upcoming Milestones

| Milestone | Focus | Deliverables | Target |
| :--- | :--- | :--- | :--- |
| **Milestone 4** | Apothic Spawners Core | Port `BaseSpawner` hooks, modifier items, Silk Touch drop table, tooltip deltas | `NEXT` |
| **Milestone 5** | Apothic Attributes Core | Port 21 combat attributes, combat rules mixins, overheal, attribute GUI | `PENDING` |
| **Milestone 6** | Apothic Enchanting Core | Access wideners, table stats (Eterna/Quanta/Arcana), level uncapping | `PENDING` |
| **Milestone 7** | Apothic Adventure Core | Affixes, sockets, gems, boss spawning, RenderPearl overlays | `PENDING` |
| **Milestone 8** | Polish & Full Parity | ModMenu integration, Chronicle of Shadows verification, GameTests | `PENDING` |
