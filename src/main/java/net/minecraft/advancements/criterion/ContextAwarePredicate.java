package net.minecraft.advancements.criterion;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ContextAwarePredicate(List<Holder<LootItemCondition>> conditions) {

    public static final Codec<Holder<LootItemCondition>> CONDITION_CODEC = Codec.PASSTHROUGH.flatXmap(
        dynamic -> {
            Dynamic<?> d = dynamic;
            if (d.get("condition").result().isPresent() && d.get("type").result().isEmpty()) {
                d = d.set("type", d.get("condition").result().get());
            }
            return LootItemCondition.CODEC.parse(d);
        },
        holder -> LootItemCondition.CODEC.encodeStart(JsonOps.INSTANCE, holder).map(json -> new Dynamic<>(JsonOps.INSTANCE, json))
    );

    public static final Codec<ContextAwarePredicate> CODEC = CONDITION_CODEC.listOf()
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
