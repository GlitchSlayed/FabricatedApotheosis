# Handoff Report — Milestone: Prompt 1 Discovery & Reference Baseline

## 1. Executive Summary

Milestone **Prompt 1** has been executed in full compliance with `AGENTS.md` and user instructions:
- Applied and merged persistent project instructions into `AGENTS.md` and synchronized `Agents.md`.
- Maintained existing local reference trees in `Refrences/` in read-only condition and established canonical symlink `reference -> Refrences`.
- Configured `.gitignore` to strictly exclude all reference trees, build outputs, and IDE caches from Git.
- Created `docs/REFERENCE_INDEX.md` detailing the layout, hashes, build identities, MojMap mappings, client/server coverage, search tooling, and uncertainties.
- Executed vanilla comparison between Minecraft 26.1.2 and 26.3 using local reference trees (`javap` bytecode disassembler).
- Documented findings in `docs/UPSTREAM_LOCK.md`, `docs/DEPENDENCY_MATRIX.md`, `docs/VANILLA_OVERLAP.md`, `docs/FEATURE_PARITY.md`, `docs/DECISIONS.md`, `docs/STATUS.md`, and this report.

---

## 2. REFERENCE_INDEX Findings

Detailed findings from inspecting the local reference trees in `Refrences/` (aliased as `reference/`):

### 2.1 File Catalog & Checksums
- **Minecraft 26.1.2 Client JAR**:
  - Path: `reference/26.1.2client.jar` (`Refrences/26.1.2client.jar`)
  - Size: 38,113,927 bytes
  - SHA-256: `b1b3158572666445eff01e82fad8c7de2e4953db6d354f311730d77a8359d0b0`
  - Total entries: 30,675 (10,682 `.class`, 13,616 `.json`, 10,669 assets, 8,777 data)
- **Minecraft 26.3 Client JAR**:
  - Path: `reference/26.3client.jar` (`Refrences/26.3client.jar`)
  - Size: 41,483,720 bytes
  - SHA-256: `4508d006323f24fa02876310c192d739af56516eb259000ac50f0909a68c9a2d`
  - Total entries: 34,227 (11,383 `.class`, 15,699 `.json`, 11,813 assets, 10,449 data)
- **Extracted 26.3 Class Subset**:
  - Path: `reference/net/` (`Refrences/net/`)
  - 525 `.class` files extracted directly from `26.3client.jar` (covering `advancements`, `world/item`, `world/level/BaseSpawner.class`, and `world/level/block` with `entity`).
  - Byte-identical SHA-256 verified against `26.3client.jar`.
- **Fabric Documentation & Examples**:
  - Path: `reference/fabric-docs-main/` (`Refrences/fabric-docs-main/`)
  - Reference mod implementations for Loom, split environment source sets, gametests, and access wideners.

### 2.2 Verified Version & Build Identifiers
- **Minecraft 26.1.2**: World Version `4790`, Protocol `775`, Build Date `2026-04-09T10:11:03+00:00`, Java 25 (`java-runtime-epsilon`), Stable release.
- **Minecraft 26.3**: World Version `5023`, Protocol `777`, Build Date `2026-09-15T11:20:48+00:00`, Java 25 (`java-runtime-epsilon`), Stable release.

### 2.3 Mappings & Provenance
- Built with **Mojang Official Mappings (MojMap)**.
- Packages follow `net.minecraft.*` and `com.mojang.*`.
- Method and field names are de-obfuscated standard Mojang identifiers (`serverTick`, `spawnDelay`, `Attributes.MINING_EFFICIENCY`).

### 2.4 Client & Server Coverage
- Client JARs bundle both the client rendering/UI subsystem and the integrated server engine (`net.minecraft.server.*`, `MinecraftServer.class`, `ServerLevel.class`, all world and gameplay logic).
- Standalone dedicated server launcher (`net.minecraft.server.Main`) is absent from client jars, but all server logic is present.

### 2.5 Limitations & Uncertainties
- Reference JARs contain compiled Java 25 bytecode (`.class`), not decompiled `.java` source files.
- NeoForge-specific patches and event classes are not present in vanilla jars; upstream mod behavior depending on NeoForge hooks must be referenced from upstream mod sources and mapped to Fabric API equivalents.

---

## 3. Vanilla Comparison Findings (26.1.2 vs 26.3)

Using local `javap` inspection of both reference JARs:

1. **Spawner Architecture**:
   - `BaseSpawner` in 26.3 introduced `setEntityData(TypedEntityData<EntityType<?>>, Level, RandomSource, BlockPos)` and `SET_DISPLAY_ENTITY_ID` (`EntityProcessor`), adapting from the legacy `setEntityId` method.
   - `SpawnerBlockEntity` added corresponding `setEntityData` method.
   - Porting decision: Modifiers altering entity types must target `TypedEntityData`.
