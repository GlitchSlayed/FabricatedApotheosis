package dev.shadowsoffire.apothic_enchanting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apothic_enchanting.event.AnvilUpdateEvent;
import dev.shadowsoffire.apothic_enchanting.library.EnchLibraryTile;
import dev.shadowsoffire.apothic_enchanting.library.EnchLibraryTile.BasicLibraryTile;
import dev.shadowsoffire.apothic_enchanting.mixin.AnvilMenuMixin;
import dev.shadowsoffire.apothic_enchanting.table.ApothEnchantmentHelper;
import dev.shadowsoffire.apothic_enchanting.table.Arcana;
import dev.shadowsoffire.apothic_enchanting.table.EnchantmentTableStats;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;

public class EnchantingTests {

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

        Ench.bootstrap();

        for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            try {
                item.builtInRegistryHolder().components();
            } catch (Exception e) {
                item.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.EMPTY);
            }
        }

        ApotheosisConfig.load();
    }

    @Test
    @DisplayName("Enchantment Library: point conversion matches 2^(N-1) formula")
    void testLevelToPoints() {
        assertEquals(1, EnchLibraryTile.levelToPoints(1));
        assertEquals(2, EnchLibraryTile.levelToPoints(2));
        assertEquals(4, EnchLibraryTile.levelToPoints(3));
        assertEquals(8, EnchLibraryTile.levelToPoints(4));
        assertEquals(16, EnchLibraryTile.levelToPoints(5));
        assertEquals(32, EnchLibraryTile.levelToPoints(6));
        assertEquals(32768, EnchLibraryTile.levelToPoints(16)); // Basic library cap (2^15)
        assertEquals(1073741824, EnchLibraryTile.levelToPoints(31)); // Ender library cap (2^30)

        // Verify incremental cost to upgrade an existing item
        // Going from Sharpness III (4 pts) to Sharpness V (16 pts) costs 12 points
        int cost3to5 = EnchLibraryTile.levelToPoints(5) - EnchLibraryTile.levelToPoints(3);
        assertEquals(12, cost3to5);

        // Going from Sharpness I (1 pt) to Sharpness IV (8 pts) costs 7 points
        int cost1to4 = EnchLibraryTile.levelToPoints(4) - EnchLibraryTile.levelToPoints(1);
        assertEquals(7, cost1to4);
    }

    @Test
    @DisplayName("Enchanting Table Stats: Step-ladder Eterna accumulation and clamping")
    void testEternaStepLadderCalculation() {
        // Test standard vanilla bookshelves: 15 shelves contributing 1.0 each, max 15.0
        EnchantmentTableStats.Builder builder1 = new EnchantmentTableStats.Builder();
        for (int i = 0; i < 15; i++) {
            builder1.addEterna(1.0F, 15.0F);
        }
        EnchantmentTableStats stats1 = builder1.build();
        assertEquals(15.0F, stats1.eterna(), 0.001F);
        assertEquals(15.0F, stats1.quanta(), 0.001F); // Default 15% quanta
        assertEquals(1, stats1.clues()); // Default 1 clue

        // Test overcapping: 20 vanilla bookshelves contributing 1.0 each, max 15.0 -> clamps at 15.0
        EnchantmentTableStats.Builder builderOvercap = new EnchantmentTableStats.Builder();
        for (int i = 0; i < 20; i++) {
            builderOvercap.addEterna(1.0F, 15.0F);
        }
        EnchantmentTableStats statsOvercap = builderOvercap.build();
        assertEquals(15.0F, statsOvercap.eterna(), 0.001F);

        // Test step-ladder progression: 15 vanilla shelves (max 15) + 10 hellshelves (contributing 1.5 each, max 30)
        // 15 from vanilla + (10 * 1.5 = 15) from hellshelves = 30 Eterna total
        EnchantmentTableStats.Builder builderMixed = new EnchantmentTableStats.Builder();
        for (int i = 0; i < 15; i++) {
            builderMixed.addEterna(1.0F, 15.0F);
        }
        for (int i = 0; i < 10; i++) {
            builderMixed.addEterna(1.5F, 30.0F);
        }
        EnchantmentTableStats statsMixed = builderMixed.build();
        assertEquals(30.0F, statsMixed.eterna(), 0.001F);
    }

    @Test
    @DisplayName("Production Enchanting Calculations: getQuantaFactor invokes production algorithm")
    void testProductionQuantaFactor() {
        RandomSource zeroRand = RandomSource.create(42L);

        // 0% Quanta: factor must be exactly 1.0F regardless of stability
        float qFactor0Unstable = ApothEnchantmentHelper.getQuantaFactor(zeroRand, 0.0F, false);
        assertEquals(1.0F, qFactor0Unstable, 0.001F);

        float qFactor0Stable = ApothEnchantmentHelper.getQuantaFactor(zeroRand, 0.0F, true);
        assertEquals(1.0F, qFactor0Stable, 0.001F);

        // Stable enchanting table (100% rectification): factor is always >= 1.0 (never negative deviation)
        for (int i = 0; i < 50; i++) {
            float factor = ApothEnchantmentHelper.getQuantaFactor(zeroRand, 50.0F, true);
            assertTrue(factor >= 1.0F, "Stable quanta factor must never reduce enchanting power");
            assertTrue(factor <= 1.50F, "50% quanta stable factor must not exceed 1.50");
        }

        // Unstable table: gaussian can deviate both positive and negative
        boolean sawAbove = false;
        boolean sawBelow = false;
        for (int i = 0; i < 200; i++) {
            float factor = ApothEnchantmentHelper.getQuantaFactor(zeroRand, 50.0F, false);
            if (factor > 1.0F) sawAbove = true;
            if (factor < 1.0F) sawBelow = true;
            assertTrue(factor >= 0.50F && factor <= 1.50F, "50% quanta factor must stay within [0.50, 1.50]");
        }
        assertTrue(sawAbove && sawBelow, "Unstable quanta must produce bidirectional power variance");
    }

    @Test
    @DisplayName("Production Enchanting Calculations: slot cost progression scaling")
    void testProductionEnchantmentCostScaling() {
        RandomSource rand = RandomSource.create(12345L);
        ItemStack dummyTool = new ItemStack(Items.DIAMOND_SWORD);

        for (int eterna = 1; eterna <= 100; eterna += 10) {
            // Slot 2 (top slot) always equals eterna level
            int slot2Cost = ApothEnchantmentHelper.getEnchantmentCost(rand, 2, (float) eterna, dummyTool);
            assertEquals(eterna, slot2Cost);

            // Slot 1 is between 60% and 80% of Slot 2
            int slot1Cost = ApothEnchantmentHelper.getEnchantmentCost(rand, 1, (float) eterna, dummyTool);
            assertTrue(slot1Cost >= 1 && slot1Cost <= slot2Cost);

            // Slot 0 is between 20% and 40% of Slot 2
            int slot0Cost = ApothEnchantmentHelper.getEnchantmentCost(rand, 0, (float) eterna, dummyTool);
            assertTrue(slot0Cost >= 1 && slot0Cost <= slot1Cost);
        }
    }

    @Test
    @DisplayName("Enchantment Library: Transactional storage insert, commit, and abort")
    void testLibraryTransactionalStorage() {
        BasicLibraryTile library = new BasicLibraryTile(BlockPos.ZERO, Ench.Blocks.LIBRARY.value().defaultBlockState());
        Storage<ItemVariant> storage = library.getItemStorage(null);

        // Insert non-enchanted item -> rejected
        ItemVariant nonEnchantedVariant = createItemVariant(new ItemStack(Items.BOOK));
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(nonEnchantedVariant, 1, tx);
            assertEquals(0, inserted);
            tx.commit();
        }

        // Prepare an enchanted book with Sharpness III (4 points)
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable enchs = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        Holder<Enchantment> dummyEnch = Holder.direct(
            Enchantment.enchantment(
                Enchantment.definition(
                    net.minecraft.core.HolderSet.empty(),
                    1, 5,
                    Enchantment.constantCost(1),
                    Enchantment.constantCost(1),
                    1
                )
            ).build(Identifier.fromNamespaceAndPath("test", "dummy"))
        );
        enchs.set(dummyEnch, 3);
        EnchantmentHelper.setEnchantments(book, enchs.toImmutable());
        ItemVariant bookVariant = createItemVariant(book);

        // Test Abort: transaction inserts book, but aborts -> library points remain 0
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(bookVariant, 1, tx);
            assertEquals(1, inserted);
            assertEquals(4, library.getPointsMap().getInt(dummyEnch)); // Visible during tx
            tx.abort();
        }
        assertEquals(0, library.getPointsMap().getInt(dummyEnch), "Aborted transaction must roll back library points to 0");

        // Test Commit: transaction inserts book and commits -> points persist
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(bookVariant, 1, tx);
            assertEquals(1, inserted);
            tx.commit();
        }
        assertEquals(4, library.getPointsMap().getInt(dummyEnch), "Committed transaction must persist points");
        assertEquals(3, library.getLevelsMap().getInt(dummyEnch));

        // Test Nested Transaction Abort
        try (Transaction outer = Transaction.openOuter()) {
            try (Transaction nested = outer.openNested()) {
                storage.insert(bookVariant, 1, nested);
                assertEquals(8, library.getPointsMap().getInt(dummyEnch));
                nested.abort();
            }
            assertEquals(4, library.getPointsMap().getInt(dummyEnch), "Nested abort must roll back to outer transaction state");
            outer.commit();
        }
        assertEquals(4, library.getPointsMap().getInt(dummyEnch));
    }

    private static ItemVariant createItemVariant(ItemStack stack) {
        return new ItemVariant() {
            @Override
            public boolean isBlank() {
                return stack.isEmpty();
            }

            @Override
            public net.minecraft.world.item.Item getObject() {
                return stack.getItem();
            }

            @Override
            public net.minecraft.core.component.DataComponentPatch getComponentsPatch() {
                return stack.getComponentsPatch();
            }

            @Override
            public net.minecraft.core.component.DataComponentMap getComponents() {
                return stack.getComponents();
            }

            @Override
            public boolean isOf(net.minecraft.world.item.Item item) {
                return stack.is(item);
            }

            @Override
            public ItemStack toStack(int count) {
                return stack.copyWithCount(count);
            }

            @Override
            public ItemVariant withComponents(net.minecraft.core.component.DataComponentPatch patch) {
                return this;
            }
        };
    }

    @Test
    @DisplayName("Anvil Overhaul: Level 40 cap removal injection validation")
    void testAnvilLevel40CapRemoval() {
        AnvilMenuMixin mixin = new AnvilMenuMixin();

        // When enableEnchanting is true (default), cap is removed (Integer.MAX_VALUE)
        ApotheosisConfig.getServer().enableEnchanting = true;
        assertEquals(Integer.MAX_VALUE, mixin.apoth_removeLevelCap(40));

        // When enableEnchanting is disabled, vanilla behavior is strictly preserved (returns old cap 40)
        ApotheosisConfig.getServer().enableEnchanting = false;
        assertEquals(40, mixin.apoth_removeLevelCap(40));

        // Reset
        ApotheosisConfig.getServer().enableEnchanting = true;
    }

    @Test
    @DisplayName("Anvil Overhaul: Prismatic Web curse removal and Anvil repair recipes")
    void testAnvilEvents() {
        // Test damaged anvil repair with iron block
        ItemStack damagedAnvil = new ItemStack(Items.DAMAGED_ANVIL);
        ItemStack ironBlock = new ItemStack(Blocks.IRON_BLOCK);
        AnvilUpdateEvent repairEvent = new AnvilUpdateEvent(damagedAnvil, ironBlock, "Anvil", 0, null);
        ApothEnchEvents.onAnvilUpdate(repairEvent);

        assertEquals(Items.ANVIL, repairEvent.getOutput().getItem(), "Damaged anvil + iron block should repair to regular anvil");
        assertEquals(5, repairEvent.getXpCost(), "Anvil iron block repair costs 5 XP levels");
        assertEquals(1, repairEvent.getMaterialCost());

        // Test Prismatic Web on enchanted item
        ItemStack swordWithCurse = new ItemStack(Items.DIAMOND_SWORD);
        Holder<Enchantment> testEnch = Holder.direct(
            Enchantment.enchantment(
                Enchantment.definition(
                    net.minecraft.core.HolderSet.empty(),
                    1, 5,
                    Enchantment.constantCost(1),
                    Enchantment.constantCost(1),
                    1
                )
            ).build(Identifier.fromNamespaceAndPath("test", "test_ench"))
        );
        ItemEnchantments.Mutable enchs = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchs.set(testEnch, 2);
        EnchantmentHelper.setEnchantments(swordWithCurse, enchs.toImmutable());

        ItemStack web = new ItemStack(Ench.Items.PRISMATIC_WEB.value());
        AnvilUpdateEvent webEvent = new AnvilUpdateEvent(swordWithCurse, web, "Clean Sword", 0, null);
        ApothEnchEvents.onAnvilUpdate(webEvent);

        assertFalse(webEvent.getOutput().isEmpty(), "Prismatic web on enchanted sword must output cleaned item");
        assertEquals(30, webEvent.getXpCost(), "Prismatic web curse removal costs 30 levels");
    }

    @Test
    @DisplayName("StepFunction: Power curve evaluation")
    void testStepFunctionEvaluation() {
        StepFunction func = StepFunction.fromBounds(10.0F, 50.0F, 10.0F);
        assertEquals(10.0F, func.min());
        assertEquals(50.0F, func.max());
        assertEquals(10.0F, func.step());
        assertEquals(4, func.steps());

        assertEquals(10.0F, func.get(0.0F), 0.001F);
        assertEquals(50.0F, func.get(1.0F), 0.001F);
    }
}
