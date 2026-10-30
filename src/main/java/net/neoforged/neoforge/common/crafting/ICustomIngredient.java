package net.neoforged.neoforge.common.crafting;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;

public interface ICustomIngredient extends CustomIngredient {

    @Override
    default boolean requiresTesting() {
        return true;
    }

    default boolean isSimple() {
        return false;
    }

    IngredientType<?> getType();

    @Override
    default net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer<?> getSerializer() {
        return getType();
    }
}
