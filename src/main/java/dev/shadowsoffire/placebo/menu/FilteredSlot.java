package dev.shadowsoffire.placebo.menu;

import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FilteredSlot extends Slot {

    protected final Predicate<ItemStack> filter;
    protected int maxStack = -1;

    public FilteredSlot(Container container, int index, int x, int y, Predicate<ItemStack> filter) {
        super(container, index, x, y);
        this.filter = filter != null ? filter : stack -> true;
    }

    public FilteredSlot(Container container, int index, int x, int y) {
        this(container, index, x, y, stack -> true);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.filter.test(stack);
    }

    public FilteredSlot setMaxStackSize(int max) {
        this.maxStack = max;
        return this;
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStack > 0 ? this.maxStack : super.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return this.maxStack > 0 ? Math.min(this.maxStack, stack.getMaxStackSize()) : super.getMaxStackSize(stack);
    }
}
