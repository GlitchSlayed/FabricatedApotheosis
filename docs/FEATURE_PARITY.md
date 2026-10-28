# Feature Parity Matrix — Apotheosis NeoForge (26.1.2) to Fabric (26.3)

## 1. Overview & Status Key

This matrix tracks the parity status of all subsystems across the Apotheosis ecosystem.

Status Definitions:
- **`not started`**: Feature identified in upstream, no implementation work started.
- **`investigated`**: Source, hooks, and porting surface audited; architecture established.
- **`implemented`**: Code written and integrated into the Fabric project skeleton.
- **`verified`**: Verified through compilation, automated tests, or in-game runtime launch.
- **`blocked`**: Dependent on an unresolved design decision or external dependency.

---

## 2. Infrastructure & Platform Skeleton

| Feature / Subsystem | Status | Verification & Evidence | Notes |
| :--- | :--- | :--- | :--- |
| **Combined 1-JAR Packaging** | `verified` | `./gradlew build` outputs single jar | `fabricated-apotheosis-1.0.0-alpha.1.jar` |
| **Fabric 26.3 Toolchain** | `verified` | Loom 1.17.21, Fabric Loader 0.19.5, Java 25 | Fully builds and packages |
| **Dedicated Server Launch** | `verified` | `./gradlew runServer --args="--nogui"` | 100% chunks loaded, clean stop, zero client leak |
| **Server-Safe Config Model** | `verified` | `ServerConfig.java`, atomic writes, schema v1 | Tested in `ServerConfigTest` & server startup |
| **Client Preferences Model** | `verified` | `ClientConfig.java`, persists in `apotheosis-client.json` | Tested in `ClientConfigTest` & `ApotheosisConfigTest` |
| **Schema & Legacy Migration** | `verified` | `ConfigMigration.java` | Tested in `ConfigMigrationTest` (v0->v1 and `.cfg`) |
| **Network Config Sync** | `verified` | `ApotheosisConfigPayload.java` | Tested in `ServerConfigTest.testNetworkStreamCodec` |
| **Remote Stale State Clearing** | `verified` | `ClientPlayConnectionEvents.DISCONNECT` hook | Tested in `ApotheosisConfigTest.testRemoteOverride` |
| **YACL Client Config Screen** | `verified` | `ApotheosisConfigScreen.java` | Segregated in client source set; read-only on remote |
| **Reference Trees Isolation** | `verified` | `.gitignore` and `REFERENCE_INDEX.md` | Reference files strictly read-only and unbundled |

---

## 3. Apothic Spawners Module

| Upstream Feature | Status | Target Scope / Plan |
| :--- | :--- | :--- |
| **Spawner Stat Modifiers** | `investigated` | Modifier items (sugar, clock, ghast tear, blaze rod, etc.) targeting `BaseSpawner.serverTick`. |
| **Redstone Activation Control** | `investigated` | Toggle spawner execution via comparator/redstone power. |
| **Condition Ignore Modifiers** | `investigated` | Ignore light level, ignore nearby players, ignore spawn range checks. |
| **Silk Touch Capture & Drops** | `investigated` | Custom loot table condition + block drops mixin to preserve spawner stats. |
| **Spawner Stat Deltas in Tooltip** | `investigated` | `Item.appendHoverText` hooking into 26.3 `TooltipDisplay`. |
| **Spawn Egg Modification** | `investigated` | Adapt from legacy `setEntityId` to 26.3 `setEntityData(TypedEntityData)`. |

---

## 4. Apothic Attributes Module

| Upstream Feature | Status | Target Scope / Plan |
| :--- | :--- | :--- |
| **21 Custom Combat Attributes** | `investigated` | Audit completed in `docs/VANILLA_OVERLAP.md`. Porting to `RangedAttribute`. |
| **Mining Speed Attribute Removal** | `verified` | Confirmed removed in upstream 3.1.0; mapped to vanilla `MINING_EFFICIENCY`. |
| **Combat Rules & Armor Piercing** | `investigated` | Mixins on `CombatRules.getDamageAfterAbsorb` and `LivingEntity.hurt`. |
| **Overheal & Ghost Health** | `investigated` | Absorption conversion and out-of-combat health recovery hooks. |
| **Attributes GUI Formatting** | `investigated` | Client screen formatting for percentage and flat attribute values. |

