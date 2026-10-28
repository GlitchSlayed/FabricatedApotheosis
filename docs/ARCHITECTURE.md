# System Architecture — Fabricated Apotheosis

## 1. High-Level System Architecture

Fabricated Apotheosis is engineered as a **single, unified Fabric distributable** for Minecraft 26.3 that consolidates the historically multi-jar NeoForge Apotheosis ecosystem into one cohesive mod artifact under mod ID `apotheosis`.

```mermaid
flowchart TD
    subgraph Client ["Client Environment Only"]
        YACL["YACL Configuration Screen (ApotheosisConfigScreen)"]
        ClientInit["ApotheosisClient"]
        ClientRender["RenderPearl & Vanilla Renderers"]
        ModMenuHook["ModMenu Integration"]
    end

    subgraph Common ["Common / Server & Client"]
        Init["Apotheosis ModInitializer"]
        Config["Server-Authoritative Config (ApotheosisConfig)"]
        
        subgraph Modules ["Separated Internal Modules"]
            Platform["platform: PlatformHelper"]
            Placebo["placebo: PlaceboSupport"]
            Attributes["attributes: AttributesModule"]
            Enchanting["enchanting: EnchantingModule"]
            Spawners["spawners: SpawnersModule"]
            Adventure["adventure: AdventureModule"]
            Integrations["integrations: IntegrationsModule"]
            Docs["docs: DocsSupport"]
        end
    end

    subgraph ServerRuntime ["Dedicated Server Execution"]
        ServerLoop["Minecraft Server Game Loop"]
        ServerWorld["ServerLevel & BlockEntities"]
    end

    ClientInit --> Init
    Init --> Config
    Init --> Modules
    YACL -->|Visual Editing| Config
    ServerLoop --> Config
    ServerLoop --> Modules
```

---

## 2. Internal Module Boundaries

The code is strictly partitioned into distinct internal packages to prevent architectural spaghetti while delivering a single combined JAR:

1. **`platform`** (`dev.shadowsoffire.apotheosis.platform`):
   - Isolates Fabric Loader specific calls, environment detection (`isClient()`, `isDedicatedServer()`), and platform abstractions.
2. **`placebo`** (`dev.shadowsoffire.apotheosis.placebo`):
   - Only the minimal, required utilities from upstream Placebo (codecs, dynamic registry wrappers, recipe helpers).
3. **`attributes`** (`dev.shadowsoffire.apothic_attributes`):
   - Registration of the 21 Apothic attributes, combat calculations, armor piercing, life steal, and attribute formatting.
   - 9-phase server-authoritative combat pipeline handling dodge, attack scaling, critical strikes, penetration, auxiliary damage, absorption, health damage, overheal/life steal, and experience scaling.
4. **`enchanting`** (`dev.shadowsoffire.apothic_enchanting`):
   - Table statistics engine (`EnchantmentTableStats`, `EnchantingStatRegistry`), supporting Eterna, Quanta, Arcana, Rectification, Clues, and Filtered stats across 30 block types.
   - Enchanting table menus (`ApothEnchantmentMenu`, `RavenEnchantmentMenu`) and level calculation formulas matching upstream.
   - Anvil overhaul (`AnvilMenuMixin`, `AnvilEvents`, `TomeItem`) removing level 40 caps, enabling optimal XP progression, and curse removal via Prismatic Web.
   - Enchantment Library (`EnchLibraryBlock`, `EnchLibraryTile`, `BasicLibraryTile`, `EnderLibraryTile`) with transactional $2^{N-1}$ point conversion and Fabric Transfer API (`Storage<ItemVariant>`).
   - 20 custom enchantments and 14 data-driven enchantment effect components.
5. **`spawners`** (`dev.shadowsoffire.apothic_spawners`):
   - Monster spawner modification, delay scaling, redstone controls, spawn conditions, and Silk Touch harvesting.
6. **`adventure`** (`dev.shadowsoffire.apotheosis.adventure`):
   - Affixes, gems, socketing, and boss mob generation.
