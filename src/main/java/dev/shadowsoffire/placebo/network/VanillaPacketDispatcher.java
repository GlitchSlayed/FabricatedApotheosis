package dev.shadowsoffire.placebo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VanillaPacketDispatcher {

    public static void dispatchTEToNearbyPlayers(BlockEntity tile) {
        if (tile.getLevel() instanceof ServerLevel serverLevel) {
            Packet<?> packet = tile.getUpdatePacket();
            if (packet != null) {
                serverLevel.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(tile.getBlockPos()), false).forEach(player -> {
                    player.connection.send(packet);
                });
            }
        }
    }

    public static void dispatchTEToNearbyPlayers(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null) {
            dispatchTEToNearbyPlayers(tile);
        }
    }
}
