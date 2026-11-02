# Milestone 10: Client Feature Checklist & UI/Rendering Verification

This document provides a comprehensive audit, status matrix, architecture verification, and reproducible test guide for all client-facing systems in the Fabricated Apotheosis ecosystem on Minecraft 26.3 Fabric.

---

## 1. Client-Facing Subsystem Checklist

| Subsystem / Component | Implementation Class | UI Controls & Behavior | Audit & Verification Status |
| :--- | :--- | :--- | :--- |
| **Attributes GUI** | `AttributesGui.java`<br>`InventoryScreenMixin.java` | Sword toggle button on inventory screen, scrolling list of player attributes, categories, search/filter, dynamic offset to avoid recipe book and status effect overlap. | **Verified (Automated + Code Audit)**<br>Dynamic layout checks recipe book state via `AbstractRecipeBookScreenAccessor`. Tooltip and formatting verified. |
| **Enchanting Table** | `ApothEnchantmentScreen.java`<br>`EnchantTableRendererMixin.java` | Eterna/Quanta/Arcana/Clues sidebar, 3 clue slots, infusion indicator, level & lapis requirements, custom book texture variants. | **Verified (Code Audit + Fixed)**<br>Fixed NeoForge translatable key to Fabric-native `info.apothic_enchanting.limited_enchantability`. |
| **Raven Enchanting Table** | `RavenEnchantmentScreen.java` | Interactive stat adjustment sliders for Eterna, Quanta, Arcana; drag & click bounds clamp correctly. | **Verified (Code Audit)**<br>Slider math clamped to valid bounds; mouse drag and click bounds verified. |
| **Enchantment Info** | `EnchantingInfoScreen.java` | Level slider, weight/chance computation, discoverable/lootable metadata labels, parent return on ESC. | **Verified (Code Audit)**<br>`onClose()` properly restores parent screen. |
| **Enchantment Library** | `EnchLibraryScreen.java` | Text search input with right-click clear, scrollbar dragging & mouse wheel, point bars, shift-extract max levels, stonecutter click sound, `DrawsOnLeft` side tooltip. | **Verified (Code Audit)**<br>Text field captures keyboard focus without closing inventory; left-side tooltip clamps safely. |
| **Reforging Table** | `ReforgingScreen.java`<br>`ReforgingTableTileRenderer.java` | Placed item animation ticking (`reforge_animation.png`), pulsating ghost items, 3 reforge tier choices with dynamic sigil/material/level costs on left panel. | **Verified (Code Audit)**<br>Sound effects, item animation tick down, and radial slot button selection match server authoritative transactions. |
| **Salvaging Table** | `SalvagingScreen.java` | 12-slot input matrix, dynamic recipe matching, consolidated output preview with gray ghost items, batch salvage action button. | **Verified (Code Audit + Fixed)**<br>Corrected input slot loop boundary from 15 to 12 so output slots are never checked as inputs. |
| **Augmenting Table** | `AugmentingScreen.java`<br>`AugmentingTableTileRenderer.java` | Custom dropdown list (`AffixDropList`), upgrading button with level cap tooltip, rerolling button with paginated potential rerolls tooltip preview, panel frame renderer. | **Verified (Code Audit)**<br>Scroll events route to open dropdown list; item state cache invalidates on stack changes. |
| **Gem Cutting Table** | `GemCuttingScreen.java` | Upgrade button active status bound to recipe matcher, amethyst break sound cascade during craft. | **Verified (Code Audit)**<br>Client-side tickable sound instance with alternating pitch verified. |
| **Gem Case & Storage** | `GemCaseScreen.java`<br>`GemCaseTileRenderer.java` | Left-panel upgrade materials drawer with `getExclusionAreas()` for EMI/REI/JEI, 18 gem select buttons, scrollbar, ghost item outlines for unowned purities. | **Verified (Code Audit)**<br>Exclusion area accurately bounds (-65, 16, 65, 193). Filter box captures search text. |
| **World Tier Selection** | `WorldTierSelectScreen.java` | Custom mountain background, 5 animated tier buttons, sword difficulty meters, activate button with confirmation state, tutorial launch button. | **Verified (Code Audit)**<br>Tier animations step ticks per frame; activation button enforces server synchronization. |
| **World Tier Details** | `WorldTierDetailScreen.java` | 3-column scroll cards displaying loot rarity chances, gem purity distributions, and monster augment attributes. Return button and ESC handling. | **Verified (Code Audit + Fixed)**<br>Added `onClose()` override to gracefully return to `WorldTierSelectScreen` on ESC key. |
| **World Tier Tutorial** | `WorldTierTutorialScreen.java` | 6 tutorial stage overlays, Next/Prev/Skip buttons, ESC key handling, step-by-step guidance. | **Verified (Code Audit + Fixed)**<br>All 6 tutorial stage textures confirmed present; `onClose()` properly exits tutorial to parent screen. |
| **Dropped Affix Beams** | `BeamRenderer.java`<br>`AffixItemEffectRenderer.java`<br>`ItemEntityRendererMixin.java` | Vertical beacon beam with inner beam and outer rotating glow cylinder colored by rarity; grows in over 15 ticks. | **Verified (Implementation Complete)**<br>Decoupled render state extraction and geometry submission; uses 26.3 `submitCustomGeometry` with `ApothRenderTypes.BEAM`. Centered cleanly without block offset. |
| **Dropped Affix Shadows** | `ShadowRenderer.java` | Block-conforming polygonal shadows under dropped affix items with rarity tinting and animation frame support. | **Verified (Code Audit)**<br>Uses `RenderTypes.entityShadow` with voxel intersection clamping. |
| **Rarity Particles** | `RarityParticle.java`<br>`AffixItemEffectRenderer.java` | Ambient colored sparkle particles rising from grounded affix items; rate-limited via entity attachment. | **Verified (Code Audit)**<br>Tick handler in `END_CLIENT_TICK` rate-limits per item; particles clear on chunk/world unload. |
| **Item Comparisons** | `EquipmentComparePositioner.java`<br>`AdventureKeys.java` | Side-by-side tooltip comparison when hovering equipment while holding `AdventureKeys.COMPARE_EQUIPMENT` (or Shift). Clamps to screen edge. | **Verified (Code Audit)**<br>Boundary calculations guarantee tooltip fits on screen across GUI scales (1x, 2x, 3x, 4x, Auto). |
| **Configuration (YACL)** | `ApotheosisConfigScreen.java`<br>`ModMenuIntegration.java` | Full YetAnotherConfigLib interface accessible via ModMenu or pause screen; Client Preferences (affix effects, particles, deltas) and Server Gameplay Rules. | **Verified (Automated + Code Audit)**<br>Added "Enable Affix Item Effects" toggle; remote multiplayer disables server settings while keeping client preferences editable. |

