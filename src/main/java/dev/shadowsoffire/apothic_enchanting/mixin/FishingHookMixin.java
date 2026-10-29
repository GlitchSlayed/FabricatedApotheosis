package dev.shadowsoffire.apothic_enchanting.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import net.minecraft.world.entity.projectile.FishingHook;

/**
 * Replaces upstream coremod FishingHookLureTransformer by ensuring fishing delay
 * never drops below 1 tick even with high lure levels.
 */
@Mixin(FishingHook.class)
public class FishingHookMixin {

    @Shadow
    private int timeUntilLured;

    @Redirect(
        method = "catchingFish",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/projectile/FishingHook;timeUntilLured:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 2
        )
    )
    private void apoth_fishingLureDelay(FishingHook hook, int vanillaComputedValue) {
        this.timeUntilLured = EnchHooks.getTicksCaughtDelay(hook);
    }
}
