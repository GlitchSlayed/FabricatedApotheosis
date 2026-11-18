package dev.shadowsoffire.apotheosis.client.mixin;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.client.attributes.AttributesClientTooltipHandler;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Enhances hovered status effect tooltips in {@link EffectsInInventory} with effect descriptions
 * and attribute modifier breakdowns.
 */
@Mixin(EffectsInInventory.class)
public class EffectsInInventoryMixin {

    @Unique
    @Nullable
    private MobEffectInstance apoth$currentEffect;

    @Inject(method = "getEffectName", at = @At("HEAD"))
    private void apoth$captureCurrentEffect(MobEffectInstance effect, CallbackInfoReturnable<Component> cir) {
        this.apoth$currentEffect = effect;
    }

    @Inject(method = "extractEffects", at = @At("RETURN"))
    private void apoth$clearCurrentEffect(GuiGraphicsExtractor gfx, Collection<MobEffectInstance> effects, int x, int height, int mouseX, int mouseY, int width, CallbackInfo ci) {
        this.apoth$currentEffect = null;
    }

    @ModifyArg(
        method = "extractText",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"
        ),
        index = 1
    )
    private List<Component> apoth$enrichEffectTooltip(List<Component> original) {
        if (this.apoth$currentEffect == null || !ApotheosisConfig.getClient().enablePotionTooltips) {
            return original;
        }
        return AttributesClientTooltipHandler.enrichEffectTooltip(this.apoth$currentEffect, original);
    }
}
