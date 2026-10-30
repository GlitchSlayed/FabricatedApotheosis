# Apotheosis Adventure: Feature Inventory & Specification (Milestone 8)

- **Module**: `apotheosis:adventure` (bundled into `fabricated-apotheosis` ecosystem distributable)
- **Target Platform**: Minecraft 26.3 Fabric (Fabric Loader 0.19.5, Loom 1.17.21, YACL 3.9.6)
- **Upstream Baseline**: Pinned `Apotheosis` 26.1.2 commit `95632f1`

---

## 1. Registry & Object Inventory

### 1.1 Built-in Registries & Loot Categories
| Identifier / Key | Class / Type | Slots / Group | Description |
| :--- | :--- | :--- | :--- |
| `apotheosis:loot_category` | `Registry<LootCategory>` | - | Built-in registry for item categories. |
| `apotheosis:bow` | `LootCategory` | `HAND` | Bows & Crossbows. |
| `apotheosis:breaker` | `LootCategory` | `MAINHAND` | Pickaxes & Shovels. |
| `apotheosis:helmet` | `LootCategory` | `HEAD` | Helmets (excluding Carved Pumpkin & Skulls). |
| `apotheosis:chestplate` | `LootCategory` | `CHEST` | Chestplates. |
| `apotheosis:leggings` | `LootCategory` | `LEGS` | Leggings. |
| `apotheosis:boots` | `LootCategory` | `FEET` | Boots. |
| `apotheosis:shield` | `LootCategory` | `HAND` | Shields. |
| `apotheosis:trident` | `LootCategory` | `MAINHAND` | Tridents. |
| `apotheosis:melee_weapon` | `LootCategory` | `MAINHAND` | Swords & tools with attack damage > 1. |
| `apotheosis:shears` | `LootCategory` | `MAINHAND` | Shears. |
| `apotheosis:none` | `LootCategory` | `ANY` | Fallback / non-affixed category. |

### 1.2 Dynamic Registries
| Registry Key | Class | Codec | Description |
| :--- | :--- | :--- | :--- |
| `apotheosis:rarities` | `DynamicRegistry<LootRarity>` | `LootRarity.CODEC` | Item rarities (Common, Uncommon, Rare, Epic, Mythic, Ancient). |
| `apotheosis:affixes` | `DynamicRegistry<Affix>` | `Affix.CODEC` | Affix definitions, stat bonuses, and effects. |
| `apotheosis:gems` | `DynamicRegistry<Gem>` | `Gem.CODEC` | Gem types, purity bonuses, and slot restrictions. |
| `apotheosis:purity_weights` | `DynamicRegistry<PurityWeights>` | `PurityWeights.CODEC` | Purity drop distribution tables. |
| `apotheosis:rarity_override` | `DynamicRegistry<RarityOverride>` | `RarityOverride.CODEC` | Fixed rarity rules per item/entity. |

### 1.3 Data Components
| Component Key | Value Type | Codec / Network | Purpose |
| :--- | :--- | :--- | :--- |
| `apotheosis:affixes` | `ItemAffixes` | Codec & StreamCodec | Affix instances keyed by `Affix` holder with level float. |
| `apotheosis:rarity` | `DynamicHolder<LootRarity>` | Codec & StreamCodec | Bound rarity of the affixed item. |
| `apotheosis:affix_name` | `Component` | Codec & StreamCodec | Formatted / generated affixed item name with styling. |
| `apotheosis:sockets` | `Integer` (0..16) | VarInt | Available gem sockets on the item. |
| `apotheosis:socketed_gems` | `ItemContainerContents` | Codec & StreamCodec | Stored gem item stacks inserted into sockets. |
| `apotheosis:gem` | `DynamicHolder<Gem>` | Codec & StreamCodec | Gem identity on `GemItem` stacks. |
| `apotheosis:purity` | `Purity` (Enum) | Codec & StreamCodec | Gem purity tier (Cracked -> Perfect). |
| `apotheosis:durability_bonus` | `Float` (0..1) | Float | Durability multiplier from affixes or reforging. |
| `apotheosis:charm_enabled` | `Boolean` | Bool | Active state toggle for `PotionCharmItem`. |
| `apotheosis:stoneforming_target`| `Block` | Registry Codec | Targeted block for stoneforming affix mining. |
| `apotheosis:malice_marker` | `Boolean` | Bool | Flag indicating item has been touched by Sigil of Malice. |
| `apotheosis:touched_by_malice` | `Integer` | VarInt | Count of malice modifications applied. |
| `apotheosis:render_alpha` | `Float` | Float | Visual rendering alpha interpolation. |
| `apotheosis:from_chest` | `Boolean` | Bool | Provenance tracking flag (world chest). |
| `apotheosis:from_trader` | `Boolean` | Bool | Provenance tracking flag (wandering trader). |
| `apotheosis:from_boss` | `Boolean` | Bool | Provenance tracking flag (boss entity). |
| `apotheosis:from_mob` | `Boolean` | Bool | Provenance tracking flag (monster drop). |

