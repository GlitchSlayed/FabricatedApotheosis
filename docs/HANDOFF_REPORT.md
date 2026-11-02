# Handoff Report — Milestone 9: Progression, World Content & Ecosystem Integration

- **Date**: 2026-10-30
- **Target Version**: Minecraft 26.3 Fabric (Loom 1.17.21, Loader 0.19.5, YACL 3.9.6)

## 1. Current Repository Tree

```text
FabricatedApotheosis/
├── .github/workflows/build.yml
├── .gitignore
├── AGENTS.md
├── Agents.md -> AGENTS.md
├── LICENSE (MIT - Code)
├── LICENSE_ASSETS (All Rights Reserved - Upstream Assets)
├── NOTICE (Third-Party Attributions)
├── README.md
├── build.gradle (with verifyNoDuplicateClasses & DuplicatesStrategy.FAIL)
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── docs/
│   ├── ADVENTURE_INVENTORY.md
│   ├── ARCHITECTURE.md
│   ├── DAMAGE_PIPELINE.md
│   ├── DECISIONS.md
│   ├── DEPENDENCY_MATRIX.md
│   ├── ENCHANTING_INVENTORY.md
│   ├── FEATURE_PARITY.md
│   ├── HANDOFF_REPORT.md
│   ├── PORTING_SURFACE.md
│   ├── REFERENCE_INDEX.md
│   ├── SAVE_COMPATIBILITY.md
│   ├── SPAWNERS_INVENTORY.md
│   ├── STATUS.md
│   ├── TEST_PLAN.md
│   ├── UPSTREAM_LOCK.md
│   └── VANILLA_OVERLAP.md
├── reference -> Refrences (symlink, excluded from git)
├── Refrences/ (read-only reference jars and classes, excluded from git)
├── upstream/ (cloned upstream 26.1.2 sources, excluded from git)
├── src/
│   ├── main/
│   │   ├── java/dev/shadowsoffire/
│   │   │   ├── apotheosis/
│   │   │   │   ├── Apotheosis.java
│   │   │   │   ├── Apoth.java
│   │   │   │   ├── AdventureConfig.java
│   │   │   │   ├── adventure/AdventureModule.java
│   │   │   │   ├── affix/
│   │   │   │   ├── attachments/
│   │   │   │   ├── commands/
│   │   │   │   │   ├── AffixCommand.java
│   │   │   │   │   ├── BossCommand.java
│   │   │   │   │   ├── WorldTierCommand.java
│   │   │   │   │   └── ...
│   │   │   │   ├── gen/
│   │   │   │   │   ├── BossDungeonFeature.java
│   │   │   │   │   ├── BossDungeonFeature2.java
│   │   │   │   │   ├── RogueSpawnerFeature.java
│   │   │   │   │   └── ItemFrameGemsProcessor.java
│   │   │   │   ├── item/
│   │   │   │   │   ├── BossSummonerItem.java
│   │   │   │   │   └── ...
│   │   │   │   ├── loot/
│   │   │   │   ├── mixin/
│   │   │   │   │   ├── BaseSpawnerAccessor.java
│   │   │   │   │   ├── EntityInvoker.java
│   │   │   │   │   ├── MobMixin.java
│   │   │   │   │   ├── NearestAttackableTargetGoalAccessor.java
│   │   │   │   │   ├── NearestAttackableTargetGoalMixin.java
│   │   │   │   │   ├── StructureTemplateMixin.java
│   │   │   │   │   └── ...
│   │   │   │   ├── mobs/
│   │   │   │   │   ├── ApothMobEvents.java
│   │   │   │   │   ├── BossSpawnerBlock.java
│   │   │   │   │   ├── InvaderSpawnRules.java
│   │   │   │   │   ├── registries/
│   │   │   │   │   │   ├── AugmentRegistry.java
│   │   │   │   │   │   ├── EliteRegistry.java
│   │   │   │   │   │   └── InvaderRegistry.java
│   │   │   │   │   ├── types/
│   │   │   │   │   │   ├── Augmentation.java
│   │   │   │   │   │   ├── Elite.java
│   │   │   │   │   │   └── Invader.java
│   │   │   │   │   └── util/
│   │   │   │   ├── net/
│   │   │   │   │   ├── BossSpawnPayload.java
│   │   │   │   │   ├── WorldTierPayload.java
│   │   │   │   │   └── ...
│   │   │   │   ├── spawner/
│   │   │   │   │   ├── RogueSpawner.java
│   │   │   │   │   └── RogueSpawnerRegistry.java
│   │   │   │   ├── tiers/
│   │   │   │   │   ├── GenContext.java
│   │   │   │   │   ├── WorldTier.java
│   │   │   │   │   └── augments/
│   │   │   │   └── util/
│   │   │   ├── apothic_attributes/
│   │   │   ├── apothic_enchanting/
│   │   │   ├── apothic_spawners/
│   │   │   │   ├── compat/SpawnerRecipeCache.java
│   │   │   │   └── modifiers/SpawnerModifier.java
│   │   │   └── placebo/
│   │   │       ├── json/
│   │   │       │   ├── ChancedEffectInstance.java
│   │   │       │   ├── NBTAdapter.java
│   │   │       │   ├── OptionalTemplateCodec.java
│   │   │       │   └── WeightedItemStack.java
│   │   │       └── systems/gear/
│   │   │           ├── GearSet.java
│   │   │           └── GearSetRegistry.java
│   │   └── resources/
│   │       ├── data/apotheosis/
│   │       │   ├── loot_table/
│   │       │   ├── placebo/gear_sets/
│   │       │   ├── recipe/spawner_modifiers/
│   │       │   ├── structure/
│   │       │   ├── tags/
│   │       │   └── worldgen/
│   │       │       ├── feature/
│   │       │       ├── placed_feature/
│   │       │       ├── processor_list/
│   │       │       ├── structure/
│   │       │       ├── structure_set/
│   │       │       └── template_pool/
│   │       ├── fabric.mod.json
│   │       └── apotheosis.mixins.json
│   ├── client/
│   │   └── java/dev/shadowsoffire/
│   │       ├── apotheosis/
│   │       │   ├── client/
│   │       │   │   ├── WorldTierSelectScreen.java
│   │       │   │   ├── WorldTierDetailScreen.java
│   │       │   │   ├── WorldTierTutorialScreen.java
│   │       │   │   └── ...
│   └── test/
│       └── java/dev/shadowsoffire/
│           ├── apotheosis/
│           │   ├── adventure/
│           │   │   ├── AdventureTests.java
│           │   │   └── ProgressionAndWorldgenTests.java
│           │   ├── attributes/
│           │   └── config/
│           ├── apothic_enchanting/
│           ├── apothic_spawners/
│           └── placebo/
```

