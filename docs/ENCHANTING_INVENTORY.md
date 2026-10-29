# Apothic Enchanting — Inventory & Transformation Audit

This document inventories every registered block, item, block entity, enchantment, effect, recipe, menu, screen, command, and data definition from the pinned Apothic Enchanting baseline (`3.0.0` / 26.1.2) before implementation, and records the replacement strategy for all NeoForge coremods/ASM transformations.

---

## 1. Registered Objects Inventory

### 1.1 Blocks (`apothic_enchanting`)
| ID | Class | Properties / Notes |
| :--- | :--- | :--- |
| `apothic_enchanting_table` | `ApothicEnchantingTableBlock` | Eterna/Quanta/Arcana table, custom GUI, replaces vanilla table |
| `raven_enchanting_table` | `RavenEnchantingTableBlock` | Enchantment selection & rerolling table with fixed stat configuration |
| `basic_bookshelf` | `TypedShelfBlock` | Wood shelf, 1.5F hardness, Eterna: 2.0 |
| `beeshelf` | `TypedShelfBlock` | Yellow shelf, 0.75F hardness, Arcana: 15% |
| `blazing_hellshelf` | `TypedShelfBlock` | Black shelf, 1.5F hardness, flame particles |
| `crystal_seashelf` | `TypedShelfBlock` | Cyan shelf, 1.5F hardness, water particles |
| `deepshelf` | `TypedShelfBlock` | Black shelf, 2.5F hardness, sculk particles |
| `dormant_deepshelf` | `TypedShelfBlock` | Black shelf, 2.5F hardness, sculk particles |
| `draconic_endshelf` | `TypedShelfBlock` | End shelf, 5.0F hardness, end particles |
| `echoing_deepshelf` | `TypedShelfBlock` | Black shelf, 2.5F hardness, sculk particles |
| `echoing_sculkshelf` | `SculkShelfBlock` | Random ticking, sculk shelf |
| `ender_library` | `EnchLibraryBlock` | Ender library, capacity 31 levels ($2^{30}$ points) |
| `endshelf` | `TypedShelfBlock` | Sand shelf, 4.5F hardness, end particles |
| `filtering_shelf` | `FilteringShelfBlock` | Shelf filtering enchantment rolls by tag/type |
| `geode_shelf` | `GeodeShelfBlock` | Stone shelf, 1.75F hardness |
| `glowing_hellshelf` | `TypedShelfBlock` | Black shelf, 1.5F hardness, flame particles |
| `heart_seashelf` | `TypedShelfBlock` | Cyan shelf, 1.5F hardness, water particles |
| `hellshelf` | `TypedShelfBlock` | Black shelf, 1.5F hardness, flame particles |
| `infused_hellshelf` | `TypedShelfBlock` | Glowing black shelf, flame particles |
| `infused_seashelf` | `TypedShelfBlock` | Glowing cyan shelf, water particles |
| `library` | `EnchLibraryBlock` | Basic library, capacity 16 levels ($2^{15}$ points) |
| `melonshelf` | `TypedShelfBlock` | Green shelf, 0.75F hardness |
| `pearl_endshelf` | `TypedShelfBlock` | Sand shelf, 4.5F hardness, end particles |
| `seashelf` | `TypedShelfBlock` | Cyan shelf, 1.5F hardness, water particles |
| `sightshelf` | `TypedShelfBlock` | Black shelf, 1.5F hardness, clue expansion |
| `sightshelf_t2` | `TypedShelfBlock` | Black shelf, 1.5F hardness, clue expansion tier 2 |
| `soul_touched_deepshelf` | `TypedShelfBlock` | Black shelf, 2.5F hardness, sculk particles |
| `soul_touched_sculkshelf`| `SculkShelfBlock` | Random ticking, sculk shelf |
| `stoneshelf` | `TypedShelfBlock` | Stone shelf, 1.75F hardness |
| `treasure_shelf` | `TreasureShelfBlock` | Stone shelf, allows rolling treasure enchantments |

