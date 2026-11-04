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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerPlayer player;

    @Unique
    private static final ThreadLocal<BlockState> apoth$DESTROYED_STATE = new ThreadLocal<>();

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void apoth_setBreaker(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        AttributesUtil.CURRENT_BREAKER.set(this.player);
        apoth$DESTROYED_STATE.set(this.player.level().getBlockState(pos));
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void apoth_clearBreaker(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        AttributesUtil.CURRENT_BREAKER.remove();
        BlockState preState = apoth$DESTROYED_STATE.get();
        apoth$DESTROYED_STATE.remove();
        if (cir.getReturnValue() && preState != null) {
            ItemStack stack = this.player.getMainHandItem();
            if (!stack.isEmpty()) {
                dev.shadowsoffire.apotheosis.socket.SocketHelper.getGems(stack).onBlockBreak(this.player, this.player.level(), pos, preState);
                dev.shadowsoffire.apotheosis.affix.AffixHelper.streamAffixes(stack).forEach(inst -> inst.onBlockBreak(this.player, this.player.level(), pos, preState));
            }
        }
    }
}
