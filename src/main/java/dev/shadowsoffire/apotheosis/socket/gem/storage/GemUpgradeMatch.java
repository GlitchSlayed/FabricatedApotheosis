package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.EnumMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record GemUpgradeMatch(PurityUpgradeRecipe recipe, int leftSlot, int rightSlot, SizedIngredient leftIng, SizedIngredient rightIng) {

    void execute(InternalItemHandler matInv, EnumMap<Purity, Integer> map, int maxCount) {
        int currentNext = map.getOrDefault(this.recipe.purity().next(), 0);
        if (currentNext >= maxCount) {
            return;
        }
        matInv.extractItem(this.leftSlot, this.leftIng.count(), false);
        matInv.extractItem(this.rightSlot, this.rightIng.count(), false);
        int currentPrev = map.getOrDefault(this.recipe.purity(), 0);
        map.put(this.recipe.purity(), Math.max(0, currentPrev - 2));
        long next = (long) currentNext + 1;
        map.put(this.recipe.purity().next(), (int) Math.min((long) maxCount, next));
    }

    @Nullable
    public static GemUpgradeMatch findMatch(Level level, Purity purity, EnumMap<Purity, Integer> map, InternalItemHandler matInv, int maxCount) {
        if (purity.ordinal() <= 0) {
            return null;
        }
        if (map.getOrDefault(purity, 0) >= maxCount) {
            return null;
        }
        Purity prev = Purity.values()[purity.ordinal() - 1];
        if (map.getOrDefault(prev, 0) < 2) {
            return null;
        }

        List<RecipeHolder<GemCuttingRecipe>> recipes = GemCuttingMenu.getRecipes(level);

        for (RecipeHolder<GemCuttingRecipe> holder : recipes) {
            if (holder.value() instanceof PurityUpgradeRecipe rec && rec.purity() == prev) {
                int leftSlot = -1, rightSlot = -1;
                SizedIngredient leftIng = null, rightIng = null;

                for (int i = 0; i < matInv.size(); i++) {
                    ItemStack stack = matInv.getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (leftIng == null) {
                        leftIng = GemCuttingRecipe.getMatch(stack, rec.left());
                        if (leftIng != null) {
                            leftSlot = i;
                            continue;
                        }
                    }

                    if (rightIng == null) {
                        rightIng = GemCuttingRecipe.getMatch(stack, rec.right());
                        if (rightIng != null) {
                            rightSlot = i;
                            continue;
                        }
                    }
                }

                if (leftIng == null || rightIng == null) {
                    continue;
                }

                return new GemUpgradeMatch(rec, leftSlot, rightSlot, leftIng, rightIng);
            }
        }

        return null;
    }
}