### 1.2 Block Entities
| ID | Block Entity Class | Block Bound | Notes |
| :--- | :--- | :--- | :--- |
| `library` | `BasicLibraryTile` | `library` | Stores points for basic enchantments (up to lvl 16) |
| `ender_library` | `EnderLibraryTile` | `ender_library` | Stores points for all enchantments (up to lvl 31) |
| `filtering_shelf` | `FilteringShelfTile` | `filtering_shelf` | Inventory container for filtering criteria |

### 1.3 Items
- **BlockItems**: All 30 block items corresponding to blocks above (with custom items `LibraryBlockItem`, `FilteringShelfBlockItem`, `TooltipBlockItem`, `GlowyBlockItem`).
- **Tomes**:
  - `helmet_tome`, `chestplate_tome`, `leggings_tome`, `boots_tome`, `weapon_tome`, `bow_tome`, `pickaxe_tome`, `fishing_tome`, `other_tome`: Category-restricted tomes (`TomeItem`).
  - `scrap_tome`: Extracts a random fraction of enchantments, destroying the source item (`ScrappingTomeItem`).
  - `improved_scrap_tome`: Keeps higher levels and rolls more enchantments (`ImprovedScrappingTomeItem`).
  - `extraction_tome`: Safely extracts all enchantments without destroying the source item (`ExtractionTomeItem`).
- **Tools & Materials**:
  - `prismatic_web`: Removes curses from items in an anvil.
  - `inert_trident`: Crafting component for infusion.
  - `infused_breath`: Dragon breath infused with arcane power.
  - `warden_tendril`: Rare drop from Wardens for deepshelf crafting.
  - `flimsy_ender_lead`, `ender_lead`, `occult_ender_lead`: Entity capture leashes with component metadata.
- **Music Discs**:
  - `music_disc_eterna`, `music_disc_quanta`, `music_disc_arcana`.

### 1.4 Enchantment Effects (Data Components)
| Component ID | Value Type | Usage |
| :--- | :--- | :--- |
| `berserking` | `BerserkingComponent` | Health reduction & mob effects on hit (`Berserker's Fury`) |
| `bonemeal_crops` | `LevelBasedValue` | Durability cost per right-click crop boost (`Nature's Blessing`) |
| `chainsaw` | `Unit` | Fells connected tree logs on block break (`Chainsaw`) |
| `chromatic` | `Unit` | Randomizes wool color when shearing sheep (`Chromatic`) |
| `crescendo` | `List<ConditionalEffect<EnchantmentValueEffect>>` | Additional shots without reloading crossbow (`Crescendo of Bolts`) |
| `drops_to_xp` | `List<ConditionalEffect<EnchantmentValueEffect>>` | Converts mob item drops to experience (`Knowledge of the Ages`) |
| `earths_boon` | `BoonComponent` | Bonus raw ore / gem drops when mining stone/deepslate (`Boon of the Earth`) |
| `exploitation` | `Unit` | Doubles wool drops, damages sheep (`Worker Exploitation`) |
| `extra_loot_roll` | `List<ConditionalEffect<EnchantmentValueEffect>>` | Multiplies mob loot rolls (`Scavenger`) |
| `growth_serum` | `Float` | Chance to immediately regrow sheep wool (`Growth Serum`) |
| `miners_fervor` | `LevelBasedValue` | Mining speed acceleration scaling (`Miner's Fervor`) |
| `reflective` | `ReflectiveComponent` | Returns damage to attacker when blocking (`Reflective Defenses`) |
| `repair_with_hp` | `List<ConditionalEffect<EnchantmentValueEffect>>` | Converts incoming healing to item durability (`Life Mending`) |
| `stable_footing` | `Unit` | Negates break speed penalty when flying (`Stable Footing`) |
| `tempting` | `Unit` | Makes animals follow the held item (`Tempting`) |

