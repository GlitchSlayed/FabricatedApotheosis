package dev.shadowsoffire.apotheosis.tiers;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.compat.GameStagesCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Generation context for random objects which are sensitive to the current world tier and luck levels.
 *
 * @param random The random source
 * @param tier   The relevant (or closest) player's world tier.
 * @param luck   The relevant (or closest) player's luck level.
 */
public record GenContext(RandomSource rand, WorldTier tier, float luck, ResourceKey<Level> dimension, Holder<Biome> biome, Set<String> stages) {

    /**
     * Returns a generation context for the target player, using an external random source.
     */
    public static GenContext forPlayerAtPos(RandomSource rand, Player player, BlockPos pos) {
        Level level = player.level();
        return new GenContext(rand, WorldTier.getTier(player), player.getLuck(), level.dimension(), level.getBiome(pos), GameStagesCompat.getStages(player));
    }

    /**
     * Returns a generation context for the target player, using an external random source.
     */
    public static GenContext forPlayer(RandomSource rand, Player player) {
        return forPlayerAtPos(rand, player, player.blockPosition());
    }

    /**
     * Returns a generation context for the target player, using the player's random source.
     */
    public static GenContext forPlayer(Player player) {
        return forPlayer(player.getRandom(), player);
    }

    public static GenContext forLoot(LootContext ctx) {
        Player player = findPlayer(ctx);
        Vec3 origin = ctx.getOptional(LootContextParams.ORIGIN);
        if (player == null && origin != null) {
            player = ctx.getLevel().getNearestPlayer(origin.x, origin.y, origin.z, 64, false);
        }
        if (player != null) {
            BlockPos pos = origin != null ? BlockPos.containing(origin) : player.blockPosition();
            return new GenContext(ctx.getRandom(), WorldTier.getTier(player), ctx.getLuck(), ctx.getLevel().dimension(), ctx.getLevel().getBiome(pos), GameStagesCompat.getStages(player));
        }
        BlockPos pos = origin != null ? BlockPos.containing(origin) : BlockPos.ZERO;
        return GenContext.standalone(ctx.getRandom(), WorldTier.HAVEN, ctx.getLuck(), ctx.getLevel(), pos);
    }

    public static GenContext standalone(RandomSource rand, WorldTier tier, float luck, ServerLevel level, BlockPos pos) {
        return new GenContext(rand, tier, luck, level.dimension(), level.getBiome(pos), Set.of());
    }

    public static GenContext dummy(RandomSource rand) {
        var lookup = dev.shadowsoffire.placebo.dynreg.DynamicRegistry.getActiveLookup();
        Holder<Biome> plains = lookup != null ? lookup.lookup(Registries.BIOME).flatMap(l -> l.get(Biomes.PLAINS)).orElse(null) : null;
        return new GenContext(rand, WorldTier.HAVEN, 0, Level.OVERWORLD, plains, Set.of());
    }

    @Nullable
    public static Player findPlayer(LootContext ctx) {
        if (ctx.getOptional(LootContextParams.THIS_ENTITY) instanceof Player p) {
            return p;
        }
        if (ctx.getOptional(LootContextParams.ATTACKING_ENTITY) instanceof Player p) {
            return p;
        }
        if (ctx.getOptional(LootContextParams.DIRECT_ATTACKING_ENTITY) instanceof Player p) {
            return p;
        }
        if (ctx.getOptional(LootContextParams.LAST_DAMAGE_PLAYER) != null) {
            return ctx.getOptional(LootContextParams.LAST_DAMAGE_PLAYER);
        }
        return null;
    }

    @Override
    public final String toString() {
        String biomeStr = this.biome.unwrapKey().map(k -> k.identifier().toString()).orElse("unregistered");
        return "GenContext[tier=%s, luck=%s, dimension=%s, biome=%s, stages=%s]".formatted(this.tier.getSerializedName(), this.luck, this.dimension.identifier(), biomeStr, this.stages);
    }

}
