package dev.shadowsoffire.apotheosis.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class CanSocketGemEvent extends Event implements ICancellableEvent {

    protected final ItemStack stack;
    protected final ItemStack gem;

    public CanSocketGemEvent(ItemStack stack, ItemStack gem) {
        this.stack = stack.copy();
        this.gem = gem.copy();
    }

    public ItemStack getInputStack() {
        return this.stack;
    }

    public ItemStack getInputGem() {
        return this.gem;
    }
}
