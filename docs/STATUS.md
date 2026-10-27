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

---

## 2. Upcoming Milestones

| Milestone | Focus | Deliverables | Target |
| :--- | :--- | :--- | :--- |
| **Milestone 3** | Apothic Spawners Core | Port `BaseSpawner` hooks, modifier items, Silk Touch drop table, tooltip deltas | `NEXT` |
| **Milestone 4** | Apothic Attributes Core | Port 21 combat attributes, combat rules mixins, overheal, attribute GUI | `PENDING` |
| **Milestone 5** | Apothic Enchanting Core | Access wideners, table stats (Eterna/Quanta/Arcana), level uncapping | `PENDING` |
| **Milestone 6** | Apothic Adventure Core | Affixes, sockets, gems, boss spawning, RenderPearl overlays | `PENDING` |
| **Milestone 7** | Polish & Full Parity | ModMenu integration, Chronicle of Shadows verification, GameTests | `PENDING` |
