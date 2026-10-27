# Architecture Decision Records (ADR)

## ADR-001: Combined Ecosystem Distributable on Fabric

- **Status**: Accepted
- **Context**: Upstream Apotheosis in NeoForge was distributed as separate jars (Apotheosis, Placebo, Apothic Attributes, Apothic Enchanting, Apothic Spawners). Managing multi-jar dependencies on modern Fabric creates fragmentation and installation complexity.
- **Decision**: Deliver a single combined mod distributable (`FabricatedApotheosis`) on Minecraft 26.3 Fabric that bundles Apotheosis, approved Placebo subsets, Apothic Attributes, Apothic Enchanting, and Apothic Spawners into a single cohesive artifact under one mod ID.
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
