package dev.shadowsoffire.apotheosis.client.compat.jei;

import java.util.List;

import org.joml.Matrix3x2fStack;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class SpawnerCategory implements IRecipeCategory<SpawnerModifier> {

    public static final Identifier TEXTURES = ApothicSpawners.loc("textures/gui/spawner_jei.png");
    public static final IRecipeType<SpawnerModifier> TYPE = IRecipeType.create(ApothicSpawners.MODID, "spawner_modifiers", SpawnerModifier.class);

    private final IDrawable bg;
    private final IDrawable icon;
    private final Component title;

    public SpawnerCategory(IGuiHelper helper) {
        this.bg = helper.drawableBuilder(TEXTURES, 0, 0, 169, 75).build();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.SPAWNER));
        this.title = ApothicSpawners.lang("title", "spawner");
    }

    @Override
    public IRecipeType<SpawnerModifier> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 169;
    }

    @Override
    public int getHeight() {
        return 75;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SpawnerModifier recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 11, 11).add(recipe.mainHand());
        if (recipe.offHand().isPresent()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 11, 48).add(recipe.offHand().get());
        }
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, -1000, -1000).add(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SPAWNER));
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(SpawnerModifier recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.bg.draw(gfx, 0, 0);
        if (recipe.offHand().isEmpty()) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, 1, 31, 0, 88, 28, 34, 256, 256);
        }

        Font font = Minecraft.getInstance().font;

        Matrix3x2fStack mvStack = gfx.pose();
        mvStack.pushMatrix();
        mvStack.translate(0, 0.5f);
        gfx.fakeItem(new ItemStack(Items.SPAWNER), 31, 29);
        mvStack.popMatrix();

        StatModifier<?> sm = recipe.statModifiers().get(0);
        Component text = sm.stat().desc();
        gfx.text(font, text, 43, 9, 0xFF404040, false);

        int color = sm.value() instanceof Number num && num.doubleValue() < 0 ? 0xFFFF6060 : 0xFF6060FF;
        String valStr = sm.value().toString();
        if (sm.value() instanceof Number n && n.doubleValue() > 0) {
            valStr = "+" + valStr;
        }
        gfx.text(font, Component.literal(valStr), 43, 21, color, false);

        if (sm.min().isPresent() && sm.max().isPresent()) {
            String bounds = String.format("[%s, %s]", sm.min().get(), sm.max().get());
            gfx.text(font, Component.literal(bounds).withStyle(ChatFormatting.GRAY), 43, 33, 0xFF808080, false);
        }
    }
}
