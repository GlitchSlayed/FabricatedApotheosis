package dev.shadowsoffire.apotheosis.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BossDungeonFeature implements Feature {

    public static final MapCodec<BossDungeonFeature> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.floatRange(0, 1).optionalFieldOf("success_chance", 1F).forGetter(BossDungeonFeature::successChance)
    ).apply(inst, BossDungeonFeature::new));

    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState BRICK = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_BRICK = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED_BRICK = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState[] BRICKS = { BRICK, MOSSY_BRICK, CRACKED_BRICK };

    private final float successChance;

    public BossDungeonFeature(float successChance) {
        this.successChance = successChance;
    }

    public BossDungeonFeature() {
        this(1.0f);
    }

    public float successChance() {
        return this.successChance;
    }

    @Override
    public MapCodec<BossDungeonFeature> codec() {
        return CODEC;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource rand, BlockPos pos) {
        if (!AdventureConfig.canGenerateIn(world) || rand.nextFloat() > this.successChance) {
            return false;
        }

        int xRadius = 3 + rand.nextInt(3);
        int floor = -1;
        int roof = 4;
        int zRadius = 3 + rand.nextInt(3);
        int doors = 0;

        BlockState[][][] states = new BlockState[xRadius * 2 + 1][6][zRadius * 2 + 1];

        for (int x = -xRadius; x <= xRadius; ++x) {
            for (int y = floor; y <= roof; ++y) {
                for (int z = -zRadius; z <= zRadius; ++z) {
                    BlockPos blockpos = pos.offset(x, y, z);
                    BlockState state = world.getBlockState(blockpos);
                    boolean flag = state.isSolid();

                    if (y == floor && !flag || y == roof && !flag) {
                        return false;
                    }

                    if ((x == -xRadius || x == xRadius || z == -zRadius || z == zRadius) && y == 1 && state.isAir() && states[x + xRadius][y - 1 + 1][z + zRadius].isAir()) {
                        ++doors;
                    }

                    states[x + xRadius][y + 1][z + zRadius] = state;
                }
            }
        }

        if (doors < 3) {
            return false;
        }

        for (int x = -xRadius; x <= xRadius; ++x) {
            for (int y = roof; y >= floor; --y) {
                for (int z = -zRadius; z <= zRadius; ++z) {
                    BlockPos blockpos = pos.offset(x, y, z);
                    BlockState state = states[x + xRadius][y + 1][z + zRadius];

                    if (x != -xRadius && y != floor && z != -zRadius && x != xRadius && y != roof && z != zRadius) {
                        if (!state.is(Blocks.CHEST)) {
                            world.setBlock(blockpos, CAVE_AIR, 2);
                        }
                    }
                    else if (blockpos.getY() >= 0 && !world.getBlockState(blockpos.below()).isSolid()) {
                        world.setBlock(blockpos, CAVE_AIR, 2);
                    }
                    else if (state.isSolid() && !state.is(Blocks.CHEST)) {
                        world.setBlock(blockpos, BRICKS[rand.nextInt(BRICKS.length)], 2);
                    }
                }
            }
        }

        for (int tries = 0; tries < 2; ++tries) {
            for (int j3 = 0; j3 < 3; ++j3) {
                int chestX = pos.getX() + rand.nextInt(xRadius * 2 + 1) - xRadius;
                int chestY = pos.getY();
                int chestZ = pos.getZ() + rand.nextInt(zRadius * 2 + 1) - zRadius;
                BlockPos chestPos = new BlockPos(chestX, chestY, chestZ);

                if (world.isEmptyBlock(chestPos)) {
                    int solidSurroundings = 0;

                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        if (world.getBlockState(chestPos.relative(dir)).isSolid()) {
                            ++solidSurroundings;
                        }
                    }

                    if (solidSurroundings == 1) {
                        world.setBlock(chestPos, StructurePiece.reorient(world, chestPos, Blocks.CHEST.defaultBlockState()), 2);
                        RandomizableContainer.setBlockEntityLootTable(world, rand, chestPos, BuiltInLootTables.SIMPLE_DUNGEON);
                        break;
                    }
                }
            }
        }

        world.setBlock(pos, Apoth.Blocks.BOSS_SPAWNER.value().defaultBlockState(), 2);
        Apotheosis.debugLog(pos, "Boss Dungeon (Variant 1)");
        return true;
    }
}
