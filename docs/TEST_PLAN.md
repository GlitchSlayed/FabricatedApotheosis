# Quality Assurance & Verification Test Plan

## 1. Test Levels & Strategy

Per `AGENTS.md` verification guidelines, every porting milestone is validated across six distinct evaluation tiers. Never present stubs, disabled code, or compilation alone as completed functionality.

| Verification Tier | Execution Method | Tooling / Commands | Success Criteria | Status |
| :--- | :--- | :--- | :--- | :--- |
| **1. Compilation** | Headless Build | `./gradlew build` | Exit code 0, zero compilation errors, JAR generated | **PASS** |
| **2. Automated Behavioral Tests** | Unit & GameTests | `./gradlew test` / Gametests | All assertions pass without exceptions | **PLANNED** |
| **3. Client Launch** | GUI Boot Test | `./gradlew runClient` | Boots to title screen, YACL screen opens, no GL errors | **IN PROGRESS** (Asset download) |
| **4. Dedicated-Server Launch** | Headless Boot Test | `./gradlew runServer --args="--nogui"` | World generated, chunks loaded, clean `stop` shutdown | **PASS** |
| **5. In-Game Verification** | Functional Gameplay | Manual testing in singleplayer | Modifiers apply, spawners drop, stats reflect deltas | **PLANNED** |
| **6. Multiplayer Verification** | Client-Server Session | Connect client to dedicated server | No desync, server-authoritative state preserved | **PLANNED** |

---

## 2. Milestone Test Matrix

### Milestone 1: Baseline & Skeleton
- [x] **Compilation**: Verified via `./gradlew build` (Created `fabricated-apotheosis-1.0.0-alpha.1.jar`).
- [x] **Dedicated-Server Launch**: Verified via `./gradlew runServer --args="--nogui"`. Server started on `:25565`, loaded 67 mods, prepared world chunks 100%, and cleanly shut down on `stop`.
- [x] **Reference Isolation**: Verified via `git status` and inspection of built jar entries (0 reference classes included).

### Milestone 2: Spawners Module
- [ ] Automated GameTest: Apply modifier items to spawner and assert delay decrease.
- [ ] Dedicated Server: Silk-touch spawner mining drops item with modifier components.
- [ ] Dedicated Server: Redstone signal disables spawner execution when configured.
- [ ] Client GUI: Verify item hover text displays delta calculations correctly.

### Milestone 3: Attributes Module
- [ ] Behavioral Test: Attack damage calculation with armor pierce vs diamond armor.
- [ ] Behavioral Test: Overheal converts excess damage to absorption hearts up to 50% max HP.
- [ ] Regression Test: Verify `mining_efficiency` functions via vanilla attribute holder without `apothic_attributes:mining_speed`.

### Milestone 4: Enchanting Module
- [ ] Behavioral Test: Bookshelf detection counts Eterna, Quanta, Arcana.
- [ ] Behavioral Test: Enchantment level uncapping allows levels > vanilla caps up to level 100.
- [ ] Dedicated Server: Anvil repair and combination respects high-tier caps.
