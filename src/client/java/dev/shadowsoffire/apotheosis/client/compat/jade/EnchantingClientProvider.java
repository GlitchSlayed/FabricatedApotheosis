package dev.shadowsoffire.apotheosis.client.compat.jade;

import java.util.Collections;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import dev.shadowsoffire.apothic_enchanting.table.EnchantmentTableStats;
import dev.shadowsoffire.apothic_enchanting.util.TooltipUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class EnchantingClientProvider implements IBlockComponentProvider {

    public static final Identifier UID = ApothicEnchanting.loc("enchanting");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(UID)) return;

        BlockState state = accessor.getBlockState();
        if (state.is(Blocks.ENCHANTING_TABLE) || state.is(Ench.Blocks.RAVEN_ENCHANTING_TABLE.value())) {
            CompoundTag data = accessor.getServerData();
            if (data != null && data.getCompound("ApothEnchTable").isPresent()) {
                CompoundTag t = data.getCompound("ApothEnchTable").get();
                EnchantmentTableStats stats = new EnchantmentTableStats(
                    t.getFloatOr("Eterna", 0f),
                    t.getFloatOr("Quanta", 0f),
                    t.getFloatOr("Arcana", 0f),
                    t.getIntOr("Clues", 0),
                    Collections.emptySet(),
                    false,
                    false
                );
                TooltipUtil.appendTableStats(stats, tooltip::add);
            }
            else {
                TooltipUtil.appendTableStats(accessor.getLevel(), accessor.getPosition(), tooltip::add);
            }
        }
        else if (state.getBlock() instanceof EnchantmentStatBlock) {
            TooltipUtil.appendBlockStats(accessor.getLevel(), state, accessor.getPosition(), tooltip::add);
        }
    }
}
