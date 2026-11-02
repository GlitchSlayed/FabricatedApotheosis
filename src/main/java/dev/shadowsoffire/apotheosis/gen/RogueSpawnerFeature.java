package dev.shadowsoffire.apotheosis.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawner;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawnerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class RogueSpawnerFeature implements Feature {

    public static final MapCodec<RogueSpawnerFeature> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.floatRange(0, 1).optionalFieldOf("success_chance", 1F).forGetter(RogueSpawnerFeature::successChance)
    ).apply(inst, RogueSpawnerFeature::new));

    public static final RuleTest STONE_TEST = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);

    private final float successChance;

    public RogueSpawnerFeature(float successChance) {
        this.successChance = successChance;
    }

    public RogueSpawnerFeature() {
        this(1.0f);
    }

    public float successChance() {
        return this.successChance;
    }

    @Override
    public MapCodec<RogueSpawnerFeature> codec() {
        return CODEC;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource rand, BlockPos pos) {
        if (!AdventureConfig.canGenerateIn(world) || rand.nextFloat() > this.successChance) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        BlockState downState = world.getBlockState(pos.below());
        BlockState upState = world.getBlockState(pos.above());
        if (STONE_TEST.test(downState, pos.below(), rand) && upState.isAir() && (state.isAir() || STONE_TEST.test(state, pos, rand))) {
            RogueSpawner item = RogueSpawnerRegistry.INSTANCE.getRandomItem(rand);
            if (item == null) {
                return false;
            }
            item.place(world, pos, rand);
            Apotheosis.debugLog(pos, "Rogue Spawner - " + RogueSpawnerRegistry.INSTANCE.getKey(item));
            return true;
        }

        return false;
    }
}
