package net.neoforged.neoforge.common.loot;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootModifier implements IGlobalLootModifier {

    protected final LootItemCondition[] conditions;
    protected final int priority;

    protected LootModifier(LootItemCondition[] conditions, int priority) {
        this.conditions = conditions;
        this.priority = priority;
    }

    protected LootModifier(LootItemCondition[] conditions) {
        this(conditions, 0);
    }

    public static <T extends LootModifier> P2<Mu<T>, LootItemCondition[], Integer> codecStart(Instance<T> inst) {
        return inst.group(
            LootItemCondition.DIRECT_CODEC.listOf().xmap(l -> l.toArray(new LootItemCondition[0]), java.util.List::of).fieldOf("conditions").forGetter(m -> m.conditions),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(m -> m.priority)
        );
    }

    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        for (LootItemCondition condition : this.conditions) {
            if (!condition.test(ctx)) return loot;
        }
        return doApply(loot, ctx);
    }

    protected abstract ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx);
}
