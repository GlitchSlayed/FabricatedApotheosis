package dev.shadowsoffire.apotheosis.loot.functions;

import java.util.Optional;

import dev.shadowsoffire.apotheosis.tiers.GenContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function base class that automatically deduces the {@link GenContext} from the {@link LootContext}.
 * <p>
 * If a gen context is not found, the function will not execute.
 */
public abstract class ContextualLootFunction extends LootItemConditionalFunction {

    protected ContextualLootFunction(Optional<Holder<LootItemCondition>> condition) {
        super(condition);
    }

    @Override
    protected final ItemStack run(ItemStack stack, LootContext ctx) {
        var gCtx = GenContext.forLoot(ctx);
        if (gCtx != null) {
            return this.run(stack, ctx, gCtx);
        }
        return stack;
    }

    protected abstract ItemStack run(ItemStack stack, LootContext ctx, GenContext gCtx);

}
