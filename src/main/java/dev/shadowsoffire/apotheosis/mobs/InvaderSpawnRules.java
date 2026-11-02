package dev.shadowsoffire.apotheosis.mobs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * Record holding per-dimension invader spawn rules.
 *
 * @param spawnChances The per-world-tier spawn chances for invaders in the target dimension.
 * @param cooldown     An optional cooldown override for this dimension. If not set, the configured default cooldown will be used.
 * @param surfaceType  The surface type used for this dimension.
 */
public record InvaderSpawnRules(Map<WorldTier, Float> spawnChances, SurfaceType surfaceType, Optional<Integer> cooldown, Optional<Boolean> cursed, Optional<Boolean> autoAggro) {

    public static final Codec<InvaderSpawnRules> CODEC = RecordCodecBuilder.<InvaderSpawnRules>create(inst -> inst
        .group(
            WorldTier.mapCodec(Codec.floatRange(0, 1)).fieldOf("spawn_chances").forGetter(InvaderSpawnRules::spawnChances),
            SurfaceType.CODEC.fieldOf("surface_type").forGetter(InvaderSpawnRules::surfaceType),
            Codec.intRange(0, 720000).optionalFieldOf("cooldown").forGetter(InvaderSpawnRules::cooldown),
            Codec.BOOL.optionalFieldOf("cursed").forGetter(InvaderSpawnRules::cursed),
            Codec.BOOL.optionalFieldOf("auto_aggro").forGetter(InvaderSpawnRules::autoAggro))
        .apply(inst, InvaderSpawnRules::new))
        .validate(InvaderSpawnRules::validate);

    public InvaderSpawnRules(Map<WorldTier, Float> spawnChances, SurfaceType surfaceType) {
        this(spawnChances, surfaceType, Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static DataResult<InvaderSpawnRules> validate(InvaderSpawnRules rules) {
        if (rules.spawnChances.size() == WorldTier.values().length) {
            return DataResult.success(rules);
        }
        else {
            StringBuilder sb = new StringBuilder("Missing Spawn Chances for the following world tiers: ");
            for (WorldTier tier : WorldTier.values()) {
                if (!rules.spawnChances.containsKey(tier)) {
                    sb.append(tier.getSerializedName()).append(" ");
                }
            }
            return DataResult.error(sb::toString);
        }
    }

    private static final Map<Identifier, InvaderSpawnRules> RULES = new HashMap<>();

    static {
        Map<WorldTier, Float> owChances = Map.of(
            WorldTier.HAVEN, 0.0f,
            WorldTier.FRONTIER, 0.018f,
            WorldTier.ASCENT, 0.02f,
            WorldTier.SUMMIT, 0.025f,
            WorldTier.PINNACLE, 0.03f);
        RULES.put(BuiltinDimensionTypes.OVERWORLD.identifier(), new InvaderSpawnRules(owChances, SurfaceType.NEEDS_SKY_OR_SAME_VERTICAL_SLICE));
        RULES.put(BuiltinDimensionTypes.OVERWORLD_CAVES.identifier(), new InvaderSpawnRules(owChances, SurfaceType.NEEDS_SKY_OR_SAME_VERTICAL_SLICE));

        Map<WorldTier, Float> netherChances = Map.of(
            WorldTier.HAVEN, 0.0f,
            WorldTier.FRONTIER, 0.025f,
            WorldTier.ASCENT, 0.027f,
            WorldTier.SUMMIT, 0.03f,
            WorldTier.PINNACLE, 0.035f);
        RULES.put(BuiltinDimensionTypes.NETHER.identifier(), new InvaderSpawnRules(netherChances, SurfaceType.ANY));

        Map<WorldTier, Float> endChances = Map.of(
            WorldTier.HAVEN, 0.0f,
            WorldTier.FRONTIER, 0.018f,
            WorldTier.ASCENT, 0.02f,
            WorldTier.SUMMIT, 0.025f,
            WorldTier.PINNACLE, 0.03f);
        RULES.put(BuiltinDimensionTypes.END.identifier(), new InvaderSpawnRules(endChances, SurfaceType.SURFACE_OUTER_END));
    }

    public static void registerRules(Identifier dimensionId, InvaderSpawnRules rules) {
        RULES.put(dimensionId, rules);
    }

    public static InvaderSpawnRules getRules(ResourceKey<DimensionType> dimKey) {
        if (dimKey == null) {
            return null;
        }
        return RULES.get(dimKey.identifier());
    }

    public static InvaderSpawnRules getRules(Identifier dimId) {
        if (dimId == null) {
            return null;
        }
        return RULES.get(dimId);
    }
}