### 1.4 Workstation Blocks & Block Entities
| Registry Key | Block Class | Tile Class | Menu / UI | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `apotheosis:simple_reforging_table` | `ReforgingTableBlock` | `ReforgingTableTile` | `ReforgingMenu` / `Screen` | Tier-1 Reforging station (Common to Rare gear). |
| `apotheosis:reforging_table` | `ReforgingTableBlock` | `ReforgingTableTile` | `ReforgingMenu` / `Screen` | Full Reforging station (All rarities, Sigils of Rebirth/Enhancement). |
| `apotheosis:salvaging_table` | `SalvagingTableBlock` | `SalvagingTableTile` | `SalvagingMenu` / `Screen` | Breaks down affixed gear into rarity materials and gem dust. |
| `apotheosis:gem_cutting_table` | `GemCuttingBlock` | None | `GemCuttingMenu` / `Screen` | Upgrades gem purity using gem dust. |
| `apotheosis:augmenting_table` | `AugmentingTableBlock`| `AugmentingTableTile`| `AugmentingMenu` / `Screen` | Re-rolls individual affix values or upgrades affix tiers. |
| `apotheosis:gem_case` | `GemCaseBlock` | `BasicGemCaseTile` | `GemCaseMenu` / `Screen` | Mass storage and categorization for up to 32,767 gems. |
| `apotheosis:ender_gem_case` | `GemCaseBlock` | `EnderGemCaseTile` | `GemCaseMenu` / `Screen` | Ender-linked mass storage for up to 2,147,483,647 gems. |

