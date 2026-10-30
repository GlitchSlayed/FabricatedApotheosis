package dev.shadowsoffire.apotheosis.loot.entry;

import java.util.Optional;
import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.tiers.GenContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.SingleEntryContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class ContextualLootPoolEntry extends SingleEntryContainerBase {

    protected ContextualLootPoolEntry(int weight, int quality, Optional<Holder<LootItemCondition>> conditions, Optional<Holder<LootItemFunction>> functions) {
        super(weight, quality, conditions, functions);
    }

    @Override
    protected final void createItemStack(Consumer<ItemStack> stackConsumer, LootContext ctx) {
        var gCtx = GenContext.forLoot(ctx);
        if (gCtx != null) {
            this.createItemStack(stackConsumer, ctx, gCtx);
        }
    }

    protected abstract void createItemStack(Consumer<ItemStack> stackConsumer, LootContext ctx, GenContext gCtx);

}
