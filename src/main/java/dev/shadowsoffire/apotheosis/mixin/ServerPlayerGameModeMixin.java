package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void apoth_setBreaker(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        AttributesUtil.CURRENT_BREAKER.set(this.player);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void apoth_clearBreaker(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        AttributesUtil.CURRENT_BREAKER.remove();
    }
}
