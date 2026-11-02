package dev.shadowsoffire.apotheosis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.client.AffixItemEffectRenderer;
import dev.shadowsoffire.apotheosis.client.AffixItemRenderState;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    private void apoth$extractAffixRenderState(ItemEntity entity, ItemEntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (!AdventureConfig.enableAffixItemEffects) {
            ((AffixItemRenderState) state).apoth$setRarityRenderData(null);
            return;
        }

        ItemStack stack = entity.getItem();
        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
        if (rarityHolder.isBound()) {
            LootRarity rarity = rarityHolder.get();
            AffixItemRenderState airs = (AffixItemRenderState) state;
            airs.apoth$setRarityRenderData(rarity.renderData());
            airs.apoth$setColor(rarity.color().getValue());
            airs.apoth$setOnGround(entity.onGround());
        }
        else {
            ((AffixItemRenderState) state).apoth$setRarityRenderData(null);
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    private void apoth$renderAffixEffects(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        AffixItemEffectRenderer.renderEffects(state, poseStack, collector, camera);
    }
}