### 1.5 Items & Materials
| Registry Key | Class | Rarity | Purpose / Mechanics |
| :--- | :--- | :--- | :--- |
| `apotheosis:mysterious_scrap_metal`| `SalvageItem` | Common | Common rarity crafting and reforging material. |
| `apotheosis:timeworn_fabric` | `SalvageItem` | Uncommon | Uncommon rarity crafting and reforging material. |
| `apotheosis:luminous_crystal_shard`| `SalvageItem` | Rare | Rare rarity crafting and reforging material. |
| `apotheosis:arcane_sands` | `SalvageItem` | Epic | Epic rarity crafting and reforging material. |
| `apotheosis:godforged_pearl` | `SalvageItem` | Mythic | Mythic rarity crafting and reforging material. |
| `apotheosis:god_fused_pearl` | `GlowyItem` | Epic | Advanced synthesis ingredient for endgame sigils. |
| `apotheosis:gem_dust` | `Item` | Common | Base currency for gem cutting, socketing, and reforging. |
| `apotheosis:gem_fused_slate` | `Item` | Common | Crafting component for gem cutting table and gem cases. |
| `apotheosis:gem` | `GemItem` | Variable | Sockets into gear; properties determined by `apotheosis:gem` & `purity`. |
| `apotheosis:potion_charm` | `PotionCharmItem` | Variable | Reusable active potion charm; tick recharge in brewing stand. |
| `apotheosis:sigil_of_socketing` | `TooltipItem` | Uncommon | Adds sockets to gear up to rarity maximums. |
| `apotheosis:sigil_of_withdrawal`| `TooltipItem` | Uncommon | Extracts socketed gems from an item without destroying either. |
| `apotheosis:sigil_of_rebirth` | `TooltipItem` | Uncommon | Re-rolls all affixes on an item in the Reforging Table. |
| `apotheosis:sigil_of_enhancement`| `TooltipItem`| Uncommon | Upgrades the rarity tier of an item in the Reforging Table. |
| `apotheosis:sigil_of_unnaming` | `TooltipItem` | Uncommon | Strips custom affixed prefix/suffix name, reverting to item base name. |
| `apotheosis:sigil_of_malice` | `TooltipItem` | Uncommon | Converts gear to Malice-touched with inverted, cursed, or altered traits. |
| `apotheosis:sigil_of_supremacy` | `TooltipItem` | Uncommon | Unlocks max-tier perfection rolls on affixes. |
| `apotheosis:iron_upgrade_smithing_template` | `SmithingTemplateItem` | Common | Tier upgrade template for vanilla iron gear. |
| `apotheosis:gold_upgrade_smithing_template` | `SmithingTemplateItem` | Common | Tier upgrade template for vanilla gold gear. |
| `apotheosis:diamond_upgrade_smithing_template` | `SmithingTemplateItem` | Common | Tier upgrade template for vanilla diamond gear. |
| `apotheosis:music_disc_flash` | `Item` | Rare | Custom music disc (Flash). |
| `apotheosis:music_disc_glimmer` | `Item` | Rare | Custom music disc (Glimmer). |
| `apotheosis:music_disc_shimmer` | `Item` | Rare | Custom music disc (Shimmer). |

---

## 2. Affix System Specification

### 2.1 Affix Categories & Data Definition
An `Affix` is registered dynamically in `apotheosis:affixes` via JSON:
- `categories`: Set of `LootCategory` tags/ids valid for the affix.
- `type`: `STAT` (provides attribute modifiers) or `EFFECT` (custom gameplay behavior).
- `rarities`: Map of valid `LootRarity` IDs to level values or ranges $[0.0, 1.0]$.
- `weight`: Selection probability weight.

### 2.2 Concrete Affix Types
| Class | Type | Mechanics |
| :--- | :--- | :--- |
| `AttributeAffix` | `STAT` | Maps to an entity attribute modifier scaled by level and slot. |
| `AttributeToggleAffix` | `STAT` | Provides toggleable attribute bonuses via player configuration. |
| `DamageReductionAffix` | `EFFECT` | Percentage damage reduction against specific damage types or tags. |
| `EnchantmentAffix` | `EFFECT` | Grants virtual enchantment levels while equipped. |
| `EnlightenedAffix` | `EFFECT` | Increases experience points dropped by killed entities. |
| `ExecutingAffix` | `EFFECT` | Instantly kills non-boss entities below a health threshold. |
| `FestiveAffix` | `EFFECT` | Causes mob deaths to spawn celebratory firework and candy drops. |
| `MagicalArrowAffix` | `EFFECT` | Converts bow/crossbow arrows to magical psychic damage ignoring armor. |
| `MobEffectAffix` | `EFFECT` | Applies status effects (poison, wither, slowness, weakness) on hit. |
| `MultiAttrAffix` | `STAT` | Provides multiple synchronized attribute modifiers simultaneously. |
| `OmneticAffix` | `EFFECT` | Allows tools to effectively harvest any block as if using pickaxe/axe/shovel. |
| `PsychicAffix` | `EFFECT` | Direct psychic damage bypassing physical armor. |
| `RadialAffix` | `EFFECT` | Breaks blocks in a 3x3 or 5x5 area with sneak toggle support. |
| `RetreatingAffix` | `EFFECT` | Grants speed and brief invisibility upon receiving heavy damage. |
| `SpectralShotAffix` | `EFFECT` | Periodically fires additional spectral arrows with no ammo consumption. |
| `StoneformingAffix` | `EFFECT` | Automatically smelts mined cobblestone/stone directly into smooth stone. |
| `TelepathicAffix` | `EFFECT` | Instantly deposits block drops and mob loot directly into player inventory. |
| `ThunderstruckAffix` | `EFFECT` | Summons lightning bolts on target on critical strike. |
| `CatalyzingAffix` | `EFFECT` | Spawns beneficial potion clouds on hit. |
| `CleavingAffix` | `EFFECT` | Deals sweeping edge damage with axes. |

