package dev.shadowsoffire.apotheosis.client.compat.jei;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipeCache;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.client.attributes.AttributesGui;
import dev.shadowsoffire.apotheosis.client.attributes.AttributesGuiHolder;
import dev.shadowsoffire.apotheosis.client.compat.jei.PotionCharmExtension.PotionCharmSubtypes;
import dev.shadowsoffire.apotheosis.client.compat.jei.StoneformingCategory.StoneformingDisplay;
import dev.shadowsoffire.apotheosis.client.mixin.AbstractContainerScreenAccessor;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.compat.enchanting.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.recipe.SupremacyRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipeCache;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseScreen;
import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.ApothEnchantmentMenu;
import dev.shadowsoffire.apothic_enchanting.table.ApothEnchantmentScreen;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipeCache;
import dev.shadowsoffire.apothic_spawners.compat.SpawnerRecipeCache;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class ApotheosisJeiPlugin implements IModPlugin {

    public static final Identifier PLUGIN_UID = Apotheosis.loc("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new SpawnerCategory(helper),
            new SalvagingCategory(helper),
            new GemCuttingCategory(helper),
            new InfusionRecipeCategory(helper),
            new StoneformingCategory(helper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SpawnerCategory.TYPE, SpawnerRecipeCache.getRecipes());
        registration.addRecipes(SalvagingCategory.TYPE, SalvagingRecipeCache.all().stream().map(RecipeHolder::value).toList());
        registration.addRecipes(GemCuttingCategory.TYPE, GemCuttingRecipeCache.all().stream().map(RecipeHolder::value).toList());
        registration.addRecipes(InfusionRecipeCategory.TYPE, InfusionRecipeCache.all());

        List<StoneformingDisplay> stoneforming = AffixRegistry.INSTANCE.getValues().stream()
            .filter(StoneformingAffix.class::isInstance)
            .sorted(Comparator.comparing(Affix::id))
            .map(affix -> new StoneformingDisplay(AffixRegistry.INSTANCE.holder(affix)))
            .toList();
        registration.addRecipes(StoneformingCategory.TYPE, stoneforming);

        // Ingredient Information
        Component socketInfo = Component.translatable("info.apotheosis.socketing");
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            for (Purity purity : Purity.ALL_PURITIES) {
                if (purity.isAtLeast(gem.getMinPurity())) {
                    registration.addIngredientInfo(gem.toStack(purity), VanillaTypes.ITEM_STACK, socketInfo);
                }
            }
        }
        registration.addIngredientInfo(Blocks.SPAWNER, Component.translatable("info.apoth_spawners.spawner"));
        registration.addIngredientInfo(Apoth.Items.GEM_DUST.value(), Component.translatable("info.apotheosis.gem_dust"));
        registration.addIngredientInfo(Apoth.Items.SIGIL_OF_SOCKETING.value(), Component.translatable("info.apotheosis.socketing"));
        registration.addIngredientInfo(Apoth.Items.SIGIL_OF_ENHANCEMENT.value(), Component.translatable("info.apotheosis.sigil_of_enhancement"));
        registration.addIngredientInfo(Apoth.Items.SIGIL_OF_UNNAMING.value(), Component.translatable("info.apotheosis.unnaming"));
        registration.addIngredientInfo(Apoth.Items.SIGIL_OF_WITHDRAWAL.value(), Component.translatable("info.apotheosis.withdrawal"));
        registration.addIngredientInfo(Ench.Items.LIBRARY.value(), Component.translatable("info.apothic_enchanting.library"));
        registration.addIngredientInfo(Blocks.ENCHANTING_TABLE, Component.translatable("info.apothic_enchanting.enchanting_table"));
        registration.addIngredientInfo(Ench.Blocks.APOTHIC_ENCHANTING_TABLE.value(), Component.translatable("info.apothic_enchanting.enchanting_table"));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(Apoth.Items.GEM.value(), new GemSubtypes());
        registration.registerSubtypeInterpreter(Apoth.Items.POTION_CHARM.value(), new PotionCharmSubtypes());
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(PotionCharmRecipe.class, new PotionCharmExtension());
        if (ApothicEnchantingCompat.isLoaded()) {
            InfusionRecipeCategory.registerExtension(CharmInfusionRecipe.class, new CharmInfusionExtension());
        }
        registration.getSmithingCategory().addExtension(SizedUpgradeRecipe.class, new SizedUpgradeRecipeExtension());
        registration.getSmithingCategory().addExtension(AddSocketsRecipe.class, new AddSocketsExtension());
        registration.getSmithingCategory().addExtension(WithdrawalRecipe.class, new WithdrawalExtension());
        registration.getSmithingCategory().addExtension(UnnamingRecipe.class, new UnnamingExtension());
        registration.getSmithingCategory().addExtension(MaliceRecipe.class, new MaliceExtension());
        registration.getSmithingCategory().addExtension(SupremacyRecipe.class, new SupremacyExtension());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(SpawnerCategory.TYPE, Blocks.SPAWNER);
        registration.addCraftingStation(SalvagingCategory.TYPE, Apoth.Blocks.SALVAGING_TABLE.value());
        registration.addCraftingStation(GemCuttingCategory.TYPE, Apoth.Blocks.GEM_CUTTING_TABLE.value());
        registration.addCraftingStation(InfusionRecipeCategory.TYPE, Blocks.ENCHANTING_TABLE, Ench.Blocks.APOTHIC_ENCHANTING_TABLE.value(), Ench.Blocks.RAVEN_ENCHANTING_TABLE.value());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
            ApothEnchantmentMenu.class,
            (MenuType<ApothEnchantmentMenu>) (MenuType<?>) Ench.Menus.ENCHANTING_TABLE,
            InfusionRecipeCategory.TYPE,
            0, 1, 2, 36
        );
        registration.addRecipeTransferHandler(new RavenTransferHandler(registration.getTransferHelper()), InfusionRecipeCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(InventoryScreen.class, new IGuiContainerHandler<InventoryScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(InventoryScreen screen) {
                if (screen instanceof AttributesGuiHolder holder) {
                    AttributesGui gui = holder.apoth_getAttributesGui();
                    if (gui != null) {
                        return gui.getExclusionAreas();
                    }
                }
                return List.of();
            }
        });

        registration.addGuiContainerHandler(ApothEnchantmentScreen.class, new IGuiContainerHandler<ApothEnchantmentScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(ApothEnchantmentScreen screen) {
                // Info tab extending above top edge
                AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
                return List.of(new Rect2i(accessor.apoth_getLeftPos() + 145, accessor.apoth_getTopPos() - 15, 27, 15));
            }
        });

        registration.addGuiContainerHandler(GemCaseScreen.class, new IGuiContainerHandler<GemCaseScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(GemCaseScreen screen) {
                return screen.getExclusionAreas();
            }
        });

        registration.addRecipeClickArea(ApothEnchantmentScreen.class, 145, -15, 27, 15, InfusionRecipeCategory.TYPE);
        registration.addRecipeClickArea(SalvagingScreen.class, 98, 34, 18, 18, SalvagingCategory.TYPE);
    }

    static class GemSubtypes implements ISubtypeInterpreter<ItemStack> {
        @Override
        public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
            UnsocketedGem inst = UnsocketedGem.of(stack);
            if (!inst.isValid()) {
                return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            }
            return inst.gem().getId() + "@" + inst.purity().getSerializedName();
        }
    }
}
