package dev.shadowsoffire.apotheosis.mobs;

import dev.shadowsoffire.apotheosis.Apoth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BossSpawnerBlock extends Block implements EntityBlock {

    public BossSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BossSpawnerTile(pos, state);
    }

    public static class BossSpawnerTile extends BlockEntity {
        public BossSpawnerTile(BlockPos pos, BlockState state) {
            super(Apoth.Tiles.BOSS_SPAWNER, pos, state);
        }
    }
}
