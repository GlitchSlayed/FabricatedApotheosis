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
| **Patchouli (Fabric)** | Optional Integration | 26.1-94 | Deferred (TBD) | CC-BY-NC-SA 3.0 | Deferred; not an initial release requirement; no replacement book system planned |
| **Gateways to Eternity** | Out of Scope | 6.0.2 | Excluded | MIT | Completely out of scope; removed from planned work and release requirements |
| **Curios API** | Unrelated Sibling Mod (NeoForge)| 15.0.0 | N/A | LGPL-3.0 | Dropped; replaced on Fabric by Trinkets/Accessories or native components |
| **Jade / Waila** | Optional Integration | 26.0.9 | Compatible | MIT | Optional tooltip display provider |
| **EMI / JEI** | Optional Integration | 29.5.0 | Compatible | MIT | Optional recipe viewer integration |
| **Fabric Loom** | Development-Only Dependency | 1.17-SNAPSHOT| 1.17.21 | Apache-2.0 | Gradle plugin |

---

## 2. Explicit Scope Decisions

### 2.1 Gateways to Eternity Scope Decision
- **Status**: **COMPLETELY OUT OF SCOPE**.
- **Decision**: Gateways to Eternity is removed from all planned work, dependency requirements, and release criteria. The core Apotheosis 1-JAR ecosystem will not bundle or plan companion work for Gateways.

### 2.2 Chronicle of Shadows & Documentation Support
- **Status**: **DEFERRED (TBD)**.
- **Decision**: Patchouli / Chronicle of Shadows is deferred and is not an initial release requirement. Do not plan a replacement book system. Book JSON assets remain untouched in `assets/apotheosis/patchouli_books/` without active runtime hooks.

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
