package dev.shadowsoffire.apotheosis.socket;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SocketTooltipComponent implements TooltipComponent {

    private final ItemStack socketed;
    private final SocketedGems gems;

    public SocketTooltipComponent(ItemStack socketed, SocketedGems gems) {
        this.socketed = socketed;
        this.gems = gems;
    }

    public ItemStack socketed() {
        return this.socketed;
    }

    public SocketedGems gems() {
        return this.gems;
    }
}
