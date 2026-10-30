package dev.shadowsoffire.placebo.tabs;

import java.util.function.Supplier;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public interface ITabFiller {

    void fillItemCategory(CreativeModeTab tab, BuildCreativeModeTabContentsEvent event);

    static ITabFiller simple(ItemLike i) {
        return (tab, output) -> output.accept(i.asItem().getDefaultInstance());
    }

    static ITabFiller delegating(Supplier<? extends ItemLike> i) {
        return (tab, output) -> {
            Item item = i.get().asItem();
            if (item instanceof ITabFiller filler) {
                filler.fillItemCategory(tab, output);
            } else {
                output.accept(item.getDefaultInstance());
            }
        };
    }
}
