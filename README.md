# Fabricated Apotheosis

[![Build](https://github.com/GlitchSlayed/FabricatedApotheosis/actions/workflows/build.yml/badge.svg)](https://github.com/GlitchSlayed/FabricatedApotheosis/actions/workflows/build.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen.svg)](https://www.minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric-0.19.5+-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A faithful port of the **Apotheosis** ecosystem from Minecraft 26.1.2 NeoForge to **Minecraft 26.3 Fabric**, distributed as **one unified ecosystem JAR**.

Fabricated Apotheosis brings all of Apotheosis, required Placebo foundations, Apothic Attributes, Apothic Enchanting, and Apothic Spawners together for Fabric, leveraging modern Minecraft 26.3 rendering abstractions and [YetAnotherConfigLib (YACL)](https://github.com/isXander/YetAnotherConfigLib) for configuration screens.

---

## ✨ Modules & Features

- **Apothic Attributes**:
  - 21 custom combat attributes (Armor Pierce, Armor Shred, Crit Chance, Crit Damage, Overheal, Life Steal, Dodge Chance, Cooldown Reduction, etc.).
  - Interactive attribute inspection screen (`AttributesGui`) accessible directly from the player inventory.
  - Native Fabric attribute formatting supporting percentage, boolean, and flat attributes without bundling NeoForge binaries.
  - Transparent migration of obsolete attributes (e.g. `apothic_attributes:mining_speed` $\rightarrow$ `minecraft:mining_efficiency`).
  - Open extension points for optional accessory slot integrations.

- **Apothic Spawners**:
  - In-world spawner modification with items (speeds, counts, nearby player checks, entity limits).
  - Silk Touch capture and drop preserving full spawner stat state.
  - Redstone activation modes and condition-ignoring modifiers.

- **Apothic Enchanting**:
  - Enchanting table bookshelf stats (Eterna, Quanta, Arcana).
  - Level uncapping and high-tier enchantments.
  - Custom tomes, capturing enchantments, and shelf progressions.

- **Apothic Adventure**:
  - Socketing, gems, affixes, boss monsters, and custom gear.

- **Placebo Foundation**:
  - Bundled internal Placebo support: duplicate-safe registration helpers, dynamic data loading (`DynamicRegistry`), atomic reloads with cache invalidation, and server-authoritative networking (`PayloadHelper`).

- **YACL Configuration Architecture**:
  - Clean separation between server gameplay settings (`config/apotheosis.json`) and client visual preferences (`config/apotheosis-client.json`).
  - In-game YACL configuration UI with live, reload-required, and restart-required impact tagging.
  - Read-only protection for server settings on remote servers with automatic disconnect cleanup to prevent retaining stale state.

---

## 📋 Requirements

| Dependency | Minimum Version | Notes |
| :--- | :--- | :--- |
| **Minecraft** | `~26.3` | Targeted release |
| **Fabric Loader** | `>= 0.19.5` | Standard mod loader |
| **Fabric API** | `>= 0.161.0+26.3` | Required platform API |
| **YetAnotherConfigLib (YACL)** | `>= 3.9.6+26.3-fabric` | Required for configuration UI |
| **Java** | `25` | Modern Java runtime |

*Optional Recommended Mods*: [ModMenu](https://github.com/TerraformersMC/ModMenu) (for in-game config button) and [Patchouli](https://github.com/VazkiiMods/Patchouli) (for Chronicle of Shadows book).

---

## 🛠️ Building from Source

### Prerequisites
- JDK 25 installed and available on `PATH`

### Build Commands

```bash
# Clone the repository
git clone https://github.com/GlitchSlayed/FabricatedApotheosis.git
cd FabricatedApotheosis

# Build the mod and run all automated unit tests
./gradlew build

# The compiled distributable will be located at:
# build/libs/fabricated-apotheosis-1.0.0-alpha.1.jar
```

### Running Test Environments

```bash
# Launch a dedicated server environment
./gradlew runServer --args="--nogui"

# Launch client test environment
./gradlew runClient
```

---

## 📖 Documentation & Architecture

Extensive design, migration, and audit documentation is available in the [`docs/`](docs/) directory:

- [docs/STATUS.md](docs/STATUS.md) — Current development status and milestone progress.
- [docs/FEATURE_PARITY.md](docs/FEATURE_PARITY.md) — Detailed feature parity matrix against upstream 26.1.2.
- [docs/VANILLA_OVERLAP.md](docs/VANILLA_OVERLAP.md) — Audit of vanilla 26.3 overlap and custom attribute resolutions.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — Internal module packaging and single-JAR architecture.
- [docs/DECISIONS.md](docs/DECISIONS.md) — Architecture decision records (ADRs).
- [docs/HANDOFF_REPORT.md](docs/HANDOFF_REPORT.md) — Latest technical verification report.

---

## ⚖️ Credits & Licensing

- **Original Mod**: Created by **Shadows_of_Fire** and contributors:
  - [Apotheosis (NeoForge)](https://github.com/Shadows-of-Fire/Apotheosis)
  - [Placebo](https://github.com/Shadows-of-Fire/Placebo)
  - [Apothic Attributes](https://github.com/Shadows-of-Fire/Apothic-Attributes)
  - [Apothic Enchanting](https://github.com/Shadows-of-Fire/Apothic-Enchanting)
  - [Apothic Spawners](https://github.com/Shadows-of-Fire/Apothic-Spawners)
- **Porting & Maintenance**: Fabric Port Contributors.
- **Code License**: Licensed under the [MIT License](LICENSE).
- **Assets License**: All upstream texture, model, audio, and language assets remain **All Rights Reserved** by Shadows_of_Fire. See [LICENSE_ASSETS](LICENSE_ASSETS) and [NOTICE](NOTICE) for full attributions.