---

## 5. Apothic Enchanting Module

| Upstream Feature | Status | Target Scope / Plan |
| :--- | :--- | :--- |
| **Table Stats (Eterna, Quanta, Arcana)** | `investigated` | Calculation of bookshelf stats around enchanting table. |
| **High-Tier Enchantment Levels** | `investigated` | Mixins on `EnchantmentHelper` and `EnchantmentMenu` to unlock up to level 100. |
| **Access Widener Transpilation** | `investigated` | Transpile `accesstransformer.cfg` to Fabric `apotheosis.accesswidener`. |
| **Custom Tomes** | `investigated` | Tomes of extraction, scrapping, and superior books. |
| **Capturing Enchantment** | `investigated` | Spawn egg drop chance on mob death based on capturing level. |

---

## 6. Placebo Support (Required Subsets)

| Upstream Feature | Status | Target Scope / Plan | Verification & Evidence |
| :--- | :--- | :--- | :--- |
| **Registration Helpers (`DeferredHelper`)** | `verified` | Clean registration wrapper with explicit duplicate key detection. | Tested in `DeferredHelperTest`: duplicate registration throws clear `IllegalStateException`. |
| **Codecs & Serialization (`StepFunction`, `PlaceboCodecs`, `MapBackedCodec`, `CodecMap`)** | `verified` | Math step functions and polymorphic codec maps for reloadable elements. | Tested in `StepFunctionTest`: exact JSON/NBT round-trip and level-based calculations pass. |
| **Dynamic Data Loading (`DynamicRegistry`, `WeightedDynamicRegistry`, `DynamicHolder`)** | `verified` | Dynamic data loading from datapacks with atomic reloading and cache invalidation. | Tested in `DynamicRegistryTest`: load, reload, invalidation, and malformed JSON error handling verified. |
| **Dynamic Registry Coordination (`DynamicRegistryManager`)** | `verified` | Server resource reload listener and network synchronization coordinator. | Verified on dedicated server startup (`Placebo/DynRegManager: Dynamic registry manager initialized successfully`). |
| **Item Cache Utilities (`CachedObject`)** | `implemented` | Component-hash based item stack caching to prevent redundant calculations. | `CachedObject.java` in place with component hashing. |
| **Experience Math (`EnchantmentUtils`)** | `verified` | Exact vanilla XP-to-level and level-to-XP conversions. | Tested in `EnchantmentUtilsTest` against vanilla level curve formulas. |
| **Network Payload Registrar (`PayloadHelper`)** | `verified` | Dual clientbound/serverbound payload registration with dedicated-server safe client handler. | Server receivers registered; client separation verified via `PlaceboClientPayloadHandler`. |
| **Packaging & Standalone Module Defense** | `verified` | Duplicate class enforcement and explicit mod conflict blocks. | `verifyNoDuplicateClasses` Gradle task, `DuplicatesStrategy.FAIL`, and `"breaks"` in `fabric.mod.json`. |

---

## 7. Integrations & Documentation

| Subsystem | Status | Target Scope / Plan |
| :--- | :--- | :--- |
| **Gateways to Eternity** | `investigated` | Excluded from initial core 1-JAR release; kept as optional companion integration. |
| **Patchouli / Chronicle of Shadows**| `investigated` | Retain complete `patchouli_books` asset tree; optional runtime dependency. |
| **ModMenu Config Integration** | `investigated` | Entrypoint hook to open YACL config screen from ModMenu list. |
| **RenderPearl GPU Pipelines** | `investigated` | Reserved for boss health bars, gem render passes, and custom screen shaders. |
