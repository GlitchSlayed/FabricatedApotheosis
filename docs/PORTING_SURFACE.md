# Porting Surface Inventory — Apotheosis Ecosystem

## 1. Inventory Summary Across Approved Modules

This inventory is generated from direct codebase scans across the pinned upstream 26.1.2 sources in `upstream/`:
- **Placebo**: 107 Java classes
- **Apothic-Attributes**: 49 Java classes
- **Apothic-Enchanting**: 102 Java classes
- **Apothic-Spawners**: 32 Java classes
- **Apotheosis Core & Adventure**: 342 Java classes
- **Total Surface**: 632 Java classes

| Category | Total Count | Representative References | Porting Strategy & Current Status |
| :--- | :--- | :--- | :--- |
| **Registries & Namespaces** | 61 registration points | `ALObjects.java`, `Apoth.java`, `Ench.java`, `ASObjects.java` | **PORTED & VERIFIED**: Unified under standard Fabric/vanilla `Registry.register` and `DeferredHelper` with `ResourceKey` collision protection |
| **NeoForge Events** | 27 subscriber classes | `ApothEnchEvents.java`, `AdventureEvents.java`, `ASEvents.java` | **PORTED & VERIFIED**: Mapped to Fabric API lifecycle/entity/attack callbacks and 9-phase combat mixin pipeline |
| **Capabilities / Attachments** | 8 attachment points | `RavenTableStats.java`, `FestiveAffix.java`, `EnchantmentTableItemHandler.java` | **PORTED & VERIFIED**: Fabric Transfer API (`Storage<ItemVariant>`), Fabric Data Attachment API |
| **Custom Packets / Networking** | 21 payload records | `ConfigPayload.java`, `CluePayload.java`, `BossSpawnPayload.java` | **PORTED & VERIFIED**: Fabric Networking API (`PayloadTypeRegistry`, `ServerPlayNetworking`, `ClientPlayNetworking`) |
| **Configuration Files** | 18 config structures | `ASConfig.java`, `ALConfig.java`, `EnchConfig.java`, `AdventureConfig.java` | **PORTED & VERIFIED**: Unified server-authoritative JSON config (`apotheosis.json`) with client YACL screen (`apotheosis-client.json`) |
| **Dynamic Registries / Codecs**| 12 dynamic codecs | `DynamicRegistry.java`, `SpawnerModifier.java`, `Affix.java`, `Gem.java` | **PORTED & VERIFIED**: Vanilla 26.3 `RegistryOps` and Codecs via Fabric Resource Reloaders with atomic reload staging |
| **Custom Recipes & Conditions**| 16 recipe serializers | `EnchantingRecipe.java`, `SpawnerModifierRecipe.java`, `PotionReforgingRecipe.java` | **PORTED & VERIFIED**: Vanilla 26.3 Recipe API + `fabric:components` custom ingredients |
| **Loot Integration** | 9 loot modifier hooks | `SpawnerBlockDropsMixin.java`, `LootTableModifier.java`, `AffixLootEntry.java` | **PORTED**: Fabric Loot API v3 (`LootTableEvents.MODIFY`) |
| **World Generation** | 7 worldgen features | `BossDungeonFeature.java`, `RogueSpawnerFeature.java` | Vanilla 26.3 `ConfiguredFeature` / `PlacedFeature` JSON definitions |
| **Access Transformers / Mixins**| 5 AT files, 61 mixins | `accesstransformer.cfg` in each module; `EnchantmentMenuMixin.java` | **PORTED & VERIFIED**: Transpiled to Fabric Access Wideners (`apotheosis.accesswidener`); narrow targeted mixins |
| **Coremods / JS / ASM** | 0 JS coremods | Legacy ASM replaced by Mixins in 26.1.2 | **PORTED & VERIFIED**: Data component effects and narrow mixins cleanly replace legacy coremod patterns |
| **Client Screens & Renderers** | 24 client classes | `EnchantmentScreen.java`, `ReforgingScreen.java`, `BossBarRenderer.java` | **PORTED & VERIFIED**: Mojang 26.3 screen abstractions + RenderPearl for GPU blend states |
| **Save Formats & Migration** | 6 NBT/component types | Spawner modifiers in `BlockEntityTag`, item components, player capability NBT | **PORTED & VERIFIED**: Vanilla 26.3 `DataComponents.CUSTOM_DATA` and Fabric Data Attachments |

---

## 2. Detailed Porting Analysis by Subsystem

### 2.1 Apothic Enchanting "Coremod" Audit
- **Historical Background**: In Minecraft 1.12–1.16, Apotheosis used coremods / bytecode ASM to modify vanilla enchantment level caps (raising levels up to 100 and overriding hardcoded constant caps).
- **26.1.2 Status**: Bytecode inspection confirms there are **0 JavaScript or legacy ASM coremods** in Apothic Enchanting 26.1.2.
- **Current Mechanism**:
  - `accesstransformer.cfg` exposes `EnchantmentMenu` fields (`enchantmentSeed`, `access`, `random`, `enchantSlots`) and `Enchantment$Builder`.
  - 18 SpongePowered Mixins (e.g. `AnvilMenuMixin`, `EnchantmentHelperMixin`, `EnchantmentMenuMixin`) inject into enchantment calculation and clamping methods.
- **Fabric 26.3 Migration**:
  - Replace the NeoForge access transformer rules with a standard Fabric `.accesswidener` file (`apotheosis.accesswidener`).
  - Target Minecraft 26.3's data-driven enchantment system, ensuring level limits and cost calculations apply cleanly.

### 2.2 Capabilities & Data Attachments
- Upstream uses NeoForge attachments (`AttachmentType`) and capabilities (`ItemCapability`, `BlockCapability`).
- Examples:
  - `RavenTableStats` attached to players or enchanting tables.
  - `EnchantmentTableItemHandler` exposing item transfer to enchanting tables.
- **Fabric Migration**:
  - Use Fabric's Data Attachment API (`fabric-data-attachment-api-v1`) for persistent player and block entity data.
  - Use Fabric Transfer API (`fabric-transfer-api-v1`) for inventory automation.

### 2.3 Networking & Payloads
- Upstream uses `net.minecraft.network.protocol.common.custom.CustomPacketPayload` with NeoForge's payload registrar.
- Because Minecraft 26.1.2 and 26.3 vanilla natively use `CustomPacketPayload`, every payload class (`CritParticlePayload`, `CluePayload`, `StatsPayload`, `AttributeTogglesPayload`) has identical serialization logic.
- **Fabric Migration**: Register using Fabric API's `PayloadTypeRegistry.playS2C()` and `PayloadTypeRegistry.playC2S()`.

### 2.4 Configuration System
- Upstream uses Placebo's custom `Configuration` parser reading forge-style `.cfg` files.
- **Fabric Migration**:
  - Server logic uses standard, lightweight Gson-backed JSON configuration (`config/apotheosis.json`).
  - Client interface uses YetAnotherConfigLib (YACL) with real-time in-game visual editing.
