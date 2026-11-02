package dev.shadowsoffire.apotheosis.adventure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.gen.BossDungeonFeature;
import dev.shadowsoffire.apotheosis.gen.BossDungeonFeature2;
import dev.shadowsoffire.apotheosis.gen.RogueSpawnerFeature;
import dev.shadowsoffire.apotheosis.mobs.InvaderSpawnRules;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_spawners.compat.SpawnerRecipeCache;
import dev.shadowsoffire.placebo.json.WeightedItemStack;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

public class ProgressionAndWorldgenTests {

    @BeforeAll
    public static void setup() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        java.lang.reflect.Field frozenField = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
        frozenField.setAccessible(true);
        java.lang.reflect.Field intrusiveField = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        intrusiveField.setAccessible(true);

        for (net.minecraft.core.Registry<?> reg : net.minecraft.core.registries.BuiltInRegistries.REGISTRY) {
            if (reg instanceof net.minecraft.core.MappedRegistry<?> mapped) {
                frozenField.set(mapped, false);
                if (mapped == net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    || mapped == net.minecraft.core.registries.BuiltInRegistries.ITEM
                    || mapped == net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                    || mapped == net.minecraft.core.registries.BuiltInRegistries.FLUID
                    || mapped == net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE) {
                    if (intrusiveField.get(mapped) == null) {
                        intrusiveField.set(mapped, new java.util.IdentityHashMap<>());
                    }
                }
            }
        }

