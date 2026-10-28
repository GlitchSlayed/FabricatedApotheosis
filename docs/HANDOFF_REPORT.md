# Handoff Report — Milestone 4: Custom Attributes, Vanilla Overlap & Attribute UI

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
├── build.gradle (with verifyNoDuplicateClasses & DuplicatesStrategy.FAIL)
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DECISIONS.md
│   ├── DEPENDENCY_MATRIX.md
│   ├── FEATURE_PARITY.md
│   ├── HANDOFF_REPORT.md
│   ├── PORTING_SURFACE.md
│   ├── REFERENCE_INDEX.md
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
│   │   │   │   ├── adventure/AdventureModule.java
│   │   │   │   ├── attributes/
│   │   │   │   │   ├── AttributesModule.java
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── ALObjects.java
│   │   │   │   │   │   └── AttributeHelper.java
│   │   │   │   │   └── format/
│   │   │   │   │       └── AttributeFormatting.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── ApotheosisConfig.java
│   │   │   │   │   ├── ClientConfig.java
│   │   │   │   │   ├── ConfigImpact.java
│   │   │   │   │   ├── ConfigMigration.java
│   │   │   │   │   ├── ServerConfig.java
│   │   │   │   │   └── network/
│   │   │   │   │       └── ApotheosisConfigPayload.java
│   │   │   │   ├── docs/DocsSupport.java
│   │   │   │   ├── enchanting/EnchantingModule.java
│   │   │   │   ├── integrations/IntegrationsModule.java
│   │   │   │   ├── placebo/PlaceboSupport.java
│   │   │   │   ├── platform/PlatformHelper.java
│   │   │   │   └── spawners/SpawnersModule.java
│   │   │   └── placebo/
│   │   │       ├── Placebo.java
│   │   │       ├── codec/
│   │   │       │   ├── CodecMap.java
│   │   │       │   ├── CodecProvider.java
│   │   │       │   ├── MapBackedCodec.java
│   │   │       │   └── PlaceboCodecs.java
│   │   │       ├── dynreg/
│   │   │       │   ├── DynamicHolder.java
│   │   │       │   ├── DynamicRegistry.java
│   │   │       │   ├── DynamicRegistryManager.java
│   │   │       │   ├── DynRegPayloads.java
│   │   │       │   ├── RegistrySerializer.java
│   │   │       │   └── WeightedDynamicRegistry.java
│   │   │       ├── network/
│   │   │       │   ├── ClientPayloadHandler.java
│   │   │       │   ├── PayloadHelper.java
│   │   │       │   └── PayloadProvider.java
│   │   │       ├── registry/
│   │   │       │   └── DeferredHelper.java
│   │   │       └── util/
│   │   │           ├── CachedObject.java
│   │   │           ├── EnchantmentUtils.java
│   │   │           ├── Offset.java
│   │   │           ├── PlaceboUtil.java
│   │   │           └── StepFunction.java
│   │   └── resources/
│   │       ├── apotheosis.mixins.json
│   │       ├── fabric.mod.json
│   │       └── assets/apothic_attributes/
│   │           ├── lang/ (en_us, zh_cn, etc.)
│   │           ├── sounds/ & sounds.json
│   │           └── textures/gui/ (attributes_gui.png, attribute_component.png, sprites/)
│   ├── client/
│   │   ├── java/dev/shadowsoffire/
│   │   │   ├── apotheosis/client/
│   │   │   │   ├── ApotheosisClient.java
│   │   │   │   ├── attributes/
│   │   │   │   │   ├── AttributeModifierComponent.java
│   │   │   │   │   ├── AttributesClientTooltipHandler.java
│   │   │   │   │   ├── AttributesGui.java
│   │   │   │   │   ├── AttributesGuiHolder.java
│   │   │   │   │   ├── ButtonPlacement.java
│   │   │   │   │   ├── ModifierSource.java
│   │   │   │   │   └── ModifierSourceType.java
│   │   │   │   ├── config/
│   │   │   │   │   └── ApotheosisConfigScreen.java
│   │   │   │   └── mixin/
│   │   │   │       ├── AbstractContainerScreenAccessor.java
│   │   │   │       ├── AbstractContainerScreenMixin.java
│   │   │   │       ├── AbstractRecipeBookScreenAccessor.java
│   │   │   │       └── InventoryScreenMixin.java
│   │   │   └── placebo/
│   │   │       ├── PlaceboClient.java
│   │   │       └── client/
│   │   │           └── PlaceboClientPayloadHandler.java
│   │   └── resources/
│   │       └── apotheosis.client.mixins.json
│   └── test/java/dev/shadowsoffire/
│       ├── apotheosis/
│       │   ├── attributes/
│       │   │   └── AttributesTest.java
│       │   └── config/
│       │       ├── ApotheosisConfigTest.java
│       │       ├── ClientConfigTest.java
│       │       ├── ConfigMigrationTest.java
│       │       └── ServerConfigTest.java
│       └── placebo/
│           ├── DeferredHelperTest.java
│           ├── DynamicRegistryTest.java
│           ├── EnchantmentUtilsTest.java
│           └── StepFunctionTest.java
```

---

## 2. Key Findings & Vanilla Overlap Resolution

### Custom Attributes Inventory
All 21 custom attributes from pinned Apothic Attributes (26.1.2) were analyzed against Minecraft 26.3 source and registered under `apothic_attributes`:
- `armor_pierce` [0, 1000]
- `armor_shred` [0, 2] (Percentage)
- `arrow_damage` [0, 10] (Percentage)
- `arrow_velocity` [0, 10] (Percentage)
- `cold_damage` [0, 1000]
- `crit_chance` [0, 10] (Percentage, base 0.05)
- `crit_damage` [0, 100] (Percentage, base 1.5)
- `current_hp_damage` [0, 1] (Percentage)
- `dodge_chance` [0, 1] (Percentage)
- `draw_speed` [0, 4] (Percentage, base 1.0)
- `experience_gained` [0, 1000] (Percentage, base 1.0)
- `fire_damage` [0, 1000]
- `ghost_health` [0, 1000]
- `healing_received` [0, 1000] (Percentage, base 1.0)
- `life_steal` [0, 10] (Percentage)
- `overheal` [0, 10] (Percentage)
- `projectile_damage` [0, 10] (Percentage, base 1.0)
- `prot_pierce` [0, 34]
- `prot_shred` [0, 0.90] (Percentage)
- `elytra_flight` [0, 1] (Boolean)
- `cooldown_reduction` [-10, 0.95] (Percentage)

All 21 attributes have `.setSyncable(true)` and are attached to all `LivingEntity` types using Fabric's `FabricDefaultAttributeRegistry.MODIFY` (`context.modifyAll`).

### Replaced / Obsolete Attributes
- **`apothic_attributes:mining_speed`**: Confirmed completely removed upstream in 3.1.0 in favor of vanilla `minecraft:mining_efficiency`.
- Stored item modifiers referencing `apothic_attributes:mining_speed` are migrated explicitly to `minecraft:mining_efficiency` via `AttributeHelper.migrateItemModifiers(ItemStack)` upon tooltip inspection or inventory load, preventing duplicate application.

### Fabric Formatting vs NeoForge Extensions
- In upstream NeoForge, attributes relied on NeoForge-only classes (`PercentageAttribute`, `BooleanAttribute`, and `IAttributeExtension.toValueComponent`).
- On Fabric, formatting is encapsulated in `AttributeFormatting.java`:
  - `FormatType.PERCENTAGE`: Formats values as percentages (e.g. `25%`, `+15%`).
  - `FormatType.BOOLEAN`: Formats boolean flags (`Enabled` / `Disabled`, `+Enabled` / `-Disabled`).
  - `FormatType.FLAT`: Formats numerical decimals (`10.5`, `+2`).
  - `buildFormula(...)`: Math representation of attribute calculations in tooltips.

### Attribute Inspection UI & 26.3 GUI Pipeline
- Ported `AttributesGui.java` to Minecraft 26.3:
  - Uses `GuiGraphicsExtractor` RenderPearl pipelines (`RenderPipelines.GUI_TEXTURED`, `blitSprite`, `fakeItem`, `text`).
  - 2D matrix transformation via `org.joml.Matrix3x2fStack`.
  - Injected into `InventoryScreen` via `InventoryScreenMixin` and `AbstractContainerScreenMixin` with zero raw OpenGL calls.
  - Interactive sword button positioned relative to the player preview box (`Offset` & `ButtonPlacement`).
  - Scrollable attribute list (max 6 visible at once), "Hide Unchanged" filter, and dynamic formula display.
  - Tooltip breakdown with modifier sources (`ModifierSource`), equipment slot provenance, and mob effect sprites via `Hud.getMobEffectSprite`.
  - Open extension point `ModifierSourceType.register()` allowing external accessory/curios mods to add custom modifier origins.

---

## 3. Verification & Test Results

1. **Automated Unit Tests**:
   - `AttributesTest.testAll21AttributesPresentAndSyncable()`: Passed.
   - `AttributesTest.testAttributeBoundsAndDefaults()`: Passed.
   - `AttributesTest.testPercentageAndBooleanFormatting()`: Passed.
   - `AttributesTest.testModifierAccumulationPrevention()`: Passed.
   - `AttributesTest.testObsoleteMiningSpeedMigration()`: Passed.
   - `AttributesTest.testHiddenAttributeFiltering()`: Passed.
   - **Total test suite**: 20 tests executed, 20 passed (`./gradlew test`).

2. **Compilation & Packaging**:
   - Aggregate compilation (`compileJava`, `compileClientJava`, `compileTestJava`) succeeded.
   - Aggregate jar build (`./gradlew build`) succeeded.
   - Duplicate class defense task (`verifyNoDuplicateClasses`) passed with 0 violations.

3. **Dedicated Server Launch**:
   - Executed `./gradlew runServer --args="--nogui"`.
   - Log output:
     `[Apotheosis/Attributes] Apothic Attributes module initialized successfully (21 attributes registered and attached)`
     `[Server thread/INFO] (Minecraft) Done (0.253s)! For help, type "help"`
   - Server shut down cleanly with `stop`.
   - Verified 100% absence of client GUI/screen classes on dedicated server.

---

## 4. Next Concrete Implementation Task

Proceed to **Milestone 5: Apothic Spawners Core**:
1. Implement spawner stat modifications hooking into Minecraft 26.3 `BaseSpawner` (min/max spawn delay, spawn count, max nearby entities, required player range, spawn range).
2. Implement spawner modifier items and data map definitions (sugar, clock, ghast tear, blaze rod, fermented spider eye, dragon egg, etc.).
3. Implement Silk Touch spawner capture and custom drops loot table handling.
4. Implement redstone control and condition-ignoring spawner modifiers.
