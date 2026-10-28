package dev.shadowsoffire.apotheosis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("leftPos")
    int apoth_getLeftPos();

    @Accessor("leftPos")
    void apoth_setLeftPos(int leftPos);

    @Accessor("topPos")
    int apoth_getTopPos();

    @Accessor("imageWidth")
    int apoth_getImageWidth();

    @Accessor("imageHeight")
    int apoth_getImageHeight();
}
