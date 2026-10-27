package dev.shadowsoffire.placebo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;

/**
 * Common gameplay and item utilities.
 */
public class PlaceboUtil {

    @SafeVarargs
    public static <T> List<T> asList(T... objs) {
        return new ArrayList<>(Arrays.asList(objs));
    }

    public static <T> List<T> toMutable(List<T> list) {
        return new ArrayList<>(list);
    }

    public static ItemStack makeStack(Object thing) {
        if (thing instanceof ItemStack stack) return stack;
        if (thing instanceof ItemLike item) return new ItemStack(item);
        if (thing instanceof Item item) return new ItemStack(item);
        throw new IllegalArgumentException("Cannot construct ItemStack from object of type: " + (thing == null ? "null" : thing.getClass()));
    }

    public static void addLore(ItemStack stack, Component lore) {
        ItemLore existing = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        List<Component> lines = new ArrayList<>(existing.lines());
        lines.add(lore);
        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    public static boolean tryHarvestBlock(ServerPlayer player, BlockPos pos) {
        return player.gameMode.destroyBlock(pos);
    }
}
