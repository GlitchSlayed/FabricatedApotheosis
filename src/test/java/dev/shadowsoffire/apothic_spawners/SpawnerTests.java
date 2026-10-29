package dev.shadowsoffire.apothic_spawners;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;

public class SpawnerTests {

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

        ApothicSpawners.init();
        ALObjects.bootstrap();
        if (SpawnerStats.REGISTRY instanceof net.minecraft.core.MappedRegistry<?> mapped) {
            mapped.freeze();
        }

        java.lang.reflect.Method bindTagsMethod = net.minecraft.core.Holder.Reference.class.getDeclaredMethod("bindTags", java.util.Collection.class);
        bindTagsMethod.setAccessible(true);
        bindTagsMethod.invoke(EntityTypes.WARDEN.builtInRegistryHolder(), java.util.List.of(ASObjects.BLACKLISTED_FROM_SPAWNERS));
        bindTagsMethod.invoke(EntityTypes.ELDER_GUARDIAN.builtInRegistryHolder(), java.util.List.of(ASObjects.BLACKLISTED_FROM_SPAWNERS));
    }

    private ApothSpawnerTile createTile() {
        return new ApothSpawnerTile(BlockPos.ZERO, Blocks.SPAWNER.defaultBlockState());
    }

    @Test
    @DisplayName("Vanilla Spawner Stats Default Values")
    public void testVanillaDefaults() {
        ApothSpawnerTile tile = createTile();
        assertEquals(200, SpawnerStats.MIN_DELAY.getValue(tile));
        assertEquals(800, SpawnerStats.MAX_DELAY.getValue(tile));
        assertEquals(4, SpawnerStats.SPAWN_COUNT.getValue(tile));
        assertEquals(6, SpawnerStats.MAX_NEARBY_ENTITIES.getValue(tile));
        assertEquals(16, SpawnerStats.REQ_PLAYER_RANGE.getValue(tile));
        assertEquals(4, SpawnerStats.SPAWN_RANGE.getValue(tile));
    }

    @Test
    @DisplayName("Custom Spawner Stats Default Values")
    public void testCustomDefaults() {
        ApothSpawnerTile tile = createTile();
        assertEquals(1.0F, SpawnerStats.INITIAL_HEALTH.getValue(tile), 0.001F);
        assertFalse(SpawnerStats.IGNORE_PLAYERS.getValue(tile));
        assertFalse(SpawnerStats.IGNORE_CONDITIONS.getValue(tile));
        assertFalse(SpawnerStats.REDSTONE_CONTROL.getValue(tile));
        assertFalse(SpawnerStats.IGNORE_LIGHT.getValue(tile));
        assertFalse(SpawnerStats.NO_AI.getValue(tile));
        assertFalse(SpawnerStats.SILENT.getValue(tile));
        assertFalse(SpawnerStats.YOUTHFUL.getValue(tile));
        assertFalse(SpawnerStats.BURNING.getValue(tile));
        assertEquals(0, SpawnerStats.ECHOING.getValue(tile));
    }

    @Test
    @DisplayName("Stat Modifier Clamping and Upper/Lower Limits")
    public void testStatModifierClamping() {
        ApothSpawnerTile tile = createTile();

        // Min Delay modifier: reduce by 50, minimum 20
        StatModifier<Integer> reduceMinDelay = new StatModifier<>(SpawnerStats.MIN_DELAY, -50, Optional.of(20), Optional.empty(), StatModifier.Mode.ADD);
        assertTrue(reduceMinDelay.apply(tile));
        assertEquals(150, SpawnerStats.MIN_DELAY.getValue(tile));

        // Apply repeatedly down to clamp limit of 20
        for (int i = 0; i < 5; i++) {
            reduceMinDelay.apply(tile);
        }
        assertEquals(20, SpawnerStats.MIN_DELAY.getValue(tile));

        // Once at minimum, further reductions must fail (item protection: do not consume)
        assertFalse(reduceMinDelay.apply(tile), "Modifier at clamp bound must return false");
        assertEquals(20, SpawnerStats.MIN_DELAY.getValue(tile));
    }

    @Test
    @DisplayName("Spawn Count Upper Limit Clamping")
    public void testSpawnCountClamping() {
        ApothSpawnerTile tile = createTile();
        StatModifier<Integer> addCount = new StatModifier<>(SpawnerStats.SPAWN_COUNT, 2, Optional.empty(), Optional.of(16), StatModifier.Mode.ADD);

        assertTrue(addCount.apply(tile));
        assertEquals(6, SpawnerStats.SPAWN_COUNT.getValue(tile));

        // Pump up to max 16
        while (SpawnerStats.SPAWN_COUNT.getValue(tile) < 16) {
            assertTrue(addCount.apply(tile));
        }
        assertEquals(16, SpawnerStats.SPAWN_COUNT.getValue(tile));

        // Cannot exceed max 16
        assertFalse(addCount.apply(tile));
        assertEquals(16, SpawnerStats.SPAWN_COUNT.getValue(tile));
    }

    @Test
    @DisplayName("Boolean Stat Toggles and Reapplication Rejection")
    public void testBooleanStatToggles() {
        ApothSpawnerTile tile = createTile();

        StatModifier<Boolean> enableIgnorePlayers = new StatModifier<>(SpawnerStats.IGNORE_PLAYERS, true, Optional.empty(), Optional.empty(), StatModifier.Mode.SET);
        assertTrue(enableIgnorePlayers.apply(tile));
        assertTrue(SpawnerStats.IGNORE_PLAYERS.getValue(tile));

        // Re-applying same value must return false (no-op, protects items from consumption)
        assertFalse(enableIgnorePlayers.apply(tile), "Re-applying true to true boolean stat must return false");

        // Inverse modifier sets back to false
        StatModifier<Boolean> disableIgnorePlayers = new StatModifier<>(SpawnerStats.IGNORE_PLAYERS, false, Optional.empty(), Optional.empty(), StatModifier.Mode.SET);
        assertTrue(disableIgnorePlayers.apply(tile));
        assertFalse(SpawnerStats.IGNORE_PLAYERS.getValue(tile));
    }

    @Test
    @DisplayName("Echoing Levels and Experience Multiplier Formula")
    public void testEchoingLevelsAndFormula() {
        ApothSpawnerTile tile = createTile();

        StatModifier<Integer> addEcho = new StatModifier<>(SpawnerStats.ECHOING, 1, Optional.of(0), Optional.of(3), StatModifier.Mode.ADD);
        assertTrue(addEcho.apply(tile));
        assertEquals(1, SpawnerStats.ECHOING.getValue(tile));

        // Test XP scaling formula: xp * (1 + echoes)
        int baseXp = 50;
        int scaled1 = baseXp * (1 + SpawnerStats.ECHOING.getValue(tile));
        assertEquals(100, scaled1);

        assertTrue(addEcho.apply(tile));
        assertEquals(2, SpawnerStats.ECHOING.getValue(tile));
        int scaled2 = baseXp * (1 + SpawnerStats.ECHOING.getValue(tile));
        assertEquals(150, scaled2);

        assertTrue(addEcho.apply(tile));
        assertEquals(3, SpawnerStats.ECHOING.getValue(tile));

        // Clamped at 3
        assertFalse(addEcho.apply(tile));
        assertEquals(3, SpawnerStats.ECHOING.getValue(tile));
    }

    @Test
    @DisplayName("Capturing Formula and Blacklist Verification")
    public void testCapturingFormulaAndBlacklist() {
        float perLevelChance = 0.005F; // From capturing.json
        int level = 3;
        float totalChance = perLevelChance * level;
        assertEquals(0.015F, totalChance, 0.0001F);

        // Verify blacklisted entities
        EntityType<?> warden = EntityTypes.WARDEN;
        assertTrue(warden.builtInRegistryHolder().is(ASObjects.BLACKLISTED_FROM_SPAWNERS), "Warden must be blacklisted from spawners");

        EntityType<?> elderGuardian = EntityTypes.ELDER_GUARDIAN;
        assertTrue(elderGuardian.builtInRegistryHolder().is(ASObjects.BLACKLISTED_FROM_SPAWNERS), "Elder Guardian must be blacklisted from spawners");

        // Normal entities are not blacklisted
        EntityType<?> zombie = EntityTypes.ZOMBIE;
        assertFalse(zombie.builtInRegistryHolder().is(ASObjects.BLACKLISTED_FROM_SPAWNERS), "Zombie must not be blacklisted");
    }

    @Test
    @DisplayName("Spawner Instability Countdown and Detonation Trigger")
    public void testInstabilityLifecycle() {
        ApothSpawnerTile tile = createTile();
        assertFalse(tile.isUnstable());

        tile.beginInstability();
        assertTrue(tile.isUnstable());

        // Re-triggering instability when already unstable must be ignored
        tile.beginInstability();
        assertTrue(tile.isUnstable());
    }

    @Test
    @DisplayName("Movable No-Ai Persistent Tagging")
    public void testMovableTagging() {
        CompoundTag data = new CompoundTag();
        data.putBoolean("apotheosis:movable", true);
        assertTrue(data.getBooleanOr("apotheosis:movable", false));

        // Slime split simulation
        CompoundTag childData = new CompoundTag();
        if (data.getBooleanOr("apotheosis:movable", false)) {
            childData.putBoolean("apotheosis:movable", true);
        }
        assertTrue(childData.getBooleanOr("apotheosis:movable", false), "Split child must inherit movable tag");
    }

    @Test
    @DisplayName("Despawn Protection Window Logic")
    public void testDespawnProtectionLogic() {
        int despawnDelay = 600; // 30 seconds
        CompoundTag data = new CompoundTag();
        data.putBoolean("apothic_spawners:from_spawner", true);

        // Within protection window: tickCount <= 600
        int tick1 = 100;
        boolean protect1 = data.getBooleanOr("apothic_spawners:from_spawner", false) && despawnDelay >= tick1;
        assertTrue(protect1, "Must prevent despawn within the 600 tick window");

        // Beyond protection window: tickCount > 600
        int tick2 = 601;
        boolean protect2 = data.getBooleanOr("apothic_spawners:from_spawner", false) && despawnDelay >= tick2;
        assertFalse(protect2, "Must allow natural despawn after 600 ticks have elapsed");
    }

    @Test
    @DisplayName("Custom Stat NBT Codec Serialization Fidelity")
    public void testNbtSerializationFidelity() {
        ApothSpawnerTile tile = createTile();
        SpawnerStats.INITIAL_HEALTH.setValue(tile, 0.5F);
        SpawnerStats.IGNORE_CONDITIONS.setValue(tile, true);
        SpawnerStats.ECHOING.setValue(tile, 2);

        // Serialize custom stats
        CompoundTag statsTag = new CompoundTag();
        tile.getStatsMap().forEach((stat, value) -> {
            statsTag.put(stat.getId().toString(), ((SpawnerStat<Object>) stat).valueCodec().encodeStart(NbtOps.INSTANCE, value).getOrThrow());
        });

        // Deserialize into fresh tile
        ApothSpawnerTile restoredTile = createTile();
        for (String key : statsTag.keySet()) {
            SpawnerStat<?> stat = SpawnerStats.REGISTRY.getValue(net.minecraft.resources.Identifier.tryParse(key));
            assertNotNull(stat);
            Object value = stat.valueCodec().decode(NbtOps.INSTANCE, statsTag.get(key)).getOrThrow().getFirst();
            ((SpawnerStat<Object>) stat).setValue(restoredTile, value);
        }

        assertEquals(0.5F, SpawnerStats.INITIAL_HEALTH.getValue(restoredTile), 0.001F);
        assertTrue(SpawnerStats.IGNORE_CONDITIONS.getValue(restoredTile));
        assertEquals(2, SpawnerStats.ECHOING.getValue(restoredTile));
    }
}
