package dev.shadowsoffire.apotheosis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apotheosis.client.ApothRenderStateHolder;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

    @ModifyArg(
        method = "submitBlitFromItemAtlas",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2fc;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
        ),
        index = 11
    )
    private int apoth_tintGhostOrGrayItem(int color, @Local(argsOnly = true) GuiItemRenderState itemState) {
        Float alpha = ((ApothRenderStateHolder) itemState.itemStackRenderState()).apoth$getRenderAlpha();
        if (alpha == null) {
            return color;
        }
        if (Float.isNaN(alpha)) {
            return 0xFF555555;
        }
        int a = Math.clamp(Math.round(alpha * 255.0F), 0, 255);
        return (a << 24) | (a << 16) | (a << 8) | a;
    }
}
