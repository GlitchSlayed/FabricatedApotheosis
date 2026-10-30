package dev.shadowsoffire.apotheosis.loot.entry;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.loot.functions.ReforgeItemFunction;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * An Affix Loot Pool Entry allows explicit injection of affix loot into a loot table.
 */
public class AffixLootPoolEntry extends ContextualLootPoolEntry {

    public static final MapCodec<AffixLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
        PlaceboCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries))
        .and(UniformContainerBase.uniformFields(inst))
        .apply(inst, AffixLootPoolEntry::new));

    private final Set<DynamicHolder<LootRarity>> rarities;
    private final Set<DynamicHolder<AffixLootEntry>> entries;

    private transient boolean validated = false;

    public AffixLootPoolEntry(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries, int weight, int quality, Optional<Holder<LootItemCondition>> conditions, Optional<Holder<LootItemFunction>> functions) {
        super(weight, quality, conditions, functions);
        this.rarities = rarities;
        this.entries = entries;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> list, LootContext ctx, GenContext gCtx) {
        if (!this.validated) {
            this.rarities.forEach(AffixLootPoolEntry::checkBound);
            this.entries.forEach(AffixLootPoolEntry::checkBound);
            this.validated = true;
        }

        ItemStack stack = LootController.createAffixItemFromPools(this.rarities, this.entries, gCtx);
        if (!stack.isEmpty()) {
            NameHelper.setItemName(ctx.getRandom(), stack);
            list.accept(stack);
        }
    }

    @Override
    public MapCodec<AffixLootPoolEntry> codec() {
        return CODEC;
    }

    public static UniformContainerBase.Builder<?> builder(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
        return UniformContainerBase.simpleBuilder(ctor(rarities, entries));
    }

    private static UniformContainerBase.EntryConstructor ctor(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
        return (weight, quality, conditions, functions) -> new AffixLootPoolEntry(rarities, entries, weight, quality, conditions, functions);
    }

    private static void checkBound(DynamicHolder<?> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("Failed to resolve dynamic holder for AffixLootPoolEntry: {}", holder.getId());
        }
    }
}