---

## 2. Milestone 9 Implementation Highlights

1. **World Tiers & Advancement Progression**:
   - Implemented 5 World Tiers (`Haven`, `Frontier`, `Ascent`, `Summit`, `Pinnacle`) with advancement-based unlocks (`Apoth.Advancements.WORLD_TIER_*`).
   - Managed server-authoritative persistence via `Attachments.WORLD_TIER` and network synchronization via `WorldTierPayload`.
   - Built full user interfaces: `WorldTierSelectScreen`, `WorldTierDetailScreen`, and `WorldTierTutorialScreen`.
   - Added `/apotheosis world_tier` command supporting getting, setting, and checking tier unlocks for players.

2. **Tier Augment Recalculation**:
   - Ported `TierAugmentRegistry` for dynamic player and monster augments.
   - Bound recalculation hooks to player tier change, player join (`ServerPlayConnectionEvents.JOIN`), player respawn (`ServerPlayerEvents.COPY_FROM`), and dimension change (`ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL`).
   - Strictly removes previous tier modifiers and applies current tier modifiers, preventing modifier accumulation across transitions.

3. **Spawner Progression Reconciliation**:
   - Reconciled standalone vs Apotheosis spawner modifier recipes in `SpawnerRecipeCache` and `SpawnerModifier`.
   - When `enableAdventure` is active, standalone `apothic_spawners` modifier recipes are filtered out, ensuring that cheaper standalone recipes cannot bypass Apotheosis tier progression and that spawner runes are required.

4. **Bosses, Invaders, Elites & Gear Sets**:
   - Ported dynamic registries: `InvaderRegistry`, `EliteRegistry`, `AugmentRegistry`, and `GearSetRegistry`.
   - Added dimension-specific spawn rules via `InvaderSpawnRules` with surface/sky condition checks.
   - Built `BossSpawnerBlock` and `BossSpawnerTile` with player proximity detection, cluster spawning, and block replacement.
   - Built `BossSummonerItem` and `/apotheosis spawn_boss` command.
   - Implemented delayed elite initialization and boss spawn announcement broadcasting (`BossSpawnPayload`).

5. **World Generation & Fabric Biome API Integration**:
   - Implemented `BossDungeonFeature` (Variant 1: classic cobblestone/brick dungeon) and `BossDungeonFeature2` (Variant 2: NBT structure `boss_1.nbt`), double chests with dungeon loot tables, and boss spawner blocks.
   - Implemented `RogueSpawnerFeature` and `RogueSpawnerRegistry` for underground covered spawners.
   - Ported Tome Towers jigsaw structures (`tower_main`, `tower_leaf`, `tower_sand`, `tower_spruce`) using standard biome tags and `ItemFrameGemsProcessor` to populate item frames with tiered gems and purity.
   - Replaced NeoForge `BiomeModifier` JSONs with Fabric Biome API v1 (`BiomeModifications.addFeature`), cleanly registering features into overworld and cave generation steps.
   - Migrated all worldgen features to 26.3 `Registries.FEATURE` (`worldgen/feature/`) format.

