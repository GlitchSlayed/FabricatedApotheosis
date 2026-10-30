package net.minecraft.advancements.criterion;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ContextAwarePredicate(List<Holder<LootItemCondition>> conditions) {

    public static final Codec<ContextAwarePredicate> CODEC = LootItemCondition.CODEC.listOf()
        .xmap(ContextAwarePredicate::new, ContextAwarePredicate::conditions);

    public static ContextAwarePredicate create(LootItemCondition... conditions) {
        return new ContextAwarePredicate(Arrays.stream(conditions).map(Holder::direct).toList());
    }

    public boolean matches(LootContext context) {
        for (Holder<LootItemCondition> holder : this.conditions) {
            if (!holder.value().test(context)) {
                return false;
            }
        }
        return true;
    }
}
