# Upstream Source Lock & Ecosystem Baseline

## 1. Verified Upstream Repositories

All upstream sources have been fetched and inspected in the `upstream/` reference directory.
The chosen versions form a verified cohesive release set targeting Minecraft 26.1.2 NeoForge.

| Project | Repository URL | Branch / Tag | Exact Commit SHA | MC Version | Code License | Asset License | Source File Reference |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Apotheosis** | `https://github.com/Shadows-of-Fire/Apotheosis.git` | `26.1` (v9.1.0) | `f2e76862ed9f4dcdf19c25400dc3d5478a46dd74` | `26.1.2` | MIT License | All Rights Reserved | `upstream/Apotheosis/gradle.properties:11` |
| **Placebo** | `https://github.com/Shadows-of-Fire/Placebo.git` | `26.1` (v10.0.2) | `8fcfa1e6e947e8f0f12044ad31ce4bef2145982a` | `26.1.2` | MIT License | MIT License | `upstream/Placebo/gradle.properties:11` |
| **Apothic-Attributes** | `https://github.com/Shadows-of-Fire/Apothic-Attributes.git` | `26.1` (v3.1.0) | `44d0a8316fcb7965c26e8ff7eda341830ca63023` | `26.1.2` | MIT License | All Rights Reserved | `upstream/Apothic-Attributes/gradle.properties:11` |
| **Apothic-Enchanting** | `https://github.com/Shadows-of-Fire/Apothic-Enchanting.git` | `26.1` (v2.1.0) | `4d167fd44222058819fa70bf99fbc3ab38220e34` | `26.1.2` | MIT License | All Rights Reserved | `upstream/Apothic-Enchanting/gradle.properties:11` |
| **Apothic-Spawners** | `https://github.com/Shadows-of-Fire/Apothic-Spawners.git` | `26.1` (v2.0.1) | `a5401df4a80b833b297c398f88a5718bdd47c02a` | `26.1.2` | MIT License | All Rights Reserved | `upstream/Apothic-Spawners/gradle.properties:11` |
| **GatewaysToEternity** | `https://github.com/Shadows-of-Fire/GatewaysToEternity.git` | `26.1` (v6.0.2) | `73aa0726c998214a180df1e2981ef1752ddb56e9` | `26.1.2` | MIT License | MIT License | `upstream/GatewaysToEternity/gradle.properties:11` |
| **Patchouli** | `https://github.com/VazkiiMods/Patchouli.git` | `release-26.1-94-beta` | `4f94b07bbd287f2e208a7ea90b0ca066e54f738c` | `26.1.2` | CC-BY-NC-SA 3.0 | CC-BY-NC-SA 3.0 | `upstream/Patchouli/gradle.properties` |
| **YetAnotherConfigLib** | `https://github.com/isXander/YetAnotherConfigLib.git` | `3.9.6+26.3-fabric` | Maven Release | `26.3` | LGPL-3.0 | LGPL-3.0 | Modrinth Maven metadata |

---

## 2. Dependency Cohesion Verification

The upstream dependencies declared in `upstream/Apotheosis/gradle.properties` verify that these exact versions interoperate:
- `placeboVersion=10.0.2` (Matches `upstream/Placebo` v10.0.2)
- `apothicAttributesVersion=3.0.1` (Compatible with `upstream/Apothic-Attributes` v3.1.0)
- `apothicSpawnersVersion=2.0.0` (Compatible with `upstream/Apothic-Spawners` v2.0.1)
- `apothicEnchantingVersion=2.0.0` (Compatible with `upstream/Apothic-Enchanting` v2.1.0)
- `gatewaysVersion=6.0.2` (Matches `upstream/GatewaysToEternity` v6.0.2)
- `patchouliVersion=26.1-94` (Matches `upstream/Patchouli` tag `release-26.1-94-beta`)
- `curiosVersion=15.0.0-beta.2+26.1.2` (Curios NeoForge; replaced on Fabric by Trinkets/Accessories or native vanilla components)

---

## 3. Platform & Target Lock

- **Target Minecraft**: `26.3` (Release build `2026-09-15T11:20:48+00:00`, World 5023, Protocol 777)
- **Mod Loader**: Fabric Loader `0.19.5`
- **Fabric API**: `0.161.0+26.3`
- **Fabric Loom**: `1.17.21` (`1.17-SNAPSHOT`)
- **Java Virtual Machine**: Java 25 (OpenJDK 25.0.4.1)
- **UI / Config Framework**: YetAnotherConfigLib (YACL) `3.9.6+26.3-fabric`
- **Rendering Architecture**: Minecraft 26.3 Mojang high-level rendering pipelines, with public RenderPearl APIs for specialized GPU passes.
