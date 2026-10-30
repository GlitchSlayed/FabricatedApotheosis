package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.shadowsoffire.apotheosis.util.LootContextAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;

@Mixin(LootContext.class)
public class LootContextMixin implements LootContextAccess {

    @Unique
    private Identifier apoth$queriedLootTableId;

    @Override
    public Identifier apoth_getQueriedLootTableId() {
        return this.apoth$queriedLootTableId;
    }

    @Override
    public void apoth_setQueriedLootTableId(Identifier id) {
        this.apoth$queriedLootTableId = id;
    }
}