---

## 2. Rendering & Resource Architecture Verification

### 2.1 Minecraft 26.3 Render Pipeline Compliance
- **Zero Raw GPU / Driver Calls**: Grepped and audited codebase for `GL11`, `GL30`, `GlStateManager`, `Vulkan`, or driver imports. None are used for ordinary rendering. All rendering operates through Mojang's high-level abstractions:
  - `GuiGraphicsExtractor` / `PoseStack` for 2D screens and widgets.
  - `RenderPipelines.GUI_TEXTURED` for GUI sprites.
  - `SubmitNodeCollector` and `collector.submitCustomGeometry(...)` for 3D world geometry.
  - `RenderTypes.eyes(...)` and `RenderTypes.entityShadow(...)` for custom world render types.
- **Render State Extraction Boundaries**:
  - Entity rendering strictly separates state extraction (`extractRenderState`) from geometry submission (`submit`).
  - `ItemEntityRenderStateMixin` stores `RarityRenderData`, color, and ground state without mutating or querying world state during submission.
  - Block entity renderers (`AugmentingTableTileRenderer`, `ReforgingTableTileRenderer`, `GemCaseTileRenderer`) follow standard block entity rendering conventions.
- **Resource Ownership & Texture Lifetimes**:
  - All 23 GUI textures, 6 tutorial overlays, and 5 tier button textures are static resources in `assets/`.
  - No per-item or per-frame textures, shaders, or pipeline instances are allocated dynamically.
  - Beam and shadow render types are memoized via `Util.memoize` in `ApothRenderTypes`.

