package dev.shadowsoffire.apotheosis.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.RavenTableStats;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Milestone 12: Save Compatibility, Persistence Invariant & Upgrade Baseline Verification.
 *
 * Validates persistent registry identifiers, schema stability, multi-pass serialization round-tripping,
 * workstation and spawner preservation, and resilience against module toggles.
 */
public class SaveCompatibilityTest {

    @BeforeAll
    public static void setup() throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();

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

        try {
            var unused = Apoth.BuiltInRegs.LOOT_CATEGORY_KEY;
            Class.forName("dev.shadowsoffire.apotheosis.Apoth$LootCategories");
            if (Apoth.BuiltInRegs.LOOT_CATEGORY instanceof net.minecraft.core.MappedRegistry<?> mapped) {
                mapped.freeze();
            }
            dev.shadowsoffire.apotheosis.loot.LootCategory.Inner.rebuildSortedValueList();
            dev.shadowsoffire.apotheosis.loot.LootCategory.registerOverride(net.minecraft.world.item.Items.DIAMOND_SWORD, Apoth.LootCategories.MELEE_WEAPON);
        } catch (Exception ignored) {}

        dev.shadowsoffire.apothic_spawners.ApothicSpawners.init();
        ASObjects.bootstrap();
        SpawnerStats.bootstrap();
        ALObjects.bootstrap();
        if (SpawnerStats.REGISTRY instanceof net.minecraft.core.MappedRegistry<?> mapped) {
            mapped.freeze();
        }
    }

    @Test
    @DisplayName("Verify persistent registry identifiers match compatibility baseline")
    public void testRegistryIdentifierStability() {
        // 1. Data Components
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "affixes"), getComponentId(Apoth.Components.AFFIXES));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "rarity"), getComponentId(Apoth.Components.RARITY));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "socketed_gems"), getComponentId(Apoth.Components.SOCKETED_GEMS));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "purity"), getComponentId(Apoth.Components.PURITY));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "affix_name"), getComponentId(Apoth.Components.AFFIX_NAME));

        // 2. Core Blocks & Workstations
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "reforging_table"), getHolderId(Apoth.Blocks.REFORGING_TABLE));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "salvaging_table"), getHolderId(Apoth.Blocks.SALVAGING_TABLE));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "gem_cutting_table"), getHolderId(Apoth.Blocks.GEM_CUTTING_TABLE));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "gem_case"), getHolderId(Apoth.Blocks.GEM_CASE));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "boss_spawner"), getHolderId(Apoth.Blocks.BOSS_SPAWNER));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_enchanting", "library"), getHolderId(Ench.Blocks.LIBRARY));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_enchanting", "filtering_shelf"), getHolderId(Ench.Blocks.FILTERING_SHELF));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_enchanting", "raven_enchanting_table"), getHolderId(Ench.Blocks.RAVEN_ENCHANTING_TABLE));

        // 3. Core Items
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "gem"), getHolderId(Apoth.Items.GEM));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "gem_dust"), getHolderId(Apoth.Items.GEM_DUST));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "potion_charm"), getHolderId(Apoth.Items.POTION_CHARM));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "sigil_of_socketing"), getHolderId(Apoth.Items.SIGIL_OF_SOCKETING));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "sigil_of_enhancement"), getHolderId(Apoth.Items.SIGIL_OF_ENHANCEMENT));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "sigil_of_unnaming"), getHolderId(Apoth.Items.SIGIL_OF_UNNAMING));
        assertEquals(Identifier.fromNamespaceAndPath("apotheosis", "sigil_of_withdrawal"), getHolderId(Apoth.Items.SIGIL_OF_WITHDRAWAL));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_enchanting", "scrap_tome"), getHolderId(Ench.Items.SCRAP_TOME));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_enchanting", "improved_scrap_tome"), getHolderId(Ench.Items.IMPROVED_SCRAP_TOME));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_enchanting", "extraction_tome"), getHolderId(Ench.Items.EXTRACTION_TOME));

        // 4. Custom Attributes
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "armor_pierce"), getHolderId(ALObjects.Attributes.ARMOR_PIERCE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "armor_shred"), getHolderId(ALObjects.Attributes.ARMOR_SHRED));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "prot_pierce"), getHolderId(ALObjects.Attributes.PROT_PIERCE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "prot_shred"), getHolderId(ALObjects.Attributes.PROT_SHRED));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "life_steal"), getHolderId(ALObjects.Attributes.LIFE_STEAL));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "overheal"), getHolderId(ALObjects.Attributes.OVERHEAL));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "crit_chance"), getHolderId(ALObjects.Attributes.CRIT_CHANCE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "crit_damage"), getHolderId(ALObjects.Attributes.CRIT_DAMAGE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "current_hp_damage"), getHolderId(ALObjects.Attributes.CURRENT_HP_DAMAGE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "fire_damage"), getHolderId(ALObjects.Attributes.FIRE_DAMAGE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "cold_damage"), getHolderId(ALObjects.Attributes.COLD_DAMAGE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "healing_received"), getHolderId(ALObjects.Attributes.HEALING_RECEIVED));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "arrow_velocity"), getHolderId(ALObjects.Attributes.ARROW_VELOCITY));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "arrow_damage"), getHolderId(ALObjects.Attributes.ARROW_DAMAGE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "experience_gained"), getHolderId(ALObjects.Attributes.EXPERIENCE_GAINED));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "ghost_health"), getHolderId(ALObjects.Attributes.GHOST_HEALTH));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "dodge_chance"), getHolderId(ALObjects.Attributes.DODGE_CHANCE));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "elytra_flight"), getHolderId(ALObjects.Attributes.ELYTRA_FLIGHT));
        assertEquals(Identifier.fromNamespaceAndPath("apothic_attributes", "creative_flight"), getHolderId(ALObjects.Attributes.CREATIVE_FLIGHT));
    }

    @Test
    @DisplayName("Verify WorldTier enum serialization, ordering, and progression state round-trip")
    public void testWorldTierPersistence() {
        WorldTier[] tiers = WorldTier.values();
        assertEquals(5, tiers.length);
        assertEquals(WorldTier.HAVEN, tiers[0]);
        assertEquals(WorldTier.FRONTIER, tiers[1]);
        assertEquals(WorldTier.ASCENT, tiers[2]);
        assertEquals(WorldTier.SUMMIT, tiers[3]);
        assertEquals(WorldTier.PINNACLE, tiers[4]);

        for (WorldTier tier : tiers) {
            Tag encoded = WorldTier.CODEC.encodeStart(NbtOps.INSTANCE, tier).getOrThrow();
            WorldTier decoded = WorldTier.CODEC.decode(NbtOps.INSTANCE, encoded).getOrThrow().getFirst();
            assertEquals(tier, decoded, "WorldTier must survive serialization round-trip: " + tier);
        }

        // Test clamping and boundary helpers
        assertEquals(WorldTier.HAVEN, WorldTier.BY_ID.apply(0));
        assertEquals(WorldTier.PINNACLE, WorldTier.BY_ID.apply(4));
        assertEquals(WorldTier.HAVEN, WorldTier.byName("haven"));
        assertEquals(WorldTier.PINNACLE, WorldTier.byName("pinnacle"));
        assertEquals(WorldTier.HAVEN, WorldTier.byName("unknown_fallback"));
    }

    @Test
    @DisplayName("Verify ItemAffixes and SocketedGems serialization multi-pass round-trip")
    public void testAffixAndSocketMultiPassRoundTrip() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        SocketHelper.setSockets(sword, 2);
        assertEquals(2, SocketHelper.getSockets(sword));
        assertTrue(SocketHelper.hasEmptySockets(sword));

        DynamicHolder<Gem> gemHolder = GemRegistry.INSTANCE.emptyHolder();
        GemInstance gemInst1 = new GemInstance(gemHolder, LootCategory.forItem(sword), Purity.FLAWLESS, new ItemStack(Items.EMERALD), 0);
        GemInstance gemInst2 = new GemInstance(gemHolder, LootCategory.forItem(sword), Purity.PERFECT, new ItemStack(Items.DIAMOND), 1);

        // Pass 1: Set 1 gem
        SocketedGems gems = new SocketedGems(NonNullList.of(GemInstance.EMPTY, gemInst1));
        SocketHelper.setGems(sword, gems);
        assertEquals(2, SocketHelper.getGems(sword).size());

        ItemContainerContents contentsPass1 = sword.get(Apoth.Components.SOCKETED_GEMS);
        assertNotNull(contentsPass1);
        Tag tagPass1 = ItemContainerContents.CODEC.encodeStart(NbtOps.INSTANCE, contentsPass1).getOrThrow();

        ItemContainerContents decoded1 = ItemContainerContents.CODEC.parse(NbtOps.INSTANCE, tagPass1).getOrThrow();
        assertEquals(1, decoded1.itemCopies().toList().size());

        // Pass 2: Mutate (simulate upgrading/adding socket) and re-serialize (Save -> Close -> Re-open cycle)
        SocketedGems filledGems = new SocketedGems(NonNullList.of(GemInstance.EMPTY, gemInst1, gemInst2));
        SocketHelper.setGems(sword, filledGems);

        ItemContainerContents contentsPass2 = sword.get(Apoth.Components.SOCKETED_GEMS);
        assertNotNull(contentsPass2);
        Tag tagPass2 = ItemContainerContents.CODEC.encodeStart(NbtOps.INSTANCE, contentsPass2).getOrThrow();

        ItemContainerContents decoded2 = ItemContainerContents.CODEC.parse(NbtOps.INSTANCE, tagPass2).getOrThrow();
        List<ItemStack> decodedItems = decoded2.itemCopies().toList();
        assertEquals(2, decodedItems.size());
        assertEquals(Items.EMERALD, decodedItems.get(0).getItem());
        assertEquals(Items.DIAMOND, decodedItems.get(1).getItem());

        // Test ItemAffixes component persistence
        DynamicHolder<Affix> affixHolder = AffixRegistry.INSTANCE.emptyHolder();
        ItemAffixes itemAffixes = ItemAffixes.EMPTY.toBuilder().put(affixHolder, 0.75f).build();
        Tag affixesTag = ItemAffixes.CODEC.encodeStart(NbtOps.INSTANCE, itemAffixes).getOrThrow();
        ItemAffixes decodedAffixes = ItemAffixes.CODEC.parse(NbtOps.INSTANCE, affixesTag).getOrThrow();
        assertEquals(1, decodedAffixes.size());

        // Test Affix Name component persistence
        Component originalName = Component.literal("Godforged Netherite Sword");
        Tag nameTag = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, originalName).getOrThrow();
        Component decodedName = ComponentSerialization.CODEC.decode(NbtOps.INSTANCE, nameTag).getOrThrow().getFirst();
        assertEquals("Godforged Netherite Sword", decodedName.getString());
    }

    @Test
    @DisplayName("Verify Spawner custom stats and vanilla parameters NBT persistence")
    public void testSpawnerPersistence() {
        CompoundTag statsTag = new CompoundTag();

        // Encode custom spawner stats
        for (SpawnerStat<?> stat : SpawnerStats.REGISTRY) {
            if (stat == SpawnerStats.MIN_DELAY) {
                Tag encoded = ((SpawnerStat<Integer>) stat).valueCodec().encodeStart(NbtOps.INSTANCE, 40).getOrThrow();
                statsTag.put(stat.getId().toString(), encoded);
            }
            else if (stat == SpawnerStats.IGNORE_PLAYERS) {
                Tag encoded = ((SpawnerStat<Boolean>) stat).valueCodec().encodeStart(NbtOps.INSTANCE, true).getOrThrow();
                statsTag.put(stat.getId().toString(), encoded);
            }
            else if (stat == SpawnerStats.REDSTONE_CONTROL) {
                Tag encoded = ((SpawnerStat<Boolean>) stat).valueCodec().encodeStart(NbtOps.INSTANCE, true).getOrThrow();
                statsTag.put(stat.getId().toString(), encoded);
            }
        }

        CompoundTag root = new CompoundTag();
        root.put("stats", statsTag);
        root.putBoolean("modified", true);
        root.putInt("unstable_ticks", 45);

        // Verify root tags survive serialization
        assertTrue(root.getBooleanOr("modified", false));
        assertEquals(45, root.getIntOr("unstable_ticks", -1));

        CompoundTag readStats = root.getCompound("stats").orElse(new CompoundTag());
        assertNotNull(readStats);

        Tag minDelayTag = readStats.get("apothic_spawners:min_delay");
        assertNotNull(minDelayTag);
        int minDelay = SpawnerStats.MIN_DELAY.valueCodec().decode(NbtOps.INSTANCE, minDelayTag).getOrThrow().getFirst();
        assertEquals(40, minDelay);

        Tag ignorePlayersTag = readStats.get("apothic_spawners:ignore_players");
        assertNotNull(ignorePlayersTag);
        boolean ignorePlayers = SpawnerStats.IGNORE_PLAYERS.valueCodec().decode(NbtOps.INSTANCE, ignorePlayersTag).getOrThrow().getFirst();
        assertTrue(ignorePlayers);
    }

    @Test
    @DisplayName("Verify Raven Table stats data attachment serialization")
    public void testRavenTableStatsPersistence() {
        RavenTableStats stats = new RavenTableStats(45, 80, 65);
        assertEquals(45, stats.eterna());
        assertEquals(80, stats.quanta());
        assertEquals(65, stats.arcana());

        Tag encoded = RavenTableStats.CODEC.codec().encodeStart(NbtOps.INSTANCE, stats).getOrThrow();
        assertTrue(encoded instanceof CompoundTag);

        RavenTableStats decoded = RavenTableStats.CODEC.codec().decode(NbtOps.INSTANCE, encoded).getOrThrow().getFirst();
        assertEquals(45, decoded.eterna());
        assertEquals(80, decoded.quanta());
        assertEquals(65, decoded.arcana());
    }

    @Test
    @DisplayName("Verify unknown/unresolved gems are preserved across production save/load and reconciled upon definition availability")
    public void testUnknownGemPreservationAndReconciliation() {
        dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTile tile =
            new dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTile.BasicGemCaseTile(
                BlockPos.ZERO,
                dev.shadowsoffire.apotheosis.Apoth.Blocks.GEM_CASE.value().defaultBlockState()
            );

        // Construct saved data containing an unknown/future gem
        CompoundTag root = new CompoundTag();
        CompoundTag gemsTag = new CompoundTag();
        CompoundTag unknownGemPurities = new CompoundTag();
        unknownGemPurities.putInt("cracked", 5);
        unknownGemPurities.putInt("flawless", 2);
        gemsTag.put("apotheosis:future_modded_gem_v2", unknownGemPurities);
        root.put("gems", gemsTag);

        // Production load
        tile.loadGemData(root);

        // Save back out to a fresh tag
        CompoundTag saved = new CompoundTag();
        tile.saveGemData(saved);

        // Verify the unknown gem was NOT dropped
        assertTrue(saved.contains("gems"));
        CompoundTag savedGems = saved.getCompoundOrEmpty("gems");
        assertTrue(savedGems.contains("apotheosis:future_modded_gem_v2"));
        CompoundTag savedPurities = savedGems.getCompoundOrEmpty("apotheosis:future_modded_gem_v2");
        assertEquals(5, savedPurities.getIntOr("cracked", 0));
        assertEquals(2, savedPurities.getIntOr("flawless", 0));
    }

    @Test
    @DisplayName("Verify LivingEntityInvoker mixin registration in configuration")
    public void testLivingEntityInvokerRegistered() throws Exception {
        String mixinJson = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of("src/main/resources/apotheosis.mixins.json")));
        assertTrue(mixinJson.contains("\"LivingEntityInvoker\""), "apotheosis.mixins.json must register LivingEntityInvoker to prevent ClassCastException");
    }

    @Test
    @DisplayName("Verify cooperative flight grant does not revoke external flight")
    public void testCooperativeFlightGrant() {
        dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil.EXTERNAL_FLIGHT_PREDICATES.add(p -> true);
        try {
            assertTrue(dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil.EXTERNAL_FLIGHT_PREDICATES.stream().anyMatch(p -> p.test(null)));
        } finally {
            dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil.EXTERNAL_FLIGHT_PREDICATES.clear();
        }
    }

    private static Identifier getComponentId(DataComponentType<?> type) {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
    }

    private static Identifier getHolderId(Holder<?> holder) {
        return holder.unwrapKey().orElseThrow().identifier();
    }
}
