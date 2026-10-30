package dev.shadowsoffire.apotheosis.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class GetItemSocketsEvent extends Event {

    protected final ItemStack stack;
    protected int sockets;

    public GetItemSocketsEvent(ItemStack stack, int sockets) {
        this.stack = stack;
        this.sockets = sockets;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public int getSockets() {
        return this.sockets;
    }

    public void setSockets(int sockets) {
        this.sockets = sockets;
    }
}
