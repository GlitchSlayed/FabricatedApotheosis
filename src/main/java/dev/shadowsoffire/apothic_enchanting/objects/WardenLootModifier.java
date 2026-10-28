package dev.shadowsoffire.apothic_enchanting.objects;

import dev.shadowsoffire.apothic_enchanting.Ench;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class WardenLootModifier {

    public static final ResourceKey<LootTable> WARDEN_TABLE = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/warden"));

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (WARDEN_TABLE.equals(key)) {
                LootPool.Builder pool = LootPool.lootPool()
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Ench.Items.WARDEN_TENDRIL.value()))
                    .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries.lookupOrThrow(Registries.ENCHANTMENT), ContextFloatProviders.between(0.0F, 1.0F)));
                tableBuilder.pool(pool.build());
            }
        });
    }
}
