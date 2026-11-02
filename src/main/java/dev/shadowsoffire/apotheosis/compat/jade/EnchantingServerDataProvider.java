package dev.shadowsoffire.apotheosis.compat.jade;

import java.util.Set;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry;
import dev.shadowsoffire.apothic_enchanting.table.EnchantmentTableStats;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class EnchantingServerDataProvider implements IServerDataProvider<BlockAccessor> {

    public static final Identifier UID = ApothicEnchanting.loc("enchanting");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BlockState state = accessor.getBlockState();
        if (state.is(Blocks.ENCHANTING_TABLE) || state.is(Ench.Blocks.RAVEN_ENCHANTING_TABLE.value())) {
            EnchantmentTableStats stats = EnchantmentTableStats.gatherStats(accessor.getLevel(), accessor.getPosition());
            CompoundTag enchTag = new CompoundTag();
            enchTag.putFloat("Eterna", stats.eterna());
            enchTag.putFloat("Quanta", stats.quanta());
            enchTag.putFloat("Arcana", stats.arcana());
            enchTag.putInt("Clues", stats.clues());
            tag.put("ApothEnchTable", enchTag);
        }
        else if (state.getBlock() instanceof EnchantmentStatBlock statBlock) {
            float eterna = EnchantingStatRegistry.getEterna(state, accessor.getLevel(), accessor.getPosition());
            float maxEterna = EnchantingStatRegistry.getMaxEterna(state, accessor.getLevel(), accessor.getPosition());
            float quanta = EnchantingStatRegistry.getQuanta(state, accessor.getLevel(), accessor.getPosition());
            float arcana = EnchantingStatRegistry.getArcana(state, accessor.getLevel(), accessor.getPosition());
            int clues = EnchantingStatRegistry.getBonusClues(state, accessor.getLevel(), accessor.getPosition());
            boolean treasure = statBlock.allowsTreasure(state, accessor.getLevel(), accessor.getPosition());
            boolean stable = statBlock.providesStability(state, accessor.getLevel(), accessor.getPosition());

            CompoundTag blockTag = new CompoundTag();
            blockTag.putFloat("Eterna", eterna);
            blockTag.putFloat("MaxEterna", maxEterna);
            blockTag.putFloat("Quanta", quanta);
            blockTag.putFloat("Arcana", arcana);
            blockTag.putInt("Clues", clues);
            blockTag.putBoolean("Treasure", treasure);
            blockTag.putBoolean("Stable", stable);

            Set<Holder<Enchantment>> blacklist = statBlock.getBlacklistedEnchantments(state, accessor.getLevel(), accessor.getPosition());
            if (!blacklist.isEmpty()) {
                ListTag list = new ListTag();
                for (Holder<Enchantment> ench : blacklist) {
                    ench.unwrapKey().ifPresent(k -> list.add(StringTag.valueOf(k.identifier().toString())));
                }
                blockTag.put("Filter", list);
            }
            tag.put("ApothEnchBlock", blockTag);
        }
    }
}
