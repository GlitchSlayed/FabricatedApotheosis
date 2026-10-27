# Dependency Matrix & Scope Classification

## 1. Scope Classification

Every discovered dependency in the Apotheosis ecosystem has been evaluated and categorized per project rules:

| Dependency / Component | Category | Upstream Version | Target Version | License | Strategy & Scope Decision |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Apotheosis Core** | Ecosystem Code to Combine | 9.1.0 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated directly into `dev.shadowsoffire.apotheosis` |
| **Apothic Attributes** | Ecosystem Code to Combine | 3.1.0 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated into `dev.shadowsoffire.apotheosis.attributes` |
| **Apothic Enchanting** | Ecosystem Code to Combine | 2.1.0 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated into `dev.shadowsoffire.apotheosis.enchanting` |
| **Apothic Spawners** | Ecosystem Code to Combine | 2.0.1 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated into `dev.shadowsoffire.apotheosis.spawners` |
| **Placebo (Required Subsets)**| Ecosystem Code to Combine | 10.0.2 | 1.0.0-alpha.1 | MIT | Only required codecs, dynamic registries, and config bridges ported into `placebo` package |
| **Fabric Loader** | External Platform Dependency | N/A | `>=0.19.5` | Apache-2.0 | Standard loader runtime |
| **Fabric API** | External Platform Dependency | N/A | `0.161.0+26.3` | Apache-2.0 | Platform hooks, events, and networking |
| **YetAnotherConfigLib (YACL)** | Embedded / External Library | N/A | `3.9.6+26.3` | LGPL-3.0 | Evaluated as external platform dependency or Jar-in-Jar (JiJ) with preserved metadata |
| **Patchouli (Fabric)** | Optional Integration | 26.1-94 | 26.3 Fabric | CC-BY-NC-SA 3.0 | Optional book engine; keeps book JSON assets uncoupled |
| **Gateways to Eternity** | Unrelated Sibling Mod / Optional Integration | 6.0.2 | Post-core | MIT | Separate standalone mod; deferred from core 1-JAR release |
| **Curios API** | Unrelated Sibling Mod (NeoForge)| 15.0.0 | N/A | LGPL-3.0 | Dropped; replaced on Fabric by Trinkets/Accessories or native components |
| **Jade / Waila** | Optional Integration | 26.0.9 | Compatible | MIT | Optional tooltip display provider |
| **EMI / JEI** | Optional Integration | 29.5.0 | Compatible | MIT | Optional recipe viewer integration |
| **Fabric Loom** | Development-Only Dependency | 1.17-SNAPSHOT| 1.17.21 | Apache-2.0 | Gradle plugin |

---

## 2. Explicit Scope Decisions

### 2.1 Gateways to Eternity Scope Decision
- **Integration in Upstream**: In Apotheosis, Gateways is used only in `dev.shadowsoffire.apotheosis.compat.gateways` for optional boss rush waves and tiered gateway pearls (Ascent, Frontier, Pinnacle, Summit).
- **Cost & Dependencies**: Gateways to Eternity has its own entity lifecycle (`GatewayEntity`), custom rendering, client particle packets, and complex wave progression state. Porting Gateways adds 43 Java classes, separate entity renderers, and additional mixins.
- **Decision**: **EXCLUDE FROM CORE DISTRIBUTABLE**.
  - Keep Gateways compat behind an optional check (`PlatformHelper.isModLoaded("gateways")`).
  - Do not bloat the initial core Apotheosis 1-JAR ecosystem with a separate mod.
  - If desired by players, Gateways to Eternity can be ported as a standalone companion mod.

### 2.2 Chronicle of Shadows & Documentation Support
- **Upstream Implementation**: Book entries are authored entirely as Patchouli JSON definitions in `assets/apotheosis/patchouli_books/apoth_chronicle/`.
- **License Considerations**: Patchouli is licensed under CC-BY-NC-SA 3.0. Flattening Patchouli into the single Apotheosis JAR would impose copyleft CC restrictions on the entire combined JAR.
- **Evaluation**:
  1. *Patchouli Fabric Jar-in-Jar / External Dependency*: Preserves Patchouli's distinct metadata and licensing. Compatible with all existing book JSON without format migration.
  2. *Internal Replacement / Lavender Viewer*: Would eliminate the Patchouli dependency but requires implementing custom screen renderers, markdown/page pagination, and book item logic.
- **Decision**: **KEEP PATCHOULI AS OPTIONAL INTEGRATION / DEPENDENCY**.
  - Retain the complete `patchouli_books` asset tree in the combined JAR.
  - When Patchouli (Fabric) is installed, the Chronicle of Shadows functions immediately.
  - If Patchouli is absent, the mod operates cleanly without errors or crashes.

### 2.3 Single JAR Packaging & YACL Strategy
- **YACL Packaging**: YACL is licensed under LGPL-3.0. In Fabric, third-party libraries should never be flattened (shaded) into the mod jar, as shading breaks Fabric mod discovery and violates LGPL separation requirements.
- **Decision**: YACL will be distributed either via Fabric's standard Jar-in-Jar (`include`) mechanism or declared as a runtime dependency. In both approaches, YACL retains its own `fabric.mod.json`, JAR manifest, and LGPL license intact.

---

## 3. Final Proposed Installation Set

For players and modpack authors:
1. **`fabricated-apotheosis-1.0.0.jar`** (Single combined ecosystem download containing Apotheosis, Apothic Attributes, Apothic Enchanting, Apothic Spawners, and required Placebo utilities).
2. **Platform Prerequisites**:
   - Fabric Loader `>=0.19.5`
   - Fabric API `0.161.0+26.3`
   - YetAnotherConfigLib (YACL) `3.9.6+26.3-fabric` (or embedded via JiJ)
3. **Optional Enhancements**:
   - Patchouli Fabric (for in-game Chronicle of Shadows guidebook)
   - ModMenu (for accessing the YACL in-game config screen from the main menu)
