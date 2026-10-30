package net.neoforged.neoforge.event.level;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDropsEvent {

    private final ServerLevel level;
    private final BlockPos pos;
    private final BlockState state;
    private final Entity breaker;
    private final List<ItemEntity> drops;

    public BlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state, Entity breaker, List<ItemEntity> drops) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.breaker = breaker;
        this.drops = drops;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public BlockState getState() {
        return this.state;
    }

    public Entity getBreaker() {
        return this.breaker;
    }

    public List<ItemEntity> getDrops() {
        return this.drops;
    }
}
