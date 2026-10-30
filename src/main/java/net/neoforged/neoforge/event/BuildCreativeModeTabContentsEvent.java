package net.neoforged.neoforge.event;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class BuildCreativeModeTabContentsEvent implements CreativeModeTab.Output {

    private final CreativeModeTab.Output output;

    public BuildCreativeModeTabContentsEvent(CreativeModeTab.Output output) {
        this.output = output;
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        if (this.output != null) {
            this.output.accept(stack, visibility);
        }
    }
}