---

## 3. Gem System Specification

### 3.1 Purity Hierarchy & Values
| Purity | Level | Step Factor | Typical Relative Power |
| :--- | :--- | :--- | :--- |
| `CRACKED` | 0 | 0.0 | 25% |
| `CHIPPED` | 1 | 0.2 | 40% |
| `FLAWED` | 2 | 0.4 | 60% |
| `REGULAR` | 3 | 0.6 | 80% |
| `FLAWLESS` | 4 | 0.8 | 100% (Baseline) |
| `PERFECT` | 5 | 1.0 | 125% |

### 3.2 Gem Bonuses & Uniqueness
- Each `Gem` defines a list of `GemBonus` entries mapped to item categories (`LootCategory`).
- Bonuses can be `AttributeBonus`, `DamageReductionBonus`, `DropTransformBonus`, `MultiAttrBonus`, or special (`AntiGravityArrowBonus`, `FrozenDropsBonus`).
- **Uniqueness Rules**:
  - A gem marked `unique: true` can only be socketed once per item.
  - Sockets cannot contain two gems providing mutually exclusive bonuses.

---

## 4. Socketing & Item Processing

### 4.1 Socket Limits per Rarity
- Sockets are bounded by rarity:
  - Common: 0–1
  - Uncommon: 1–2
  - Rare: 1–2
  - Epic: 2–3
  - Mythic: 3–4
  - Ancient: 4–5
- Sigil of Socketing (`AddSocketsRecipe`) can increase sockets up to the maximum permitted by the item's `LootRarity`.

### 4.2 Sockets & Gem Operations
- **Socketing**: Combining an item with available empty sockets and a `GemItem` in a Crafting Grid or Smithing Table inserts the gem into `apotheosis:socketed_gems`.
- **Withdrawal**: Combining an item containing gems with `apotheosis:sigil_of_withdrawal` extracts all socketed gems as individual `GemItem` stacks without damaging the host item or the gems.

---

## 5. Workstations & Economy

### 5.1 Reforging Table (`ReforgingMenu`)
- Inputs: Affixed Item, Rarity Material (matching rarity), Gem Dust, Experience Levels.
- Operation: Re-rolls all affixes on the item within its current rarity, or enhances rarity when using `Sigil of Enhancement`.
- Server authority: Full item and cost verification before mutating inventories.

### 5.2 Salvaging Table (`SalvagingMenu`)
- Inputs: Up to 6 affixed items.
- Operation: Destroys items to produce appropriate materials based on `LootRarity` and item category:
  - Armor/weapons -> scrap metal, fabric, crystal shards, sands, godforged pearls.
  - Gems -> Gem Dust based on `Purity`.
- Non-salvageable items are preserved.

### 5.3 Gem Cutting Table (`GemCuttingMenu`)
- Inputs: 2x Gems of identical ID and `Purity` $P$, Gem Dust.
- Output: 1x Gem of identical ID with upgraded `Purity` $P + 1$.
- Perfect gems cannot be upgraded further.

### 5.4 Gem Case (`GemCaseMenu`)
- Storage for mass gems with auto-stacking, filter by purity, search by gem ID, and withdrawal/deposit.

---

## 6. Milestone Scope Boundaries
- **In-Scope for Milestone 8**: Complete Adventure item/economy systems, affixes, gems, purity, sockets, workstations, recipes, sigils, charms, tooltips, commands, unit tests, and save-compatibility.
- **Deferred to Milestone 9**: World Tiers (`WorldTier`), Invader mob spawning, Boss dungeons, Rogue spawners, boss loot tables, and spawner runes.
