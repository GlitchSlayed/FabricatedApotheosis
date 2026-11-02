package dev.shadowsoffire.apotheosis.client.compat.jei;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.payloads.SetRavenStatsPayload;
import dev.shadowsoffire.apothic_enchanting.table.RavenEnchantmentMenu;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class RavenTransferHandler implements IRecipeTransferHandler<RavenEnchantmentMenu, InfusionRecipe> {

    private final IRecipeTransferHandler<RavenEnchantmentMenu, InfusionRecipe> defaultHandler;

    @SuppressWarnings("unchecked")
    public RavenTransferHandler(IRecipeTransferHandlerHelper helper) {
        IRecipeTransferInfo<RavenEnchantmentMenu, InfusionRecipe> info = helper.createBasicRecipeTransferInfo(
            RavenEnchantmentMenu.class,
            (MenuType<RavenEnchantmentMenu>) (MenuType<?>) Ench.Menus.RAVEN_ENCHANTING_TABLE,
            InfusionRecipeCategory.TYPE,
            0, 1, 2, 36
        );
        this.defaultHandler = helper.createUnregisteredRecipeTransferHandler(info);
    }

    @Override
    public Class<? extends RavenEnchantmentMenu> getContainerClass() {
        return RavenEnchantmentMenu.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MenuType<RavenEnchantmentMenu>> getMenuType() {
        return Optional.of((MenuType<RavenEnchantmentMenu>) (MenuType<?>) Ench.Menus.RAVEN_ENCHANTING_TABLE);
    }

    @Override
    public IRecipeType<InfusionRecipe> getRecipeType() {
        return InfusionRecipeCategory.TYPE;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
        RavenEnchantmentMenu menu,
        InfusionRecipe recipe,
        IRecipeSlotsView recipeSlots,
        Player player,
        boolean maxTransfer,
        boolean doTransfer
    ) {
        IRecipeTransferError error = this.defaultHandler.transferRecipe(menu, recipe, recipeSlots, player, maxTransfer, doTransfer);
        if (error != null) {
            return error;
        }

        if (doTransfer) {
            int eterna = (int) Math.ceil(recipe.getRequirements().eterna());
            int quanta = (int) Math.ceil(recipe.getRequirements().quanta());
            int arcana = (int) Math.ceil(recipe.getRequirements().arcana());
            ClientPlayNetworking.send(new SetRavenStatsPayload(eterna, quanta, arcana));
            menu.setRavenStats(eterna, quanta, arcana);
        }

        return null;
    }
}
