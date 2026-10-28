package dev.shadowsoffire.apothic_enchanting.table.infusion;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Cached list of {@link InfusionRecipe} instances, refreshed whenever the server finishes a reload
 * or the client receives the recipe map from the server.
 */
public final class InfusionRecipeCache {

    private static volatile List<InfusionRecipe> RECIPES = List.of();

    private InfusionRecipeCache() {}

    /** Called from {@code ApothEnchEvents} on server start and data pack sync. */
    public static void rebuildFromServer(MinecraftServer server) {
        rebuildFromRecipes(server.getRecipeManager().getRecipes());
    }

    /** Called from client / server recipe sync. */
    public static void rebuildFromRecipes(Collection<RecipeHolder<?>> recipes) {
        RECIPES = recipes.stream()
            .map(RecipeHolder::value)
            .filter(r -> r instanceof InfusionRecipe)
            .map(r -> (InfusionRecipe) r)
            .toList();
    }

    public static void clear() {
        RECIPES = List.of();
    }

    public static List<InfusionRecipe> all() {
        return RECIPES;
    }

    @Nullable
    public static InfusionRecipe findMatch(ItemStack input, float eterna, float quanta, float arcana) {
        return RECIPES.stream()
            .filter(r -> r.matches(input, eterna, quanta, arcana))
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public static InfusionRecipe findItemMatch(ItemStack input) {
        return RECIPES.stream()
            .filter(r -> r.matches(input))
            .findFirst()
            .orElse(null);
    }
}
