package dev.shadowsoffire.placebo.menu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;

public class MenuUtil {

    public static <T extends AbstractContainerMenu> MenuType<T> type(MenuSupplier<T> factory) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> posType(PosFactory<T> factory) {
        return new ExtendedMenuType<>(factory::create, BlockPos.STREAM_CODEC);
    }

    public static <T extends AbstractContainerMenu, D> MenuType<T> bufType(ExtendedMenuType.ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return new ExtendedMenuType<>(factory, codec);
    }

    public static <M extends AbstractContainerMenu> InteractionResult openGui(Player player, BlockPos pos, PosFactory<M> factory) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new SimplerMenuProvider<>(player.level(), pos, factory));
        return InteractionResult.CONSUME;
    }

    @FunctionalInterface
    public interface PosFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory pInv, BlockPos pos);
    }
}
