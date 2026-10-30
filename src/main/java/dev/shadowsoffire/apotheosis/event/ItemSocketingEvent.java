package dev.shadowsoffire.apotheosis.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ItemSocketingEvent extends Event {

    protected final ItemStack stack;
    protected final ItemStack gem;
    protected ItemStack output;

    public ItemSocketingEvent(ItemStack stack, ItemStack gem, ItemStack output) {
        this.stack = stack.copy();
        this.gem = gem.copy();
        this.output = output;
    }

    public ItemStack getInputStack() {
        return this.stack;
    }

    public ItemStack getInputGem() {
        return this.gem;
    }

    public ItemStack getOutput() {
        return this.output.copy();
    }

    public void setOutput(ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Setting an empty output is undefined behavior");
        }
        this.output = output.copy();
    }
}