        var compMap = net.minecraft.core.component.DataComponentMap.builder()
            .set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 64)
            .build();
        for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            try {
                item.builtInRegistryHolder().components();
            } catch (Exception e) {
                item.builtInRegistryHolder().bindComponents(compMap);
            }
        }
    }

    @Test
    @DisplayName("Verify WorldTier ordering and unlock progression")
    public void testWorldTierOrderingAndProgression() {
        WorldTier[] tiers = WorldTier.values();
        assertEquals(5, tiers.length);
        assertEquals(WorldTier.HAVEN, tiers[0]);
        assertEquals(WorldTier.FRONTIER, tiers[1]);
        assertEquals(WorldTier.ASCENT, tiers[2]);
        assertEquals(WorldTier.SUMMIT, tiers[3]);
        assertEquals(WorldTier.PINNACLE, tiers[4]);

        assertTrue(WorldTier.PINNACLE.isAtLeast(WorldTier.HAVEN));
        assertTrue(WorldTier.PINNACLE.isAtLeast(WorldTier.SUMMIT));
        assertFalse(WorldTier.HAVEN.isAtLeast(WorldTier.FRONTIER));

        assertEquals(WorldTier.HAVEN, WorldTier.byName("haven"));
        assertEquals(WorldTier.PINNACLE, WorldTier.byName("pinnacle"));
        assertEquals(WorldTier.HAVEN, WorldTier.byName("unknown_fallback"));
    }

    @Test
    @DisplayName("Verify tier bonuses apply and remove without modifier accumulation")
    public void testTierBonusApplicationAndCleanRemoval() {
        AttributeMap attributes = new AttributeMap(net.minecraft.world.entity.LivingEntity.createLivingAttributes().build());
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> maxHealth = Attributes.MAX_HEALTH;
        AttributeInstance inst = attributes.getInstance(maxHealth);
        assertNotNull(inst);
        double baseValue = inst.getBaseValue();

        Identifier testModifierId = Apotheosis.loc("tier_bonus_health");
        AttributeModifier mod1 = new AttributeModifier(testModifierId, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        // Apply Haven/Ascent bonus
        inst.addOrReplacePermanentModifier(mod1);
        assertEquals(baseValue * 1.5, inst.getValue(), 0.001);

        // Transition: Remove old bonus, apply Pinnacle bonus (1.0 = +100%)
        inst.removeModifier(testModifierId);
        AttributeModifier mod2 = new AttributeModifier(testModifierId, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        inst.addOrReplacePermanentModifier(mod2);
        assertEquals(baseValue * 2.0, inst.getValue(), 0.001);

        // Transition back to Haven: Remove bonus
        inst.removeModifier(testModifierId);
        assertEquals(baseValue, inst.getValue(), 0.001, "Modifier must not accumulate after removal");
    }

    @Test
    @DisplayName("Verify Spawner Recipe Cache reconciles standalone and Apotheosis progression recipes")
    public void testSpawnerRecipeCacheReconciliation() {
        Identifier standaloneRecipe = Identifier.fromNamespaceAndPath("apothic_spawners", "modifier/spawn_count");
        Identifier progressionRecipe = Identifier.fromNamespaceAndPath("apotheosis", "modifier/spawn_count_tier_2");

        // When Adventure is enabled, standalone recipes are suppressed in favor of tiered ones
        assertTrue(SpawnerRecipeCache.shouldFilterRecipe(standaloneRecipe, true),
            "Standalone spawner modifier recipes should be filtered out when Adventure module is active");
        assertFalse(SpawnerRecipeCache.shouldFilterRecipe(progressionRecipe, true),
            "Apotheosis progression spawner recipes should remain active when Adventure module is active");

        // When Adventure is disabled (standalone spawners), standalone recipes are kept
        assertFalse(SpawnerRecipeCache.shouldFilterRecipe(standaloneRecipe, false),
            "Standalone recipes must be preserved when Adventure module is disabled");
    }

    @Test
    @DisplayName("Verify Invader Spawn Rules resolve per dimension")
    public void testInvaderSpawnRules() {
        InvaderSpawnRules overworldRules = InvaderSpawnRules.getRules(BuiltinDimensionTypes.OVERWORLD.identifier());
        assertNotNull(overworldRules);
        assertEquals(SurfaceType.NEEDS_SKY_OR_SAME_VERTICAL_SLICE, overworldRules.surfaceType());
        assertTrue(overworldRules.spawnChances().containsKey(WorldTier.HAVEN));
        assertTrue(overworldRules.spawnChances().get(WorldTier.FRONTIER) > 0);

        InvaderSpawnRules netherRules = InvaderSpawnRules.getRules(BuiltinDimensionTypes.NETHER.identifier());
        assertNotNull(netherRules);
        assertEquals(SurfaceType.ANY, netherRules.surfaceType());
    }

    @Test
    @DisplayName("Verify Feature MapCodecs encode and decode correctly")
    public void testFeatureMapCodecs() {
        BossDungeonFeature f1 = new BossDungeonFeature(0.75f);
        var json1 = BossDungeonFeature.CODEC.codec().encodeStart(JsonOps.INSTANCE, f1).getOrThrow();
        assertEquals(0.75f, json1.getAsJsonObject().get("success_chance").getAsFloat(), 0.001);

        BossDungeonFeature decoded1 = BossDungeonFeature.CODEC.codec().parse(JsonOps.INSTANCE, json1).getOrThrow();
        assertEquals(0.75f, decoded1.successChance(), 0.001);

        BossDungeonFeature2 f2 = new BossDungeonFeature2(0.5f);
        var json2 = BossDungeonFeature2.CODEC.codec().encodeStart(JsonOps.INSTANCE, f2).getOrThrow();
        assertEquals(0.5f, json2.getAsJsonObject().get("success_chance").getAsFloat(), 0.001);

        RogueSpawnerFeature f3 = new RogueSpawnerFeature(0.25f);
        var json3 = RogueSpawnerFeature.CODEC.codec().encodeStart(JsonOps.INSTANCE, f3).getOrThrow();
        assertEquals(0.25f, json3.getAsJsonObject().get("success_chance").getAsFloat(), 0.001);

        RogueSpawnerFeature decoded3 = RogueSpawnerFeature.CODEC.codec().parse(JsonOps.INSTANCE, json3).getOrThrow();
        assertEquals(0.25f, decoded3.successChance(), 0.001);

        // Also test default decoding
        RogueSpawnerFeature decodedDef = RogueSpawnerFeature.CODEC.codec().parse(JsonOps.INSTANCE, new com.google.gson.JsonObject()).getOrThrow();
        assertEquals(1.0f, decodedDef.successChance(), 0.001);
    }

    @Test
    @DisplayName("Verify GearSet serialization round-trip")
    public void testGearSetCodec() {
        ItemStackTemplate swordTemplate = ItemStackTemplate.fromStack(new ItemStack(Items.DIAMOND_SWORD));
        WeightedItemStack mainhand = new WeightedItemStack(Optional.of(swordTemplate), 10, 0.05f);

        GearSet set = new GearSet(50, 2.5f, List.of(mainhand), Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        var json = GearSet.CODEC.encodeStart(JsonOps.INSTANCE, set).getOrThrow();
        GearSet decoded = GearSet.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

        assertEquals(50, decoded.getWeight());
        assertEquals(2.5f, decoded.getQuality(), 0.001);
        assertEquals(1, decoded.mainhands().size());
        assertEquals(10, decoded.mainhands().get(0).weight());
        assertEquals(0.05f, decoded.mainhands().get(0).dropChance(), 0.001);
    }
}
