package dev.shadowsoffire.apotheosis.compat.jade;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class SpawnerServerDataProvider implements IServerDataProvider<BlockAccessor> {

    public static final Identifier UID = ApothicSpawners.loc("spawner");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ApothSpawnerTile tile) {
            CompoundTag custom = tile.saveCustomOnly(accessor.getLevel().registryAccess());
            tag.put("ApothSpawner", custom);
        }
    }
}
