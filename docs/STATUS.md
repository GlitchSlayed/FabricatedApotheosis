# Project Status & Roadmap

## 1. Current Milestone: Task 1 — Discovery, Architecture & Working Skeleton

- **Status**: **COMPLETE**
- **Date**: 2026-10-27
- **Key Achievements**:
  - [x] Cloned and verified upstream 26.1.2 source repositories (`Apotheosis`, `Placebo`, `Apothic-Attributes`, `Apothic-Enchanting`, `Apothic-Spawners`, `GatewaysToEternity`, `Patchouli`).
  - [x] Created `docs/UPSTREAM_LOCK.md` with exact commit SHAs, licenses, and verified cohesion.
  - [x] Created `docs/DEPENDENCY_MATRIX.md` with 6-way dependency classification and scope decisions.
  - [x] Created `docs/PORTING_SURFACE.md` inventorying 632 Java classes across the ecosystem.
  - [x] Created `docs/VANILLA_OVERLAP.md` auditing all 21 custom attributes against vanilla 26.1.2, 26.2, and 26.3.
  - [x] Initialized reproducible Gradle build targeting Minecraft 26.3, Fabric Loader 0.19.5, Loom 1.17.21, and YACL 3.9.6.
  - [x] Created split-source skeleton (`main` and `client`) with clean module boundaries.
  - [x] Implemented server-safe config model (`ApotheosisConfig.java`) and client YACL screen (`ApotheosisConfigScreen.java`).
  - [x] Verified compilation via `./gradlew build` generating `fabricated-apotheosis-1.0.0-alpha.1.jar`.
  - [x] Verified dedicated server launch via `./gradlew runServer --args="--nogui"` (zero client leakage, 100% world load, clean shutdown).

---

## 2. Upcoming Milestones

| Milestone | Focus | Deliverables | Target |
| :--- | :--- | :--- | :--- |
| **Milestone 2** | Apothic Spawners Core | Port `BaseSpawner` hooks, modifier items, Silk Touch drop table, tooltip deltas | `NEXT` |
| **Milestone 3** | Apothic Attributes Core | Port 21 combat attributes, combat rules mixins, overheal, attribute GUI | `PENDING` |
| **Milestone 4** | Apothic Enchanting Core | Access wideners, table stats (Eterna/Quanta/Arcana), level uncapping | `PENDING` |
| **Milestone 5** | Apothic Adventure Core | Affixes, sockets, gems, boss spawning, RenderPearl overlays | `PENDING` |
| **Milestone 6** | Polish & Full Parity | ModMenu integration, Chronicle of Shadows verification, GameTests | `PENDING` |
