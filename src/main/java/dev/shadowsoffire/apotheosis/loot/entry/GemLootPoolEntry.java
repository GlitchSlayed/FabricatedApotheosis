package dev.shadowsoffire.apotheosis.loot.entry;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class GemLootPoolEntry extends ContextualLootPoolEntry {

    public static final MapCodec<GemLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(a -> a.purities),
        PlaceboCodecs.setOf(GemRegistry.INSTANCE.holderCodec()).optionalFieldOf("gems", Set.of()).forGetter(a -> a.gems))
        .and(UniformContainerBase.uniformFields(inst))
        .apply(inst, GemLootPoolEntry::new));

    private final Set<Purity> purities;
    private final Set<DynamicHolder<Gem>> gems;

    private transient boolean validated = false;

    public GemLootPoolEntry(Set<Purity> purities, Set<DynamicHolder<Gem>> gems, int weight, int quality, Optional<Holder<LootItemCondition>> conditions, Optional<Holder<LootItemFunction>> functions) {
        super(weight, quality, conditions, functions);
        this.purities = purities;
        this.gems = gems;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> list, LootContext ctx, GenContext gCtx) {
        if (!this.validated) {
            this.gems.forEach(this::checkBound);
            this.validated = true;
        }

        Gem gem;
        if (!this.gems.isEmpty()) {
            gem = GemRegistry.INSTANCE.getRandomItemFromHolders(gCtx, this.gems);
        }
        else {
            gem = GemRegistry.INSTANCE.getRandomItem(gCtx);
        }

        Purity purity = Purity.random(gCtx, this.purities);
        ItemStack stack = gem.toStack(purity);
        list.accept(stack);
    }

    @Override
    public MapCodec<GemLootPoolEntry> codec() {
        return CODEC;
    }

    public static UniformContainerBase.Builder<?> builder(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
        return UniformContainerBase.simpleBuilder(ctor(purities, gems));
    }

    private static UniformContainerBase.EntryConstructor ctor(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
        return (weight, quality, conditions, functions) -> new GemLootPoolEntry(purities, gems, weight, quality, conditions, functions);
    }

    private void checkBound(DynamicHolder<Gem> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("A GemLootPoolEntry failed to resolve the Gem {}!", holder.getId());
        }
    }
}
