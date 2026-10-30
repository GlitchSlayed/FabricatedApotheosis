package dev.shadowsoffire.apotheosis.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;

public interface LootContextAccess {

    Identifier apoth_getQueriedLootTableId();

    void apoth_setQueriedLootTableId(Identifier id);

    static Identifier getQueriedLootTableId(LootContext ctx) {
        if (ctx instanceof LootContextAccess access) {
            Identifier id = access.apoth_getQueriedLootTableId();
            if (id != null) {
                return id;
            }
        }
        return Identifier.fromNamespaceAndPath("minecraft", "empty");
    }
}
