package dev.shadowsoffire.apotheosis.client.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.client.attributes.AttributesGui;
import dev.shadowsoffire.apotheosis.client.attributes.AttributesGuiHolder;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> implements AttributesGuiHolder {

    @Unique
    @Nullable
    private AttributesGui apoth_attributesGui;

    public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, null, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void apoth_initAttributesGui(CallbackInfo ci) {
        if (!ApotheosisConfig.getClient().enableAttributesGui) return;

        this.apoth_attributesGui = new AttributesGui((InventoryScreen) (Object) this);
        this.addRenderableOnly(this.apoth_attributesGui);
        this.addWidget(this.apoth_attributesGui);
        this.addRenderableWidget(this.apoth_attributesGui.getToggleBtn());
        this.addRenderableWidget(this.apoth_attributesGui.getHideUnchangedBtn());

        if (AttributesGui.wasOpen) {
            this.apoth_attributesGui.toggleVisibility();
        }
    }

    @Override
    @Nullable
    public AttributesGui apoth_getAttributesGui() {
        return this.apoth_attributesGui;
    }
}
