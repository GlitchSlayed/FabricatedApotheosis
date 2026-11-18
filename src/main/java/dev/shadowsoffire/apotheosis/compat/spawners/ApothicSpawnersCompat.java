package dev.shadowsoffire.apotheosis.compat.spawners;

import java.util.Map;

import com.mojang.serialization.Dynamic;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.PresetSpawnerStats;
import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class ApothicSpawnersCompat {

    public static final String MODID = ApothicSpawners.MODID;

    /**
     * Key for the Capturing enchantment.
     */
    public static final ResourceKey<Enchantment> CAPTURING = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MODID, "capturing"));

    public static boolean isLoaded() {
        return true;
    }

    /**
     * Applies preset spawner stats through Apothic Spawners' stat registry.
     */
    public static void applyStats(PresetSpawnerStats stats, SpawnerBlockEntity entity) {
        if (!(entity instanceof ApothSpawnerTile spawner)) {
            Apotheosis.LOGGER.error("Expected an ApothSpawnerTile at {} but found {} - preset spawner stats will not be applied.",
                entity.getBlockPos(), entity.getClass().getName());
            return;
        }
        for (Map.Entry<Identifier, Dynamic<?>> entry : stats.stats().entrySet()) {
            SpawnerStat<?> stat = SpawnerStats.REGISTRY.getValue(entry.getKey());
            if (stat == null) {
                Apotheosis.LOGGER.error("Unknown spawner stat {} found in preset spawner stats - it will be skipped.", entry.getKey());
                continue;
            }
            applyStat(stat, spawner, entry.getValue());
        }
    }

    private static <T> void applyStat(SpawnerStat<T> stat, ApothSpawnerTile tile, Dynamic<?> value) {
        stat.valueCodec().parse(value)
            .resultOrPartial(msg -> Apotheosis.LOGGER.error("Failed to parse value for spawner stat {}: {}", stat.getId(), msg))
            .ifPresent(v -> stat.setValue(tile, v));
    }
}
