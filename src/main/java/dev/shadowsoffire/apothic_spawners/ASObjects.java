package dev.shadowsoffire.apothic_spawners;

import dev.shadowsoffire.apothic_spawners.advancements.ModifierTrigger;
import dev.shadowsoffire.apothic_spawners.advancements.SpawnEggItemPredicate;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootTable;

public class ASObjects {

    private static final DeferredHelper HELPER = DeferredHelper.create(ApothicSpawners.MODID);

    public static final RecipeType<SpawnerModifier> SPAWNER_MODIFIER = HELPER.recipe("spawner_modifier");

    public static final Holder.Reference<RecipeSerializer<SpawnerModifier>> SPAWNER_MODIFIER_SERIALIZER = HELPER.recipeSerializer("spawner_modifier", () -> SpawnerModifier.SERIALIZER);

    public static final DataComponentType<Float> CAPTURING = HELPER.enchantmentEffect("capturing", builder -> builder.persistent(Codec.floatRange(0.001F, 1F)));

    public static final ResourceKey<Enchantment> CAPTURING_ENCH = ResourceKey.create(Registries.ENCHANTMENT, ApothicSpawners.loc("capturing"));

    public static final ModifierTrigger MODIFIER_TRIGGER = HELPER.criteriaTrigger("spawner_modifier", new ModifierTrigger());

    public static final TagKey<EntityType<?>> BLACKLISTED_FROM_SPAWNERS = TagKey.create(Registries.ENTITY_TYPE, ApothicSpawners.loc("blacklisted_from_spawners"));

    /**
     * Loot table rolled when an unstable spawner detonates.
     */
    public static final ResourceKey<LootTable> UNSTABLE_SPAWNER_LOOT = ResourceKey.create(Registries.LOOT_TABLE, ApothicSpawners.loc("gameplay/unstable_spawner"));

    static {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, ApothicSpawners.loc("spawn_egg"), new DataComponentPredicate.ConcreteType<>(SpawnEggItemPredicate.CODEC.codec()));
    }

    public static void bootstrap() {
        SpawnerStats.bootstrap();
    }
}
