package dev.shadowsoffire.apotheosis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.client.attributes.AttributesGui;
import dev.shadowsoffire.apotheosis.client.attributes.AttributesGuiHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void apoth_compareHoveredEquipment(GuiGraphicsExtractor gfx, int mouseX, int mouseY, CallbackInfo ci) {
        if (AdventureModuleClient.tryCompareItems((AbstractContainerScreen<?>) (Object) this, this.hoveredSlot, gfx, mouseX, mouseY)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTooltip", at = @At("TAIL"))
    private void apoth_renderCanSocketTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY, CallbackInfo ci) {
        AdventureModuleClient.renderCanSocketTooltip((AbstractContainerScreen<?>) (Object) this, this.hoveredSlot, gfx, mouseX, mouseY);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void apoth_onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof AttributesGuiHolder holder) {
            AttributesGui gui = holder.apoth_getAttributesGui();
            if (gui != null && gui.isOpen() && gui.isMouseOver(mouseX, mouseY)) {
                if (gui.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void apoth_onMouseDragged(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof AttributesGuiHolder holder) {
            AttributesGui gui = holder.apoth_getAttributesGui();
            if (gui != null && gui.isOpen()) {
                if (gui.mouseDragged(event, dx, dy)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
