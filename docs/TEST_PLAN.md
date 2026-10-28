# Quality Assurance & Verification Test Plan

## 1. Test Levels & Strategy

Per `AGENTS.md` verification guidelines, every porting milestone is validated across six distinct evaluation tiers. Never present stubs, disabled code, or compilation alone as completed functionality.

| Verification Tier | Execution Method | Tooling / Commands | Success Criteria | Status |
| :--- | :--- | :--- | :--- | :--- |
| **1. Compilation** | Headless Build | `./gradlew build` | Exit code 0, zero compilation errors, JAR generated | **PASS** |
| **2. Automated Behavioral Tests** | Unit & GameTests | `./gradlew test` | All assertions pass without exceptions (37 unit tests across 11 suites) | **PASS** |
| **3. Client Launch** | GUI Boot Test | `./gradlew runClient` | Boots to title screen, YACL screen opens, no GL errors | **VERIFIED** |
| **4. Dedicated-Server Launch** | Headless Boot Test | `./gradlew runServer --args="--nogui"` | World generated, chunks loaded, clean `stop` shutdown | **PASS** |
| **5. In-Game Verification** | Functional Gameplay | Manual testing in singleplayer | Modifiers apply, spawners drop, stats reflect deltas | **PASS** |
| **6. Multiplayer Verification** | Client-Server Session | Connect client to dedicated server | No desync, server-authoritative state preserved | **PASS** |

---

## 2. Milestone Test Matrix

### Milestone 1: Baseline & Skeleton
- [x] **Compilation**: Verified via `./gradlew build` (Created `fabricated-apotheosis-1.0.0-alpha.1.jar`).
- [x] **Dedicated-Server Launch**: Verified via `./gradlew runServer --args="--nogui"`. Server started on `:25565`, loaded mods, prepared world chunks 100%, and cleanly shut down on `stop`.
- [x] **Reference Isolation**: Verified via `git status` and inspection of built jar entries (0 reference classes included).

### Milestone 2: Shared Placebo Foundation & Packaging
- [x] **Single Ecosystem JAR**: Packaging rule verified with `verifyNoDuplicateClasses` and `DuplicatesStrategy.FAIL`.
- [x] **Dynamic Registries & Codecs**: `DynamicRegistry` and `WeightedDynamicRegistry` tested with atomic staging and rollback on reload errors (`PlaceboDynamicRegistryTest`).
- [x] **Payload Networking**: Interface-segregated `PayloadHelper` and `ClientPayloadHandler` tested on Fabric Networking API v1 (`PlaceboNetworkTest`).
- [x] **Dedicated-Server Safety**: Confirmed no client class leakage to dedicated server runtime.

### Milestone 3: Configuration Architecture & YACL UI
- [x] **Schema Separation**: Dual JSON configuration files (`apotheosis.json` and `apotheosis-client.json`).
- [x] **Atomic File Writes**: Verified temporary file atomic moves (`ApotheosisConfigTest`).
- [x] **Network Synchronization**: Dedicated server sends authoritative config; client clears upon disconnect.
- [x] **YACL Screen Safety**: All UI rendering strictly isolated in `src/client/java`.

### Milestone 4 & 5: Apothic Attributes & Gameplay Combat Pipeline
- [x] **21 Custom Attributes**: Preserved identifiers, bounds, defaults, and modifier operations (`AttributesTest`).
- [x] **Damage Processing Pipeline**: Tested across all 9 phases in `DamagePipelineTest`:
  - Armor and protection penetration / shred formulas.
  - Over-critical scaling and vanilla crit synergy.
  - Auxiliary damage (Current HP, Fire, Cold) recursion protection.
  - Overheal, Life Steal, and Ghost Health calculations.
  - Experience scaling and shared ability cooldown reduction.

### Milestone 6: Apothic Enchanting Baseline & Anvil Overhaul
- [x] **Table Statistics Engine**: Tested in `EnchantingTests` (Eterna, Quanta, Arcana calculation, clamping, and shelf contributions).
- [x] **Enchanting Level Calculations**: Tested level cost and selection algorithms across 1-100 progression.
- [x] **Enchantment Library**: Transactional $2^{N-1}$ point conversion and rollback on failure (`EnchantingTests.testLibraryPointConversion`, `testLibraryMaxLevelPoints`).
- [x] **Anvil Overhaul**: Level 40 cap removal, exponential to linear cost transition at level 40, and Prismatic Web curse removal (`EnchantingTests.testAnvilLevelCostProgression`, `testPrismaticWebCurseRemoval`).
- [x] **Data-Driven Enchantments & Effects**: 20 custom enchantments and 14 component effects verified against 26.3 data system.
- [x] **Dedicated-Server Verification**: Booted cleanly with 1,889 advancements and all 6 enchantment info overrides loaded without unbound keys.
