# Future Upgrade Guide — Porting to Future Minecraft Versions (e.g. 26.4)

> [!WARNING]
> This guide outlines the planned engineering procedure for porting Fabricated Apotheosis to future Minecraft versions (such as Minecraft 26.4) when that target is released.
> **Do not write speculative adapters or claim 26.4 compatibility in advance.**

---

## 1. Guiding Principles for Future Porting

1. **Vanilla-First Evaluation**:
   - Before porting or modifying custom mechanics, inspect the target vanilla codebase and Fabric API changes.
   - If vanilla introduces native attributes, data components, or systems that supersede custom mechanisms, evaluate them using `docs/VANILLA_OVERLAP.md` criteria (matching meaning, units, operations, timing, persistence, and synchronization).
2. **Persistent Registry Identifier Invariance**:
   - Never rename or alter existing namespace identifiers (`apotheosis:*`, `apothic_attributes:*`, `apothic_enchanting:*`, `apothic_spawners:*`).
   - All blocks, items, attributes, enchantments, and data components must remain stable to ensure world save continuity.
3. **No Unchecked Schema Churn**:
   - Schema version increments must be accompanied by explicit migration routines in `ConfigMigration.java` or codec parsers.
   - Damaged or unrecognized data tags must be preserved in NBT compounds rather than silently reset.

---

## 2. Step-by-Step Upgrade Procedure

### Step 1: Upstream & Target Discovery
1. Obtain the official target client and server JARs and place them in `reference/` (read-only, excluded from git).
2. Audit target mapping differences (methods, field renames, access widened fields).
3. Update `REFERENCE_INDEX.md` with target build provenance and mapping schemes.

### Step 2: Toolchain & Dependency Audit
1. Update `gradle.properties`:
   - `minecraft_version`
   - `fabric_api_version`
   - `yacl_version`
2. Update optional compile-only integrations:
   - Check Modrinth Maven for compatible releases of `jei` and `jade`.
   - If a dependency is not yet published for the target version, keep the integration code isolated and mark the dependency deferred in `docs/DEPENDENCY_MATRIX.md`.

### Step 3: Mixin and Render Pipeline Verification
1. Inspect any targeted methods in `apotheosis.mixins.json` and client mixins.
2. Verify render state extraction boundaries: ensure extraction occurs on main render thread and custom geometry submissions use high-level Mojang abstractions (`SubmitNodeCollector`, `RenderPipelines`).
3. Audit for zero raw driver (OpenGL/Vulkan) calls.

### Step 4: Verification Against Compatibility Fixtures
1. Execute the persistence test suite:
   ```bash
   ./gradlew test --tests dev.shadowsoffire.apotheosis.persistence.SaveCompatibilityTest
   ```
2. Run the complete automated test suite:
   ```bash
   ./gradlew test --rerun-tasks
   ```
3. Test copied representative world saves created in 26.3:
   - Place world save in run directory.
   - Boot dedicated server with `./gradlew runServer --args="--nogui"`.
   - Verify placed blocks, spawner parameters, library points, affix equipment, and world tiers.
   - Generate new chunks and observe feature generation.
   - Save, shut down (`stop`), restart, and verify persistence round-tripping.

---

## 3. Same-Fork Upgrade Policy

- **Supported Path**: Upgrade from 1.0.0-alpha.1 (Minecraft 26.3) to subsequent versions of this fork.
- **Pre-Upgrade Requirement**: Complete world backup (`saves/` directory) is strictly required prior to launching on a newer release.
- **Downgrades**: Downgrading world saves to older mod versions is unsupported and may result in lost items or invalid block entity states.
