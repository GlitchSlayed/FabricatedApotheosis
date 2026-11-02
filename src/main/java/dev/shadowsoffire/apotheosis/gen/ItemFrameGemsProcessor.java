package dev.shadowsoffire.apotheosis.gen;

import java.util.Set;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class ItemFrameGemsProcessor implements StructureProcessor {

    public static final MapCodec<ItemFrameGemsProcessor> CODEC = PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of())
        .xmap(ItemFrameGemsProcessor::new, i -> i.purities);

    protected final Set<Purity> purities;

    public ItemFrameGemsProcessor(Set<Purity> purities) {
        this.purities = purities;
    }

    public Set<Purity> purities() {
        return this.purities;
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }

    public void processEntity(ServerLevelAccessor level, BlockPos pos, CompoundTag nbt, RandomSource rand) {
        String id = nbt.getStringOr("id", "");
        if ("minecraft:item_frame".equals(id)) {
            ServerLevel sLevel = level.getLevel();
            Player player = sLevel.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), -1, false);
            GenContext ctx = player != null ? GenContext.forPlayerAtPos(rand, player, pos) : GenContext.standalone(rand, WorldTier.HAVEN, 0, sLevel, pos);
            Gem gem = GemRegistry.INSTANCE.getRandomItem(ctx);
            if (gem != null) {
                Purity purity = Purity.random(ctx, this.purities);
                ItemStack stack = gem.toStack(purity);
                nbt.put("Item", ItemStack.CODEC.encodeStart(sLevel.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow());
            }
            nbt.put("block_pos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow());
        }
    }
}
