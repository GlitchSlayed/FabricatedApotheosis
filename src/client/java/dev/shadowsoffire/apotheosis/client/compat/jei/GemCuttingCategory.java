package dev.shadowsoffire.apotheosis.client.compat.jei;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.BasicGemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingBlock;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class GemCuttingCategory implements IRecipeCategory<GemCuttingRecipe> {

    public static final Identifier TEXTURES = Apotheosis.loc("textures/gui/gem_cutting_jei.png");
    public static final IRecipeType<GemCuttingRecipe> TYPE = IRecipeType.create(Apotheosis.MODID, "gem_cutting", GemCuttingRecipe.class);

    private static final Map<Class<?>, GemCuttingExtension<?>> EXTENSIONS = new IdentityHashMap<>();

    private final IDrawable background;
    private final IDrawable icon;

    public GemCuttingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 148, 78).addPadding(0, 0, 0, 0).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.GEM_CUTTING_TABLE.value()));
    }

    @Override
    public IRecipeType<GemCuttingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return GemCuttingBlock.NAME;
    }

    @Override
    public int getWidth() {
        return 148;
    }

    @Override
    public int getHeight() {
        return 78;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(GemCuttingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setRecipe(IRecipeLayoutBuilder builder, GemCuttingRecipe recipe, IFocusGroup focuses) {
        GemCuttingExtension ext = EXTENSIONS.get(recipe.getClass());
        if (ext != null) {
            ext.setRecipe(builder, recipe, focuses);
        }
    }

    public static <T extends GemCuttingRecipe> void registerExtension(Class<T> cls, GemCuttingExtension<T> ext) {
        EXTENSIONS.put(cls, ext);
    }

    public interface GemCuttingExtension<T extends GemCuttingRecipe> {
        void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);
    }

    static {
        registerExtension(PurityUpgradeRecipe.class, new PurityUpgradeExtension());
        registerExtension(BasicGemCuttingRecipe.class, new BasicGemCuttingExtension());
    }

    public static class PurityUpgradeExtension implements GemCuttingExtension<PurityUpgradeRecipe> {
        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, PurityUpgradeRecipe recipe, IFocusGroup focuses) {
            DynamicHolder<Gem> focus = GemItem.getGem(focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().map(IFocus::getTypedValue).map(ITypedIngredient::getIngredient).orElse(ItemStack.EMPTY));
            List<ItemStack> inputs = new ArrayList<>();
            List<ItemStack> outputs = new ArrayList<>();

            if (focus.isBound()) {
                inputs.add(focus.get().toStack(recipe.purity()));
                outputs.add(focus.get().toStack(recipe.purity().next()));
            }
            else {
                GemRegistry.INSTANCE.getValues().stream().forEachOrdered(g -> {
                    if (recipe.purity().isAtLeast(g.getMinPurity())) {
                        inputs.add(g.toStack(recipe.purity()));
                        outputs.add(g.toStack(recipe.purity().next()));
                    }
                });
            }

            builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredients(VanillaTypes.ITEM_STACK, inputs);
            builder.addSlot(RecipeIngredientRole.INPUT, 48, 4).addIngredients(VanillaTypes.ITEM_STACK, inputs);
            builder.addSlot(RecipeIngredientRole.INPUT, 19, 56).addIngredients(VanillaTypes.ITEM_STACK, this.toStacks(recipe.left()));
            builder.addSlot(RecipeIngredientRole.INPUT, 76, 56).addIngredients(VanillaTypes.ITEM_STACK, this.toStacks(recipe.right()));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).addIngredients(VanillaTypes.ITEM_STACK, outputs);
        }

        private List<ItemStack> toStacks(List<SizedIngredient> ingredients) {
            ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
            List<ItemStack> stacks = new ArrayList<>();
            for (SizedIngredient i : ingredients) {
                i.ingredient().display().resolveForStacks(ctx).forEach(stack -> stacks.add(stack.copyWithCount(i.count())));
            }
            return stacks;
        }
    }

    public static class BasicGemCuttingExtension implements GemCuttingExtension<BasicGemCuttingRecipe> {
        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, BasicGemCuttingRecipe recipe, IFocusGroup focuses) {
            ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
            builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredients(VanillaTypes.ITEM_STACK, recipe.base().display().resolveForStacks(ctx));
            builder.addSlot(RecipeIngredientRole.INPUT, 48, 4).addIngredients(VanillaTypes.ITEM_STACK, recipe.top().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
            builder.addSlot(RecipeIngredientRole.INPUT, 19, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.left().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
            builder.addSlot(RecipeIngredientRole.INPUT, 76, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.right().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).add(VanillaTypes.ITEM_STACK, recipe.output());
        }

        private static java.util.stream.Stream<ItemStack> sizedStacks(SizedIngredient si) {
            ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
            return si.ingredient().display().resolveForStacks(ctx).stream().map(s -> s.copyWithCount(si.count()));
        }
    }
}
