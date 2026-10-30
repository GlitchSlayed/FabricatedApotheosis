package dev.shadowsoffire.apotheosis.compat.spawners;

import dev.shadowsoffire.apotheosis.util.PresetSpawnerStats;
import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class ApothicSpawnersCompat {

    public static final String MODID = ApothicSpawners.MODID;

    public static boolean isLoaded() {
        return true;
    }

    public static void applyStats(PresetSpawnerStats stats, SpawnerBlockEntity entity) {
        // Wired in Milestone 9 boss spawners
    }
}
