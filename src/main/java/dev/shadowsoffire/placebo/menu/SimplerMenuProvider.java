package dev.shadowsoffire.placebo.menu;

import dev.shadowsoffire.placebo.menu.MenuUtil.PosFactory;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

public class SimplerMenuProvider<M extends AbstractContainerMenu> implements ExtendedMenuProvider<BlockPos> {

    private final Component title;
    private final PosFactory<M> factory;
    private final BlockPos pos;

    public SimplerMenuProvider(Level level, BlockPos pos, PosFactory<M> factory) {
        this.factory = factory;
        this.pos = pos;
        this.title = Component.translatable(level.getBlockState(pos).getBlock().getDescriptionId());
    }

    @Override
    public Component getDisplayName() {
        return this.title;
    }

    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return this.factory.create(pContainerId, pInventory, this.pos);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.pos;
    }
}
