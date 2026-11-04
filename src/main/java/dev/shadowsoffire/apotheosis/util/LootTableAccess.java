package dev.shadowsoffire.apotheosis.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public interface LootTableAccess {

    ResourceKey<LootTable> apoth_getTableKey();

    void apoth_setTableKey(ResourceKey<LootTable> key);
}
