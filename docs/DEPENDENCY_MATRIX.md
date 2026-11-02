# Dependency Matrix & Scope Classification

## 1. Scope Classification

Every discovered dependency in the Apotheosis ecosystem has been evaluated and categorized per project rules:

| Dependency / Component | Category | Upstream Version | Target Version | License | Strategy & Scope Decision |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Apotheosis Core** | Ecosystem Code to Combine | 9.1.0 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated directly into `dev.shadowsoffire.apotheosis` |
| **Apothic Attributes** | Ecosystem Code to Combine | 3.1.0 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated into `dev.shadowsoffire.apotheosis.attributes` |
| **Apothic Enchanting** | Ecosystem Code to Combine | 2.1.0 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated into `dev.shadowsoffire.apotheosis.enchanting` |
| **Apothic Spawners** | Ecosystem Code to Combine | 2.0.1 | 1.0.0-alpha.1 | MIT / ARR Assets | Integrated into `dev.shadowsoffire.apotheosis.spawners` |
| **Placebo (Required Subsets)**| Ecosystem Code to Combine | 10.0.2 | 1.0.0-alpha.1 | MIT | Required codecs, dynamic registries, and network bridges ported into `placebo` package |
| **Fabric Loader** | External Platform Dependency | N/A | `>=0.19.5` | Apache-2.0 | Standard loader runtime |
| **Fabric API** | External Platform Dependency | N/A | `0.161.0+26.3` | Apache-2.0 | Platform hooks, events, transfer API, and networking |
| **YetAnotherConfigLib (YACL)** | Nested Library (JiJ) | N/A | `3.9.6+26.3` | LGPL-3.0 | Nested via Jar-in-Jar (`include`). Unshaded with manifest and license intact in `META-INF/jars/` |
| **Just Enough Items (JEI)** | Optional Integration | 19.21.0 | `31.17.0.108` | MIT | Verified on 26.3. Custom categories, catalysts, recipe transfer, and exclusion zones |
| **Jade / Waila** | Optional Integration | 26.0.9 | `26.3.5+fabric` | MIT | Verified on 26.3. Spawner stats and enchanting bookshelf/table data providers |
| **EMI** | Optional Integration | 1.1.18 | Deferred (TBD) | MIT | No 26.3 release published. Covered automatically via EMI's built-in JEI bridge |
| **Accessories / Trinkets** | Optional Integration | N/A | Deferred (TBD) | LGPL / MIT | No 26.3 releases published. Open `ModifierSourceType` extension points ready |
| **Patchouli (Fabric)** | Optional Integration | 26.1-94 | Deferred (TBD) | CC-BY-NC-SA 3.0 | Deferred; not an initial release requirement; no replacement book system planned |
| **Gateways to Eternity** | Out of Scope | 6.0.2 | Excluded | MIT | Completely out of scope; removed from planned work and release requirements |
| **Fabric Loom** | Development-Only Dependency | 1.17-SNAPSHOT| 1.17.21 | Apache-2.0 | Gradle build plugin |

---

## 2. Explicit Scope Decisions

### 2.1 Gateways to Eternity Scope Decision
- **Status**: **COMPLETELY OUT OF SCOPE**.
- **Decision**: Gateways to Eternity is removed from all planned work, dependency requirements, and release criteria. The core Apotheosis 1-JAR ecosystem will not bundle or plan companion work for Gateways.

### 2.2 Chronicle of Shadows & Documentation Support
- **Status**: **DEFERRED (TBD)**.
- **Decision**: Patchouli / Chronicle of Shadows is deferred and is not an initial release requirement. Do not plan a replacement book system. Book JSON assets remain untouched in `assets/apotheosis/patchouli_books/` without active runtime hooks.

### 2.3 Single JAR Packaging & YACL Strategy
- **YACL Packaging**: YACL is licensed under LGPL-3.0. In Fabric, third-party libraries must never be flattened (shaded) into the mod jar, as shading breaks Fabric mod discovery and violates LGPL separation requirements.
- **Decision**: YACL `3.9.6+26.3-fabric` is properly nested via Fabric's standard Jar-in-Jar (`include`) mechanism in `build.gradle`. It sits intact in `META-INF/jars/` with its original `fabric.mod.json`, JAR manifest, and LGPL license metadata fully preserved.

---

## 3. Actual Installation Set

For players and modpack authors:
1. **`fabricated-apotheosis-1.0.0-alpha.1.jar`** (Single combined ecosystem download containing Apotheosis, Apothic Attributes, Apothic Enchanting, Apothic Spawners, required Placebo utilities, and nested YACL).
2. **Platform Prerequisites**:
   - Fabric Loader `>=0.19.5`
   - Fabric API `0.161.0+26.3`
3. **Optional Enhancements**:
   - Just Enough Items (JEI) `31.17.0.108+` (in-game recipe viewer and transfer)
   - Jade `26.3.5+fabric` (in-game HUD block/spawner inspection)
   - ModMenu (for accessing the in-game configuration screen from the title menu)
