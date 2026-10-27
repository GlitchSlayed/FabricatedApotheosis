# Reference Trees Index — Minecraft 26.1.2 & 26.3

## Overview

This document catalogs the local Minecraft reference files available in the workspace, their verified identity, mapping scheme, client/server coverage, search tooling, and noted limitations.

These reference trees are read-only and excluded from version control, build distributions, and Gradle source sets.

---

## 1. Exact Paths

| Asset / Tree | Filesystem Path | Workspace Alias Path | Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- | :--- |
| **Minecraft 26.1.2 Client JAR** | `/home/vansh/Documents/projects/FabricatedApotheosis/Refrences/26.1.2client.jar` | `reference/26.1.2client.jar` | 38,113,927 bytes | `b1b3158572666445eff01e82fad8c7de2e4953db6d354f311730d77a8359d0b0` |
| **Minecraft 26.3 Client JAR** | `/home/vansh/Documents/projects/FabricatedApotheosis/Refrences/26.3client.jar` | `reference/26.3client.jar` | 41,483,720 bytes | `4508d006323f24fa02876310c192d739af56516eb259000ac50f0909a68c9a2d` |
| **Extracted 26.3 Class Subset** | `/home/vansh/Documents/projects/FabricatedApotheosis/Refrences/net/` | `reference/net/` | 525 `.class` files | Matches `26.3client.jar` entries |
| **Fabric Documentation & Examples**| `/home/vansh/Documents/projects/FabricatedApotheosis/Refrences/fabric-docs-main/` | `reference/fabric-docs-main/` | Directory tree | Upstream Fabric documentation repository |

> [!NOTE]
> The directory on disk is named `Refrences/`. To support standard project path conventions and `AGENTS.md` instructions, a symlink `reference -> Refrences` is maintained at workspace root. Neither path modifies existing reference files.

---

## 2. Verified Version and Build Information

### Minecraft 26.1.2 (`26.1.2client.jar`)
Verified via internal `version.json`:
- **ID / Name**: `26.1.2`
- **World Version**: `4790`
- **Series ID**: `main`
- **Protocol Version**: `775`
- **Pack Version**:
  - Resource: Major `84`, Minor `0`
  - Data: Major `101`, Minor `1`
- **Build Timestamp**: `2026-04-09T10:11:03+00:00`
- **Java Target**: Java 25 (`java-runtime-epsilon`)
- **Release Channel**: `stable` (production release)

### Minecraft 26.3 (`26.3client.jar`)
Verified via internal `version.json`:
- **ID / Name**: `26.3`
- **World Version**: `5023`
- **Series ID**: `main`
- **Protocol Version**: `777`
- **Pack Version**:
  - Resource: Major `97`, Minor `1`
  - Data: Major `121`, Minor `0`
- **Build Timestamp**: `2026-09-15T11:20:48+00:00`
- **Java Target**: Java 25 (`java-runtime-epsilon`)
- **Release Channel**: `stable` (production release)

### Extracted Class Subset (`reference/net/`)
- Contains 525 compiled `.class` files.
- Bytecode analysis confirms Java 25 class format (Major version 69, minor 0).
- Checksums verified byte-identical against `26.3client.jar` (e.g. `BaseSpawner.class` SHA-256 `1c8ce2496703b01c8522cacd15923b78baa977a0d2cbbe968d31a80aee64ccb5`).

---

## 3. Source Provenance & Mapping Information

- **Provenance**: Official Mojang production client distributions, digitally signed (`META-INF/MOJANGCS.SF`, `MOJANGCS.RSA`).
- **Mapping / Naming Scheme**: **Mojang Official Mappings (MojMap)**.
  - Class names, package structures, method names, and field names follow Mojang's official nomenclature:
    - `net.minecraft.world.level.BaseSpawner`
    - `net.minecraft.world.level.block.entity.SpawnerBlockEntity`
    - `net.minecraft.world.entity.ai.attributes.Attributes`
    - `net.minecraft.core.component.DataComponents`
- **Extracted Tree (`reference/net`)**:
  - Specific focused subset covering:
    - `net/minecraft/advancements/` (3 files: `Advancement`, `AdvancementType`, `DisplayInfo`)
    - `net/minecraft/world/item/` (4 files: `Item`, `TooltipFlag`, `Item$TooltipContext`, `component/TooltipDisplay`)
    - `net/minecraft/world/level/BaseSpawner.class`
    - `net/minecraft/world/level/block/` (517 files, covering block entities, blocks, states)

---

## 4. Client and Server Coverage

- **Coverage Type**: Client Distribution JAR with Integrated Server.
  - Contains all client-side code (`net.minecraft.client.*`, UI, renderer, sound engine, models, shaders).
  - Contains shared server logic (`net.minecraft.server.*`, `net.minecraft.server.MinecraftServer`, `net.minecraft.server.level.ServerLevel`).
  - Contains gameplay simulation (`net.minecraft.world.*`, entities, blocks, items, recipes, advancements, criteria).
  - Contains data generators and data pack assets (`data/minecraft/`, `assets/minecraft/`).
- **Dedicated Server Limitations**:
  - The standalone dedicated server bootstrap (`net.minecraft.server.Main`) is packaged in Mojang's server distribution. However, all core server logic is identical and bundled in this client JAR.
  - For porting and Fabric development, Fabric Loader maps and runs common code across both client and dedicated server environments.

---

## 5. Useful Targeted Search Commands

Use these exact CLI commands to inspect classes and assets without unpacking or altering the reference files:

### Inspect Method & Field Signatures
```bash
# Inspect class members in Minecraft 26.3
javap -p -cp reference/26.3client.jar net.minecraft.world.level.BaseSpawner

# Inspect class members in Minecraft 26.1.2 (upstream reference)
javap -p -cp reference/26.1.2client.jar net.minecraft.world.level.BaseSpawner
```

### Inspect Bytecode Instructions
```bash
# View disassembled bytecode implementation of a specific method
javap -c -p -cp reference/26.3client.jar net.minecraft.world.level.BaseSpawner
```

### Search for Classes by Pattern
```bash
# Find classes matching a pattern in 26.3
unzip -l reference/26.3client.jar | grep -i "Spawner"

# Find classes matching a pattern in 26.1.2
unzip -l reference/26.1.2client.jar | grep -i "Attribute"
```

### Read Vanilla Data Packs & Assets
```bash
# Read a block loot table
unzip -p reference/26.3client.jar data/minecraft/loot_table/blocks/spawner.json

# Read an item model definition
unzip -p reference/26.3client.jar assets/minecraft/items/spawner.json
```

### Query Extracted Class Files Directly
```bash
javap -p reference/net/minecraft/world/level/block/entity/SpawnerBlockEntity.class
```

---

## 6. Missing Source & Uncertainties

1. **Bytecode vs Java Source**:
   - The reference trees contain compiled `.class` files, not decompiled `.java` source files.
   - Exact signatures and bytecode operations are fully inspectable via `javap`.
   - When full decompiled context is required, Fabric Loom generates remapped sources during Gradle setup.
2. **NeoForge Specific Extensions**:
   - The reference trees are pure vanilla Mojang jars. Upstream Apotheosis 26.1.2 utilized NeoForge patches and hooks (e.g., NeoForge item capabilities, event bus, custom block entity ticker hooks).
   - Any behavior relying on NeoForge extensions must be verified against upstream mod source code and matched to vanilla 26.3 or Fabric API equivalents.
3. **No File Mutation**:
   - In adherence to project guidelines, all reference files are strictly read-only and preserved in their original state.