6. **Loot Table & Data Migration**:
   - Ported boss entity drop tables, dungeon chests, and wandering trader loot.
   - Migrated loot table `TagEntry` schema from legacy `"name": "tag"` to 26.3 `"items": "#tag"`.

---

## 3. Verification & Test Evidence

1. **Automated Unit Tests**:
   - Executed `./gradlew test --rerun-tasks`: 62 tests executed across 14 test classes, **62 passed in 9s**.
   - Comprehensive tests in `ProgressionAndWorldgenTests.java`:
     - `Verify WorldTier ordering and unlock progression`: verified ordinal ordering, unlock checks, and fallback resolution.
     - `Verify tier bonuses apply and remove without modifier accumulation`: verified clean removal of old tier modifiers and reapplication of new ones on attribute maps.
     - `Verify Spawner Recipe Cache reconciles standalone and Apotheosis progression recipes`: verified standalone recipe suppression in combined mode and preservation in standalone mode.
     - `Verify Invader Spawn Rules resolve per dimension`: verified Overworld surface and Nether rules.
     - `Verify Feature MapCodecs encode and decode correctly`: verified custom and default `success_chance` round-tripping on `BossDungeonFeature`, `BossDungeonFeature2`, and `RogueSpawnerFeature`.
     - `Verify GearSet serialization round-trip`: verified `GearSet.CODEC` round-trip.

2. **Compilation & Packaging**:
   - `./gradlew compileJava compileClientJava`: Succeeded with 0 errors.

3. **Dedicated Server Launch**:
   - Executed `./gradlew runServer --args="--nogui"`.
   - Verified log output:
     `[Worker-Main-1/INFO] (Minecraft) Loaded 1910 advancements`
     `[BiomeModificationImpl] Applied 342 biome modifications to 57 of 67 new biomes in 10.49 ms`
     `[Server thread/INFO] (Minecraft) Preparing level "world"`
     `[Server thread/INFO] (Minecraft) Preparing spawn area: 100%`
     `[Server thread/INFO] (Minecraft) Done (0.401s)! For help, type "help"`
     `[Server thread/INFO] (Minecraft) Saving chunks for level 'ServerLevel[world]'/minecraft:overworld`
     `[Server thread/INFO] (Minecraft) Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether`
     `[Server thread/INFO] (Minecraft) Saving chunks for level 'ServerLevel[world]'/minecraft:the_end`
     `[Server thread/INFO] (Minecraft) Stopping server`
     `BUILD SUCCESSFUL in 18s`
   - Zero registry errors, zero loot table parse errors, zero unmapped biomes, and clean shutdown.

---

## 4. In-Game Test Procedure for Milestone 9

To test Milestone 9 functionality in-game:
1. **World Tiers Progression**:
   - Open inventory and click the World Tier orb icon, or run `/apotheosis world_tier` to view the current tier (default: `Haven`).
   - Earn the prerequisite advancement or run `/advancement grant @s only apotheosis:world_tier/frontier` to unlock `Frontier`.
   - Open the World Tier screen, select `Frontier`, and click Activate. Verify that monster health and damage increase and that the client chat receives the tier transition packet.
   - Run `/attribute @s ...` to verify that player augments are applied, and change tiers again to verify obsolete modifiers are cleanly removed without accumulation.
2. **Boss Spawners & Dungeons**:
   - Locate an underground Boss Dungeon or run `/execute in minecraft:overworld run locate structure ...` or place a `apotheosis:boss_spawner`.
   - Approach the boss spawner in Survival mode. Observe the spawner detecting the player within 8 blocks, bursting into an invading boss mob with custom equipment, name, and glowing effect, replacing the spawner with air.
   - Kill the boss and verify that affix-bearing boss equipment and rare boss loot drop.
3. **Boss Summoner & Commands**:
   - Run `/apotheosis spawn_boss ~ ~ ~ apotheosis:overworld/zombie apotheosis:mythic` to directly spawn a custom invader boss.
   - Right-click the ground with a Boss Summoner item to trigger an invader spawn.
4. **Spawner Progression**:
   - In combined mode, try using a standalone spawner upgrade item (e.g. Fermented Spider Eye for inverse counts). Verify that the recipe requires Apotheosis progression runes according to the active tier.
5. **Tome Towers**:
   - Explore overworld biomes to discover naturally generated Tome Towers. Check the chests and item frames for enchanted tomes and socketable gems with varying purity levels.

---

## 5. Next Concrete Implementation Task

Proceed to **Milestone 10: Platform Integrations & ModMenu**:
1. Wire ModMenu configuration screen factory to `ApotheosisConfigScreen`.
2. Implement accessory slot / trinket compatibility extension points (without hard dependencies).
3. Finalize client flair, visual polish, and custom particle rendering pipelines.
4. Note: Gateways to Eternity remains completely out of scope; Patchouli / Chronicle of Shadows deferred TBD.
