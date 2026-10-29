# Save Compatibility & Upgrade Policy

> [!IMPORTANT]
> The requirements and verification procedures documented here represent **planned policies and verification checks**, not completed implementation. Existing legacy migration code remains in place pending future review and removal.

---

## 1. Scope and Boundary Policy

1. **No External Migration Required**:
   - Migration from upstream NeoForge Apotheosis, Zenith, alternate Fabric ports, legacy `.cfg` configurations, or unrelated historical versions is **out of scope and not required**.
2. **Fork-Internal Version Compatibility**:
   - Backward compatibility **is strictly required** between future releases of **this Fabric fork** and worlds generated using earlier supported versions of this fork.
3. **Future Minecraft Target Preparation (e.g. 26.4)**:
   - Preparing for future versions (such as Minecraft 26.4) requires maintaining stable persistent schemas and validating upgrades against representative worlds once the target is officially available.
   - **Do not claim 26.4 compatibility in advance.**

---

## 2. Persistence Requirements

- **Stable Persistent Registry Identifiers**:
  - All blocks, items, entities, attributes, enchantments, and dynamic registry entries must retain consistent, deterministic registry identifiers across versions.
- **Explicit Schema Versioning & Internal Migration**:
  - Saved data formats owned by this fork (data components, custom block entity NBT, player capabilities) must include explicit schema versioning.
  - When changes to internal formats are necessary, provide deterministic migrations from supported earlier versions of this fork.
- **Comprehensive Content Preservation**:
  - The following state must be preserved across supported upgrades without loss or truncation:
    - Items and custom data components (affixes, gems, sockets, tomes).
    - Enchantment Library point totals and stored enchantment levels.
    - Spawner state (modifiers, delay, spawn counts, player range, redstone control flags).
    - Workstation inventories and active container states.
    - Player progression, advancements, and unlocked stats.
- **No Silent Reset or Deletion**:
  - Unresolved, damaged, or incompatible saved data must never be silently cleared or reset to defaults. Diagnostic logs must clearly identify the affected element and context.
- **Resilience to Module Disabling**:
  - Disabling an ecosystem module via configuration must never silently destroy or wipe existing persisted data in the world. Blocks, items, or entity data associated with disabled modules should remain intact on disk for when the module is re-enabled.

---

## 3. Upgrade Verification Procedures

- **World Upgrade Testing**:
  - Upgrades must be verified using copied representative test worlds.
  - Verification workflow:
    1. Load existing world in the new mod version.
    2. Inspect retained state across all registered workstations, libraries, spawners, and equipped items.
    3. Perform active gameplay (combining, enchanting, modifying spawners).
    4. Generate new chunks to verify worldgen and feature injection stability.
    5. Save, close, and reopen the world to confirm persistence round-tripping.
- **Backup Prerequisite & Downgrades**:
  - World backups are strictly required prior to running upgrades.
  - **Downgrade support is not promised or supported.**
