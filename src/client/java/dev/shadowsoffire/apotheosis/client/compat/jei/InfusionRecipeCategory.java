package dev.shadowsoffire.apotheosis.client.compat.jei;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.ApothEnchantmentScreen;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import dev.shadowsoffire.apothic_enchanting.util.TooltipUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;

public class InfusionRecipeCategory implements IRecipeCategory<InfusionRecipe> {

    public static final Identifier UID = ApothicEnchanting.loc("enchanting");
    public static final IRecipeType<InfusionRecipe> TYPE = IRecipeType.create(ApothicEnchanting.MODID, "enchanting", InfusionRecipe.class);
    public static final Identifier TEXTURES = ApothicEnchanting.loc("textures/gui/enchanting_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public InfusionRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURES, 0, 0, 170, 56);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.ENCHANTING_TABLE));
        this.localizedName = TooltipUtil.lang("recipes", "infusion");
    }

    @Override
    public int getWidth() {
        return 170;
    }

    @Override
    public int getHeight() {
        return 56;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public IRecipeType<InfusionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 6, 6);
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 37, 6);
        output.add(VanillaTypes.ITEM_STACK, recipe.getOutput().create());
        input.add(recipe.getInput());
    }

    @Override
    public void draw(InfusionRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 0, 0, 0, 0, 170, 56, 256, 256);

        boolean hover = false;
        if (mouseX > 57 && mouseX <= 57 + 108 && mouseY > 4 && mouseY <= 4 + 19) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 57, 4, 0, 71, 108, 19, 256, 256);
            hover = true;
        }

        Font font = Minecraft.getInstance().font;
        Stats stats = recipe.getRequirements();
        Stats maxStats = recipe.getMaxRequirements();
        gfx.text(font, TooltipUtil.lang("gui", "enchant.eterna"), 16, 26, 0xFF3DB53D, false);
        gfx.text(font, TooltipUtil.lang("gui", "enchant.quanta"), 16, 36, 0xFFFC5454, false);
        gfx.text(font, TooltipUtil.lang("gui", "enchant.arcana"), 16, 46, 0xFFA800A8, false);
        int level = (int) stats.eterna();

        String s = "" + level;
        int width = 86 - font.width(s);
        EnchantmentNames.getInstance().initSeed(recipe.hashCode());
        FormattedText itextproperties = EnchantmentNames.getInstance().getRandomName(font, width);
        int color = hover ? 0xFFFFFF80 : 0xFF685E4A;
        drawWordWrap(font, itextproperties, 77, 6, width, color, gfx);
        color = 0xFF80FF20;
        gfx.text(font, s, 77 + width, 13, color);

        int[] pos = { getBarLength(stats.eterna()), getBarLength(stats.quanta()), getBarLength(stats.arcana()) };
        if (stats.eterna() > 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 56, 27, 0, 56, pos[0], 5, 256, 256);
        }
        if (stats.quanta() > 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 56, 37, 0, 61, pos[1], 5, 256, 256);
        }
        if (stats.arcana() > 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 56, 47, 0, 66, pos[2], 5, 256, 256);
        }
        if (maxStats.eterna() > 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 56 + pos[0], 27, pos[0], 90, getBarLength(maxStats.eterna() - stats.eterna()), 5, 256, 256);
        }
        if (maxStats.quanta() > 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 56 + pos[1], 37, pos[1], 95, getBarLength(maxStats.quanta() - stats.quanta()), 5, 256, 256);
        }
        if (maxStats.arcana() > 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 56 + pos[2], 47, pos[2], 100, getBarLength(maxStats.arcana() - stats.arcana()), 5, 256, 256);
        }

        if (hover) {
            List<Component> list = new ArrayList<>();
            Holder<Enchantment> ench = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Ench.Enchantments.INFUSION);
            Component infusionName = Enchantment.getFullname(ench, 1);
            list.add(Component.translatable("container.enchant.clue", infusionName).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            drawTooltip(gfx, font, list, (int) mouseX, (int) mouseY);
        }
        else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 26 && mouseY <= 27 + 5) {
            List<Component> list = new ArrayList<>();
            list.add(TooltipUtil.lang("gui", "enchant.eterna").withStyle(ChatFormatting.GREEN));
            if (maxStats.eterna() == stats.eterna()) {
                list.add(TooltipUtil.lang("info", "eterna_exact", stats.eterna(), 100).withStyle(ChatFormatting.GRAY));
            }
            else {
                list.add(TooltipUtil.lang("info", "eterna_at_least", stats.eterna(), 100).withStyle(ChatFormatting.GRAY));
                if (maxStats.eterna() > -1) list.add(TooltipUtil.lang("info", "eterna_at_most", maxStats.eterna(), 100).withStyle(ChatFormatting.GRAY));
            }
            drawTooltip(gfx, font, list, (int) mouseX, (int) mouseY);
        }
        else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 36 && mouseY <= 37 + 5) {
            List<Component> list = new ArrayList<>();
            list.add(TooltipUtil.lang("gui", "enchant.quanta").withStyle(ChatFormatting.RED));
            if (maxStats.quanta() == stats.quanta()) {
                list.add(TooltipUtil.lang("info", "percent_exact", stats.quanta()).withStyle(ChatFormatting.GRAY));
            }
            else {
                list.add(TooltipUtil.lang("info", "percent_at_least", stats.quanta()).withStyle(ChatFormatting.GRAY));
                if (maxStats.quanta() > -1) list.add(TooltipUtil.lang("info", "percent_at_most", maxStats.quanta()).withStyle(ChatFormatting.GRAY));
            }
            drawTooltip(gfx, font, list, (int) mouseX, (int) mouseY);
        }
        else if (mouseX > 56 && mouseX <= 56 + 110 && mouseY > 46 && mouseY <= 47 + 5) {
            List<Component> list = new ArrayList<>();
            list.add(TooltipUtil.lang("gui", "enchant.arcana").withStyle(ChatFormatting.DARK_PURPLE));
            if (maxStats.arcana() == stats.arcana()) {
                list.add(TooltipUtil.lang("info", "percent_exact", stats.arcana()).withStyle(ChatFormatting.GRAY));
            }
            else {
                list.add(TooltipUtil.lang("info", "percent_at_least", stats.arcana()).withStyle(ChatFormatting.GRAY));
                if (maxStats.arcana() > -1) list.add(TooltipUtil.lang("info", "percent_at_most", maxStats.arcana()).withStyle(ChatFormatting.GRAY));
            }
            drawTooltip(gfx, font, list, (int) mouseX, (int) mouseY);
        }
    }

    public static int getBarLength(float stat) {
        return ApothEnchantmentScreen.getBarLength(stat);
    }

    public static void drawWordWrap(Font font, FormattedText pText, int pX, int pY, int pMaxWidth, int pColor, GuiGraphicsExtractor gfx) {
        for (FormattedCharSequence formattedcharsequence : font.split(pText, pMaxWidth)) {
            gfx.text(font, formattedcharsequence, pX, pY, pColor, false);
            pY += 9;
        }
    }

    private static void drawTooltip(GuiGraphicsExtractor gfx, Font font, List<Component> list, int x, int y) {
        gfx.tooltip(
            font,
            list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
            x, y,
            DefaultTooltipPositioner.INSTANCE,
            null, false);
    }
}
