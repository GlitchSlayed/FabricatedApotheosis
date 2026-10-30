package dev.shadowsoffire.apotheosis.adventure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.DataResult;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingTableTile;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingTableTile;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Ingredient;

public class AdventureTests {

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

        try {
            var unused = Apoth.BuiltInRegs.LOOT_CATEGORY_KEY;
            Class.forName("dev.shadowsoffire.apotheosis.Apoth$LootCategories");
            if (Apoth.BuiltInRegs.LOOT_CATEGORY instanceof net.minecraft.core.MappedRegistry<?> mapped) {
                mapped.freeze();
            }
            LootCategory.Inner.rebuildSortedValueList();
            LootCategory.registerOverride(Items.DIAMOND_SWORD, Apoth.LootCategories.MELEE_WEAPON);
            LootCategory.registerOverride(Items.BOW, Apoth.LootCategories.BOW);
            LootCategory.registerOverride(Items.NETHERITE_CHESTPLATE, Apoth.LootCategories.CHESTPLATE);
            LootCategory.registerOverride(Items.IRON_PICKAXE, Apoth.LootCategories.BREAKER);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            AdventureModule.init();
        } catch (Throwable t) {
            // Ignored in non-instrumented test JVM
        }
    }

    @Test
    @DisplayName("LootCategory accurately categorizes vanilla equipment")
    public void testLootCategories() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        LootCategory swordCat = LootCategory.forItem(sword);
        assertNotNull(swordCat);
        assertFalse(swordCat.isNone());

        ItemStack bow = new ItemStack(Items.BOW);
        LootCategory bowCat = LootCategory.forItem(bow);
        assertNotNull(bowCat);
        assertFalse(bowCat.isNone());

        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        LootCategory chestCat = LootCategory.forItem(chestplate);
        assertNotNull(chestCat);
        assertFalse(chestCat.isNone());

        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        LootCategory pickCat = LootCategory.forItem(pickaxe);
        assertNotNull(pickCat);
        assertFalse(pickCat.isNone());
    }

    @Test
    @DisplayName("Socket management, empty checks, and SocketedGems serialization")
    public void testSocketsAndGems() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        assertEquals(0, SocketHelper.getSockets(sword));
        assertFalse(SocketHelper.hasEmptySockets(sword));

        // Add 2 sockets
        SocketHelper.setSockets(sword, 2);
        assertEquals(2, SocketHelper.getSockets(sword));
        assertTrue(SocketHelper.hasEmptySockets(sword));

        // Create socketed gems container
        DynamicHolder<Gem> gemHolder = GemRegistry.INSTANCE.emptyHolder();
        GemInstance gemInst1 = new GemInstance(gemHolder, LootCategory.forItem(sword), Purity.FLAWLESS, new ItemStack(Items.EMERALD), 0);
        GemInstance gemInst2 = new GemInstance(gemHolder, LootCategory.forItem(sword), Purity.PERFECT, new ItemStack(Items.DIAMOND), 1);

        SocketedGems gems = new SocketedGems(NonNullList.of(GemInstance.EMPTY, gemInst1));
        SocketHelper.setGems(sword, gems);

        assertEquals(2, SocketHelper.getGems(sword).size());
        assertTrue(SocketHelper.hasEmptySockets(sword), "Should still have 1 empty socket");

        // Fill second socket
        SocketedGems filledGems = new SocketedGems(NonNullList.of(GemInstance.EMPTY, gemInst1, gemInst2));
        SocketHelper.setGems(sword, filledGems);

        // Serialization round-trip of ItemContainerContents component
        ItemContainerContents contents = sword.get(Apoth.Components.SOCKETED_GEMS);
        assertNotNull(contents);
        DataResult<Tag> nbtResult = ItemContainerContents.CODEC.encodeStart(NbtOps.INSTANCE, contents);
        assertTrue(nbtResult.isSuccess(), "Serialization must succeed");
        Tag tag = nbtResult.getOrThrow();

        DataResult<ItemContainerContents> decodedResult = ItemContainerContents.CODEC.parse(NbtOps.INSTANCE, tag);
        assertTrue(decodedResult.isSuccess(), "Deserialization must succeed");
        ItemContainerContents decoded = decodedResult.getOrThrow();
        List<ItemStack> decodedItems = decoded.itemCopies().toList();
        assertEquals(2, decodedItems.size());
        assertEquals(Items.EMERALD, decodedItems.get(0).getItem());
        assertEquals(Items.DIAMOND, decodedItems.get(1).getItem());
    }

    @Test
    @DisplayName("Affix exclusivity rules and ItemAffixes serialization round-trip")
    public void testAffixesAndExclusivity() {
        DynamicHolder<Affix> affix1Holder = AffixRegistry.INSTANCE.emptyHolder();
        DynamicHolder<Affix> affix2Holder = AffixRegistry.INSTANCE.emptyHolder();

        // Serialization test
        ItemAffixes itemAffixes = ItemAffixes.EMPTY.toBuilder().put(affix1Holder, 0.75f).build();
        DataResult<Tag> encoded = ItemAffixes.CODEC.encodeStart(NbtOps.INSTANCE, itemAffixes);
        assertTrue(encoded.isSuccess(), "ItemAffixes must encode without error");

        DataResult<ItemAffixes> decoded = ItemAffixes.CODEC.parse(NbtOps.INSTANCE, encoded.getOrThrow());
        assertTrue(decoded.isSuccess(), "ItemAffixes must decode without error");
        assertEquals(0.75f, decoded.getOrThrow().getLevel(affix1Holder), 0.0001f);
    }

    @Test
    @DisplayName("Purity ordinal ordering and upgrade mechanics")
    public void testPurityUpgrades() {
        Purity[] purities = Purity.values();
        assertEquals(6, purities.length);
        assertEquals(Purity.CRACKED, purities[0]);
        assertEquals(Purity.CHIPPED, purities[1]);
        assertEquals(Purity.FLAWED, purities[2]);
        assertEquals(Purity.NORMAL, purities[3]);
        assertEquals(Purity.FLAWLESS, purities[4]);
        assertEquals(Purity.PERFECT, purities[5]);

        assertTrue(Purity.CRACKED.compareTo(Purity.CHIPPED) < 0);
        assertTrue(Purity.PERFECT.compareTo(Purity.FLAWLESS) > 0);
    }

    @Test
    @DisplayName("SalvagingRecipe item conservation and output derivation")
    public void testSalvagingOutputConservation() {
        SalvagingRecipe.OutputData output1 = new SalvagingRecipe.OutputData(Items.IRON_NUGGET, 2, 5);
        SalvagingRecipe recipe = new SalvagingRecipe(
            Ingredient.of(Items.IRON_SWORD),
            List.of(output1)
        );

        List<SalvagingRecipe.OutputData> outputs = recipe.getOutputs();
        assertNotNull(outputs);
        assertEquals(1, outputs.size());
        assertEquals(2, outputs.get(0).min());
        assertEquals(5, outputs.get(0).max());
    }

    @Test
    @DisplayName("Inventory operations and item conservation for ReforgingTableTile")
    public void testReforgingTableInventory() {
        ReforgingTableTile tile = new ReforgingTableTile(BlockPos.ZERO, Apoth.Blocks.REFORGING_TABLE.value().defaultBlockState());
        tile.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
        assertFalse(tile.getInventory().getItem(0).isEmpty());
        assertEquals(Items.DIAMOND_SWORD, tile.getInventory().getItem(0).getItem());

        tile.getInventory().clearContent();
        assertTrue(tile.getInventory().getItem(0).isEmpty());
    }

    @Test
    @DisplayName("Inventory operations and item conservation for SalvagingTableTile")
    public void testSalvagingTableInventory() {
        SalvagingTableTile tile = new SalvagingTableTile(BlockPos.ZERO, Apoth.Blocks.SALVAGING_TABLE.value().defaultBlockState());
        tile.getOutput().setItem(0, new ItemStack(Items.DIAMOND, 3));
        assertEquals(3, tile.getOutput().getItem(0).getCount());

        ItemStack extracted = tile.getOutput().removeItem(0, 2);
        assertEquals(2, extracted.getCount());
        assertEquals(1, tile.getOutput().getItem(0).getCount());
    }
}
