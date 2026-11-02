package dev.shadowsoffire.apotheosis.client.compat.jade;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SpawnerClientProvider implements IBlockComponentProvider {

    public static final Identifier UID = ApothicSpawners.loc("spawner");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(UID)) return;

        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof ApothSpawnerTile tile) {
            CompoundTag serverData = accessor.getServerData();
            if (serverData != null) {
                serverData.getCompound("ApothSpawner").ifPresent(spawnerTag -> {
                    try {
                        tile.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, accessor.getLevel().registryAccess(), spawnerTag));
                    }
                    catch (Exception ignored) {}
                });
            }

            for (SpawnerStat<?> stat : SpawnerStats.REGISTRY) {
                Component comp = stat.getTooltip(tile);
                if (comp != null && !comp.getString().isEmpty()) {
                    tooltip.add(comp);
                }
            }
        }
    }
}