### 1.5 Custom Enchantments (20)
`berserkers_fury`, `boon_of_the_earth`, `chainsaw`, `chromatic`, `crescendo_of_bolts`, `endless_quiver`, `growth_serum`, `icy_thorns`, `infusion`, `knowledge_of_the_ages`, `life_mending`, `miners_fervor`, `natures_blessing`, `rebounding`, `reflective_defenses`, `scavenger`, `shield_bash`, `spearfishing`, `stable_footing`, `tempting`, `worker_exploitation`.

### 1.6 Recipes & Menus
- **Recipes**: `apothic_enchanting:infusion`, `apothic_enchanting:keep_nbt_infusion`.
- **Menus**:
  - `enchanting_table`: `ApothEnchantmentMenu` (positions, stats, clues, infusion detection).
  - `raven_enchanting_table`: `RavenEnchantmentMenu` (rerolling, stats, selection).
  - `library`: `EnchLibraryContainer` (insertion, extraction, level selection).

---

## 2. Coremod & ASM Transformation Audit & Fabric Replacements

NeoForge upstream used five coremod transformer classes in `dev.shadowsoffire.apothic_enchanting.coremods`. Here is the exact audit and how each is replaced on Fabric:

| Upstream Transformer | Target Classes / Methods | Original ASM Behavior | Fabric Replacement Implementation |
| :--- | :--- | :--- | :--- |
| **`EnchInfoRedirector`** | `EnchantCommand`, `AnvilMenu`, `CreativeModeTabs`, `Enchantment` | Replaced virtual calls to `Enchantment.getMaxLevel()` with `INVOKESTATIC EnchHooks.getMaxLevel(Enchantment)`. | Implemented `AnvilMenuMixin` (`@ModifyConstant(ordinal = 2, require = 1)` lifting cap to `Integer.MAX_VALUE` and `@Redirect` to `EnchHooks.getMaxAnvilCombineLevel(ench)`), and `EnchantCommandMixin` (`@Redirect` to `EnchHooks.getMaxLevel(ench)`). |
| **`EnchInfoLootRedirector`**| `EnchantRandomlyFunction`, `EnchantmentHelper`, `SingleEnchantment` | Replaced virtual calls to `Enchantment.getMaxLevel()` in loot generation with `INVOKESTATIC EnchHooks.getMaxLootLevel(Enchantment)`. | Implemented `EnchantRandomlyFunctionMixin` and `SingleEnchantmentMixin` via `@Redirect` on `Enchantment.getMaxLevel()` to `EnchHooks.getMaxLootLevel(ench)`. |
| **`FishingHookLureTransformer`** | `FishingHook.catchingFish(BlockPos)` | Replaced the final `PUTFIELD timeUntilHooked` calculation with `EnchHooks.getTicksCaughtDelay(FishingHook)` to prevent delay from being $\le 0$ when `lureSpeed` is very high. | Implemented `FishingHookMixin` via `@Redirect` on `timeUntilLured` invoking `EnchHooks.getTicksCaughtDelay(this)`. |
| **`EnchantmentCallRewriter`** | Bytecode utility | Assisted ASM method rewriting. | Eliminated entirely; replaced with declarative Fabric Mixins. |
| **`ApothEnchCoreMod`** | NeoForge `ClassProcessorProvider` | Coremod registration. | Eliminated entirely; zero ASM plugins or external transformers needed. |

---

## 3. Storage, Anvil & Item Safety Invariants

1. **Shift-Click & Full Inventory**: `EnchLibraryContainer` must safely handle shift-clicks and full inventory edge cases without voiding points or books.
2. **Block Destruction**: When an Enchantment Library or Filtering Shelf is broken, its internal contents (points/items) are either retained (Ender Library uses persistent data/attachments) or dropped safely into the world (Basic Library / Filtering Shelf).
3. **Anvil Combinations**: Anvil combination costs must respect `maxAnvilCombineLevel` and allow leveling up beyond vanilla caps if configured.
4. **Disconnect & World Unload**: Library points must serialize deterministically using `CompoundTag` codecs.
