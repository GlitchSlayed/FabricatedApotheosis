package dev.shadowsoffire.apothic_enchanting.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AnvilUpdateEvent {

    private final ItemStack left;
    private final ItemStack right;
    private final String name;
    private final Player player;
    private ItemStack output = ItemStack.EMPTY;
    private int xpCost;
    private int materialCost;
    private boolean canceled = false;

    public AnvilUpdateEvent(ItemStack left, ItemStack right, String name, int xpCost, Player player) {
        this.left = left;
        this.right = right;
        this.name = name;
        this.xpCost = xpCost;
        this.player = player;
    }

    public ItemStack getLeft() {
        return this.left;
    }

    public ItemStack getRight() {
        return this.right;
    }

    public String getName() {
        return this.name;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public void setOutput(ItemStack output) {
        this.output = output;
    }

    public int getXpCost() {
        return this.xpCost;
    }

    public void setXpCost(int xpCost) {
        this.xpCost = xpCost;
    }

    public int getMaterialCost() {
        return this.materialCost;
    }

    public void setMaterialCost(int materialCost) {
        this.materialCost = materialCost;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