7. **`client`** (`dev.shadowsoffire.apothic_attributes.client`, `dev.shadowsoffire.apothic_enchanting.client`, `dev.shadowsoffire.apotheosis.client`):
   - Client-only screens (`AttributesGui`, `ApothEnchantmentScreen`, `RavenEnchantmentScreen`, `EnchLibraryScreen`, `EnchantingInfoScreen`), tooltips, particle factories, keybindings, and rendering hooks.
8. **`integrations`** (`dev.shadowsoffire.apotheosis.integrations`):
   - Optional external mod hooks (Patchouli, ModMenu, Jade, EMI/JEI).
9. **`docs`** (`dev.shadowsoffire.apotheosis.docs`):
   - Guidebook integration and Chronicle of Shadows documentation structures.

---

## 3. Server Authority & Dedicated Server Safety

- **Authoritative Gameplay**: All spawner modifications, attribute calculations, loot determinations, and inventory interactions execute strictly on the logical server. The client only sends interaction packets or receives state synchronization.
- **Client Class Segregation**:
  - The project utilizes Loom's `splitEnvironmentSourceSets()`.
  - All classes referencing GUI, screens, YACL, Minecraft client classes, or rendering live strictly in `src/client/java`.
  - The dedicated server classpath NEVER loads client classes. Dedicated server launch has been verified with 100% clean startup and shutdown.

---

## 4. Apothic Enchanting Pipeline & Transactional Storage

```mermaid
flowchart TD
    subgraph TableStats ["Enchanting Statistics Engine"]
        Blocks["Block / Shelf Detection (AABB search)"] --> Reg["EnchantingStatRegistry (Dynamic Data)"]
        Reg --> Stats["Calculated Stats: Eterna, Quanta, Arcana, Rectification, Clues"]
        Stats --> Menu["ApothEnchantmentMenu / RavenEnchantmentMenu"]
    end

    subgraph Calculations ["Enchanting Formulas"]
        Menu --> Seed["Player Seed + Item Enchantability"]
        Seed --> Levels["Levels 1-100 Selection & Clues"]
        Levels --> EnchResult["Enchantment Result Generation"]
    end

    subgraph Library ["Enchantment Library Architecture"]
        Tile["EnchLibraryTile (Base / Ender)"]
        TransferAPI["Fabric Transfer API (Storage<ItemVariant>)"] <--> Tile
        Points["Point Conversion: 2^(Level - 1) Points per Level"] <--> Tile
        Snapshot["SnapshotParticipant (Transactional Rollback on Fail)"] <--> Points
    end
```

---

## 4. Configuration Model

```mermaid
sequenceDiagram
    participant Player as Player / Admin
    participant YACL as YACL GUI (Client)
    participant Config as ApotheosisConfig (Common)
    participant Disk as config/apotheosis.json

    Note over Config,Disk: Dedicated Server Startup
    Config->>Disk: Load from file or write defaults
    Disk-->>Config: JSON Config Object

    Note over Player,YACL: In-Game Configuration
    Player->>YACL: Opens YACL Screen via ModMenu / Pause
    YACL->>Config: Reads current values
    Player->>YACL: Adjusts options & clicks Save
    YACL->>Config: Writes updated values
    Config->>Disk: Serializes to apotheosis.json
```

---

## 5. Rendering Architecture

- **High-Level Integration First**: Mod models, blocks, and items utilize Minecraft 26.3 native models (`assets/apotheosis/models/`) and block entity renderers.
- **RenderPearl Abstraction**: Where custom GPU blend states, custom shaders, or direct GPU buffers are required (e.g. boss health bars, gem socketing overlays), use Minecraft 26.3 public RenderPearl APIs.
- **No Raw GL/Vulkan Imports**: Avoid raw `GL11`, `GlStateManager`, or platform-specific backend classes to ensure cross-backend compatibility across OpenGL and Vulkan.
