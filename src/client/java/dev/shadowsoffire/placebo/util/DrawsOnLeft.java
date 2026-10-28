package dev.shadowsoffire.placebo.util;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;

public interface DrawsOnLeft {

    default void drawOnLeft(GuiGraphicsExtractor gfx, List<? extends FormattedText> list, int y) {
        if (this instanceof AbstractContainerScreen<?> screen) {
            draw(screen, gfx, list, y);
        }
    }

    static void draw(AbstractContainerScreen<?> screen, GuiGraphicsExtractor gfx, List<? extends FormattedText> text, int y) {
        if (text == null || text.isEmpty()) return;
        Font font = screen.getFont();
        int maxW = 0;
        for (FormattedText c : text) {
            maxW = Math.max(maxW, font.width(c));
        }
        int x = screen.leftPos - maxW - 16;
        int h = text.size() * (font.lineHeight + 2) + 8;
        int w = maxW + 12;
        gfx.fill(x, y, x + w, y + h, 0x90000000);
        int curY = y + 4;
        for (FormattedText c : text) {
            gfx.text(font, Language.getInstance().getVisualOrder(c), x + 6, curY, 0xFFFFFFFF);
            curY += font.lineHeight + 2;
        }
    }
}