2. **Attributes System**:
   - 26.1.2 has 37 attributes; 26.3 has 42 attributes (+5 attributes: `AIR_DRAG_MODIFIER`, `BELOW_NAME_DISTANCE`, `BOUNCINESS`, `FRICTION_MODIFIER`, `NAME_TAG_DISTANCE`).
   - Vanilla natively provides `Attributes.MINING_EFFICIENCY` and `Attributes.SUBMERGED_MINING_SPEED`.
   - Porting decision: Obsolete custom `apothic_attributes:mining_speed` attribute is confirmed unnecessary and completely removed in favor of vanilla `MINING_EFFICIENCY`.
3. **Data Components**:
   - Expanded from 112 to 124 components in 26.3.
   - Includes native fuel definitions (`COOKING_FUEL`, `BREWING_FUEL`), compostability (`COMPOSTABLE`), and weapon animation overrides (`ATTACK_ANIMATION`, `INTERACT_ANIMATION`).
   - Porting decision: Replace legacy Placebo hooks with native 26.3 DataComponents.
4. **Tooltips**:
   - Governed by `TooltipDisplay` and `Item.TooltipContext` in 26.3.
   - Porting decision: Adapt item tooltips to use 26.3 hover text parameters.

---

## 4. Repository & Artifact Hygiene

- Initialized Git repository on `main` branch.
- Created `.gitignore` excluding `Refrences/`, `reference`, `build/`, `.gradle/`, and IDE metadata.
- Ensured reference trees remain read-only without modifying any original files.
- Configured exclusion policies so Gradle builds and distribution archives will not bundle reference sources or classes.

---

## 5. Changed Files

| File | Action | Description |
| :--- | :--- | :--- |
| `.gitignore` | Created | Ignores reference trees, build outputs, and caches |
| `AGENTS.md` | Created / Merged | Consolidated project instructions and persistent reference rules |
| `Agents.md` | Symlinked | Links to `AGENTS.md` for dual filename compatibility |
| `reference` | Symlinked | Canonical link pointing to `Refrences/` without mutating original files |
| `docs/REFERENCE_INDEX.md` | Created | Detailed index of reference trees, paths, hashes, commands, and coverage |
| `docs/UPSTREAM_LOCK.md` | Created | Pins upstream 26.1.2 NeoForge and target 26.3 Fabric baselines |
| `docs/DEPENDENCY_MATRIX.md` | Created | Approved ecosystem modules and third-party dependencies |
| `docs/VANILLA_OVERLAP.md` | Created | Deep vanilla comparison (26.1.2 vs 26.3) and keep/replace/adapt decisions |
| `docs/FEATURE_PARITY.md` | Created | Feature status matrix for all modules |
| `docs/DECISIONS.md` | Created | Architecture Decision Records (ADRs 001 - 004) |
| `docs/STATUS.md` | Created | Roadmap and milestone progress tracking |
| `docs/HANDOFF_REPORT.md` | Created | Milestone completion report and findings summary |

---

## 6. Verification Results

| Check | Tool / Method | Outcome |
| :--- | :--- | :--- |
| Git Exclusion Check | `git status` | Pass: `Refrences/` and `reference` excluded from git tracking |
| Reference File Check | `sha256sum`, `unzip -l`, `python3` | Pass: Checksums and structure verified for both JARs and class trees |
| Version Verification | `version.json` extraction | Pass: 26.1.2 (build 2026-04-09) and 26.3 (build 2026-09-15) confirmed |
| Bytecode Inspection | `javap -p -cp <jar>` | Pass: Verified class structures, attributes, and spawner method changes |
| Reference Integrity | Filesystem checks | Pass: No reference files edited, moved, or deleted |

---

## 7. Remaining Limitations

- Upstream Apotheosis mod source code (Java source of Apotheosis, Apothic Spawners, etc.) is not yet checked into this workspace; it will be introduced during the subsequent implementation milestones.
- Reference JARs contain compiled `.class` files rather than `.java` source code, requiring `javap` or Loom remapped sources for inspection.

---

## 8. Next Concrete Implementation Task

**Prompt 2**: Initialize the Minecraft 26.3 Fabric Gradle build environment:
1. Configure `build.gradle`, `settings.gradle`, and `gradle.properties` targeting Minecraft 26.3, Fabric Loader, Fabric API, and YACL.
2. Configure Loom environment split source sets (`main` and `client`).
3. Create the mod entrypoints and base metadata (`fabric.mod.json`).
4. Verify project compilation and execution of `./gradlew build`.
