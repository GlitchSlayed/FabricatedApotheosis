# Project Status & Roadmap

## 1. Current Milestone: Prompt 1 — Discovery & Baseline Architecture

- **Status**: **COMPLETE**
- **Date**: 2026-10-27
- **Accomplishments**:
  - [x] Initialized Git repository and configured strict exclusion rules in `.gitignore` for `reference/` and `Refrences/`.
  - [x] Maintained reference trees in read-only condition and established canonical symlink `reference -> Refrences`.
  - [x] Completed full layout inspection and generated `docs/REFERENCE_INDEX.md` cataloging exact paths, hashes, versions, mapping scheme (MojMap), and search commands.
  - [x] Merged persistent project instructions into `AGENTS.md` and linked `Agents.md`.
  - [x] Performed vanilla 26.1.2 vs 26.3 comparison using local reference trees (`javap` bytecode analysis).
  - [x] Created `docs/UPSTREAM_LOCK.md`, `docs/DEPENDENCY_MATRIX.md`, `docs/VANILLA_OVERLAP.md`, `docs/FEATURE_PARITY.md`, `docs/DECISIONS.md`.
  - [x] Created comprehensive `docs/HANDOFF_REPORT.md`.

---

## 2. Roadmap

| Milestone | Focus | Key Deliverables | Status |
| :--- | :--- | :--- | :--- |
| **Prompt 1** | Discovery & Reference Setup | Reference Index, Vanilla Overlap Audit, Repo Hygiene, Project Docs | **DONE** |
| **Prompt 2** | Build Environment Initialization | Gradle / Fabric Loom 26.3, Fabric API, YACL integration, Mod Entrypoint | `NEXT` |
| **Prompt 3** | Spawner Core & Modifiers | `BaseSpawner` mixin, SpawnerModifierItems, Spawner component serialization | `PENDING` |
| **Prompt 4** | Spawner Harvesting & Persistence | Silk Touch loot tables, drops mixin, BlockEntity save/load hooks | `PENDING` |
| **Prompt 5** | Apothic Attributes Foundation | Attribute registrations, tooltip formatting, entity attributes | `PENDING` |
| **Prompt 6** | Apothic Enchanting Foundation | Enchanting table stats, capturing enchantment, custom tomes | `PENDING` |
| **Prompt 7** | Config & In-Game UI | YACL configuration screens, client/server config synchronization | `PENDING` |
| **Prompt 8** | Integration & Parity Verification | Gametests, client & dedicated server launch verification, parity audit | `PENDING` |
