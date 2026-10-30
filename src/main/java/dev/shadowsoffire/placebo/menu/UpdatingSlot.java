package dev.shadowsoffire.placebo.menu;

import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class UpdatingSlot extends FilteredSlot {

    protected final Runnable callback;

    public UpdatingSlot(Container container, int index, int x, int y, Predicate<ItemStack> filter, Runnable callback) {
        super(container, index, x, y, filter);
        this.callback = callback;
    }

    public UpdatingSlot(Container container, int index, int x, int y, Predicate<ItemStack> filter) {
        this(container, index, x, y, filter, null);
    }

    public UpdatingSlot(Container container, int index, int x, int y) {
        this(container, index, x, y, stack -> true, null);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.callback != null) {
            this.callback.run();
        }
    }
}
