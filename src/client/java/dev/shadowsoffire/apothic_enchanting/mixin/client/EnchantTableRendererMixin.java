package dev.shadowsoffire.apothic_enchanting.mixin.client;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apothic_enchanting.table.ApothicTableState;
import dev.shadowsoffire.apothic_enchanting.table.BookTexturedTable;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;

/**
 * Mixin which allows swapping out the book texture used by {@link EnchantTableRenderer}.
 * 
 * @see BookTexturedTable
 */
@Mixin(EnchantTableRenderer.class)
public class EnchantTableRendererMixin {

    @Shadow
    private BookModel bookModel;

    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 1)
    private void apoth_pickBookTexture(EnchantingTableBlockEntity blockEntity, EnchantTableRenderState state,
        float partialTicks, net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress,
        CallbackInfo ci) {
        if (blockEntity.getBlockState().getBlock() instanceof BookTexturedTable b) {
            ((ApothicTableState) state).apoth_setBookTexture(Sheets.BLOCK_ENTITIES_MAPPER.apply(b.getBookTextureId()));
        }
        else {
            ((ApothicTableState) state).apoth_setBookTexture(null);
        }
    }

    @ModifyArg(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;I)V"), index = 6, require = 1)
    private SpriteId apoth_replaceBookTexture(SpriteId original, @Local(argsOnly = true) EnchantTableRenderState state) {
        SpriteId override = ((ApothicTableState) state).apoth_getBookTexture();
        return override != null ? override : original;
    }

    @ModifyArg(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitCrumblingOverlay(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), index = 3, require = 0)
    private RenderType apoth_replaceCrumblingBookTexture(RenderType original, @Local(argsOnly = true) EnchantTableRenderState state) {
        SpriteId override = ((ApothicTableState) state).apoth_getBookTexture();
        if (override != null) {
            return override.renderType(this.bookModel.renderType());
        }
        return original;
    }

}