---

## 3. Dedicated-Server Class Loading & Isolation

- **Client Code Isolation**:
  - All screens, client mixins, renderers, keybindings, and YACL UI classes reside exclusively in `src/client/java/`.
  - `fabric.mod.json` separates client entrypoints (`apotheosis:client`, `apothic_enchanting:client`, `apothic_spawners:client`) from main common entrypoints.
  - `ApotheosisConfig.java` in common code contains zero client references or UI imports.
- **Multiplayer Server Verification**:
  - Automated tests verify dedicated-server boot and shutdown without encountering `ClassNotFoundException`, `NoClassDefFoundError`, or client class access.

---

## 4. Reproducible In-Game Verification Checklist

For visual execution and interactive verification in a running Minecraft 26.3 client:

### A. Workstation Screens & Focus
1. **Reforging Table**:
   - Place a damaged or un-affixed weapon in slot 0. Observe placement sound and 8-tick gear animation (`textures/gui/reforge_animation.png`).
   - Hover over each of the 3 reforge choices; verify costs (sigils, materials, levels) display clearly in the left slide-out panel.
   - Click a valid choice: verify server-side deduction of materials and item transformation.
2. **Salvaging Table**:
   - Place up to 12 items across the 4x3 input grid.
   - Verify the 6 output slots display grayed-out preview items representing minimum-to-maximum yield ranges.
   - Click "Salvage"; verify inputs are consumed and resulting affix materials populate output slots.
3. **Augmenting Table**:
   - Place an affix item in the main slot. Open the affix dropdown list; select an affix.
   - Click "Upgrade" or "Reroll"; verify preview tooltip updates.
4. **Enchantment Library**:
   - Type in the search box; verify inventory key (E) does not close the screen while typing.
   - Right-click search box to clear. Drag scrollbar and scroll mouse wheel.
   - Shift-click an entry to extract maximum affordable level to an enchanted book.
5. **Gem Case**:
   - Place gems into input slot; verify they auto-deposit with amethyst sound.
   - Check left-hand upgrade panel; verify upgrade buttons enable when valid materials and matching purity gems are present.

### B. World Tiers & Progression UI
1. **Tier Selection (`WorldTierSelectScreen`)**:
   - Press the World Tier key (default `O` / configured key) or run `/apoth tier`.
   - Cycle through Haven, Frontier, Ascent, Summit, Pinnacle. Verify mountain animated backgrounds and sword difficulty ratings update.
   - Click "?" button to launch tutorial. Step through all 6 pages using Next/Prev, then press ESC or Skip; verify seamless return to select screen.
   - Click "Show Detailed Info"; examine the 3-column cards for drop rates and enemy augments; press ESC to return.

### C. Visual Effects & Loot Beams
1. **Dropped Affix Items**:
   - Drop an affix item of each rarity (Common, Uncommon, Rare, Epic, Mythic, Ancient).
   - Observe vertical beam growing upward over 15 ticks with color matching the rarity border.
   - Observe colored sparkle particles gently floating upward from the ground around the item.
   - In Video Settings / YACL Config, toggle "Enable Affix Item Effects" OFF; verify beams and particles instantly cease rendering. Toggle back ON; verify they resume.

### D. Multi-Scale GUI & Window Resize
1. Change GUI Scale from Auto -> 1x -> 2x -> 3x -> 4x across small (854x480) and full-screen (1920x1080) window sizes.
2. Open the Inventory Screen with Attributes panel open. Toggle Recipe Book open/closed. Verify the Attributes panel flips or slides cleanly without overlapping potion icons or recipe categories.

---

## 5. Explicit Unverified Cases (Live GPU Execution)
- **Vulkan Backend**: While all rendering uses public Mojang abstractions (`SubmitNodeCollector`, `RenderPipelines`) free of OpenGL-specific assumptions, physical execution under VulkanMod requires an interactive Vulkan GPU environment and remains explicitly marked **Unverified (No GPU)**.
- **Extreme Item Clusters (>500 dropped affix items in a single chunk)**: Headless test suites verify rate limiting and zero buffer leaks, but interactive frame-rate profiling with hundreds of concurrent loot beams requires live client profiling.
