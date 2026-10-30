package dev.shadowsoffire.apotheosis.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import dev.shadowsoffire.placebo.util.DrawsOnLeft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base class implementing common functionality for dark-mode menus used in the Adventure Module.
 */
public abstract class AdventureContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements DrawsOnLeft {

    private static final Identifier SLOT_HIGHLIGHT_BACK = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT = Identifier.withDefaultNamespace("container/slot_highlight_front");
    private static final int DARK_HIGHLIGHT_TINT = 0x40FFFFFF;

    public AdventureContainerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    public AdventureContainerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, int imageWidth, int imageHeight) {
        super(pMenu, pPlayerInventory, pTitle, imageWidth, imageHeight);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {}

    public int getLeftPos() {
        return this.leftPos;
    }

    public int getTopPos() {
        return this.topPos;
    }

    @Override
    protected void extractSlotHighlightBack(GuiGraphicsExtractor gfx) {
        this.drawTintedHighlight(gfx, SLOT_HIGHLIGHT_BACK);
    }

    @Override
    protected void extractSlotHighlightFront(GuiGraphicsExtractor gfx) {
        this.drawTintedHighlight(gfx, SLOT_HIGHLIGHT_FRONT);
    }

    private void drawTintedHighlight(GuiGraphicsExtractor gfx, Identifier sprite) {
        if (this.hoveredSlot != null && this.hoveredSlot.isHighlightable()) {
            RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
            gfx.blitSprite(pipeline, sprite, this.hoveredSlot.x - 4, this.hoveredSlot.y - 4, 24, 24, DARK_HIGHLIGHT_TINT);
        }
    }

}
