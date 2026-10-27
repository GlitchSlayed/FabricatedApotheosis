# Upstream Lock & Target Baseline

## 1. Upstream Pinned Baseline

The port is based on the pinned Minecraft 26.1.2 NeoForge ecosystem of Apotheosis:

| Component | Pinned Upstream Version | Upstream Mod Loader | Notes |
| :--- | :--- | :--- | :--- |
| **Minecraft Base** | `26.1.2` (World 4790, Protocol 775) | Mojang / NeoForge | Reference JAR: `reference/26.1.2client.jar` |
| **Apotheosis Core** | 26.1.2 Ecosystem | NeoForge | Core modular framework |
| **Placebo** | 26.1.2 Component | NeoForge | Utility library; only required functionality to be ported |
| **Apothic Attributes** | 26.1.2 Component | NeoForge | Attribute system extensions |
| **Apothic Enchanting** | 26.1.2 Component | NeoForge | High-tier enchanting & stats |
| **Apothic Spawners** | 26.1.2 Component | NeoForge | Spawner modification and mechanics |

---

## 2. Target Platform & Dependencies

| Component | Target Version / Identifier | Platform | Role |
| :--- | :--- | :--- | :--- |
| **Minecraft Target** | `26.3` (World 5023, Protocol 777) | Mojang / Fabric | Target JAR: `reference/26.3client.jar` |
| **Mod Loader** | Fabric Loader `>= 0.16.x` | Fabric | Runtime loader |
| **Fabric API** | Target `26.3` compatible | Fabric | Core engine hooks and registry extensions |
| **Configuration Library** | YetAnotherConfigLib (YACL) v3 | Fabric | In-game and file-based config screen |
| **Rendering Abstraction** | RenderPearl / Vanilla 26.3 | Fabric / Mojang | High-level rendering abstraction |
| **Java Runtime** | Java 25 (Classfile 69) | JVM | Runtime target |

---

## 3. Policy Constraints

- **No NeoForge Binary Bundling**: NeoForge jars and binaries will never be bundled into the Fabric release.
- **Single Ecosystem Distributable**: All approved modules are delivered in one cohesive Fabric mod JAR.
- **Server Authoritative**: All transactions, spawner logic, attribute calculations, and persistence remain strictly server-authoritative.
- **Clean Separation of Client/Server**: Dedicated server classpaths must never load client classes or screens.
