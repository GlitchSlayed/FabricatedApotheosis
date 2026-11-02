package dev.shadowsoffire.apotheosis.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Boss Dungeon Feature (Variant 2)
 */
public class BossDungeonFeature2 implements Feature {

    public static final MapCodec<BossDungeonFeature2> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.floatRange(0, 1).optionalFieldOf("success_chance", 1F).forGetter(BossDungeonFeature2::successChance)
    ).apply(inst, BossDungeonFeature2::new));

    public static final Identifier TEMPLATE_ID = Apotheosis.loc("boss_1");

    protected static int xRadius = 4;
    protected static int floor = -1;
    protected static int roof = 3;
    protected static int roofTop = 6;
    protected static int zRadius = 4;

    private final float successChance;

    public BossDungeonFeature2(float successChance) {
        this.successChance = successChance;
    }

    public BossDungeonFeature2() {
        this(1.0f);
    }

    public float successChance() {
        return this.successChance;
    }

    @Override
    public MapCodec<BossDungeonFeature2> codec() {
        return CODEC;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource rand, BlockPos pos) {
        if (!AdventureConfig.canGenerateIn(world) || rand.nextFloat() > this.successChance) {
            return false;
        }

        BlockState[][][] states = new BlockState[9][8][9];

        int doors = 0;

        for (int x = -xRadius; x <= xRadius; ++x) {
            for (int y = floor; y <= roofTop; ++y) {
                for (int z = -zRadius; z <= zRadius; ++z) {
                    BlockPos blockpos = pos.offset(x, y, z);
                    BlockState state = world.getBlockState(blockpos);
                    boolean flag = state.isSolid();

                    if (y == floor && !flag || y == roof && !flag) {
                        return false;
                    }
                    if (y == roof + 1 && Math.abs(x) < xRadius && Math.abs(z) < zRadius && !flag) {
                        return false;
                    }

                    if (isDoorSpace(x, z) && y == 1 && state.isAir() && states[x + xRadius][y - 1 + 1][z + zRadius].isAir()) {
                        ++doors;
                    }

                    states[x + xRadius][y + 1][z + zRadius] = state;
                }
            }
        }

        if (doors >= 3) {
            MinecraftServer server = world.getLevel().getServer();
            if (server != null) {
                StructureTemplate template = server.getStructureTemplateManager().get(TEMPLATE_ID).orElse(null);
                if (template != null) {
                    template.placeInWorld(world, pos.offset(-4, -1, -4), pos.offset(-4, -1, -4), new StructurePlaceSettings(), rand, 4);

                    boolean rand1 = rand.nextBoolean();
                    boolean rand2 = rand.nextBoolean();

                    BlockPos chest1 = pos.offset(rand1 ? xRadius - 1 : -xRadius + 1, 0, rand2 ? zRadius - 1 : -zRadius + 1);
                    BlockPos chest2 = pos.offset(!rand1 ? xRadius - 1 : -xRadius + 1, 0, !rand2 ? zRadius - 1 : -zRadius + 1);

                    world.setBlock(chest1, StructurePiece.reorient(world, chest1, Blocks.CHEST.defaultBlockState()), 2);
                    RandomizableContainer.setBlockEntityLootTable(world, rand, chest1, BuiltInLootTables.SIMPLE_DUNGEON);
                    world.setBlock(chest2, StructurePiece.reorient(world, chest2, Blocks.CHEST.defaultBlockState()), 2);
                    RandomizableContainer.setBlockEntityLootTable(world, rand, chest2, BuiltInLootTables.SIMPLE_DUNGEON);

                    world.setBlock(pos, Apoth.Blocks.BOSS_SPAWNER.value().defaultBlockState(), 2);
                    Apotheosis.debugLog(pos, "Boss Dungeon (Variant 2)");
                    return true;
                }
            }
        }

        return false;
    }

    protected static boolean isDoorSpace(int x, int z) {
        return (x == -xRadius || x == xRadius) && Math.abs(z) <= 1 || (z == -zRadius || z == zRadius) && Math.abs(x) <= 1;
    }
}
