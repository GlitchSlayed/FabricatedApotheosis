package dev.shadowsoffire.apotheosis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.gui.Hud;

@Mixin(Hud.class)
public class HudMixin {

    /**
     * Extends the time action bar overlay messages stay on screen from 3 seconds to 8 seconds.
     */
    @ModifyConstant(method = "setOverlayMessage", constant = @Constant(intValue = 60))
    public int apoth_extendTime(int old) {
        return 160;
    }

}
