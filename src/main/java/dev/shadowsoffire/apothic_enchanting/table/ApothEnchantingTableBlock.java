package dev.shadowsoffire.apothic_enchanting.table;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ApothEnchantingTableBlock extends EnchantingTableBlock {

    public ApothEnchantingTableBlock(Block.Properties props) {
        super(props);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof EnchantingTableBlockEntity tile)) {
            return null;
        }
        Component title = ((Nameable) tile).getDisplayName();
        return new ExtendedMenuProvider<BlockPos>() {

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return new ApothEnchantmentMenu(id, inv, ContainerLevelAccess.create(world, pos), tile.getAttachedOrCreate(EnchantmentTableItemHandler.TYPE), pos);
            }

            @Override
            public BlockPos getScreenOpeningData(ServerPlayer player) {
                return pos;
            }
        };
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity be) {
            EnchantmentTableItemHandler handler = be.getAttachedOrCreate(EnchantmentTableItemHandler.TYPE);
            ItemStack fuel = handler.getItem(0);
            if (!fuel.isEmpty()) {
                Block.popResource(level, pos, fuel.copy());
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        for (BlockPos offset : BOOKSHELF_OFFSETS) {
            BlockState shelfState = level.getBlockState(pos.offset(offset));
            if (shelfState.getBlock() instanceof EnchantmentStatBlock statBlock) {
                statBlock.spawnTableParticle(shelfState, level, rand, pos, offset);
            }
        }
    }
}
