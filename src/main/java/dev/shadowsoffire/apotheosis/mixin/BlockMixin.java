package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

@Mixin(Block.class)
public abstract class BlockMixin {

    @ModifyVariable(method = "popExperience", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int apoth_scaleBlockExperience(int amount) {
        ServerPlayer breaker = AttributesUtil.CURRENT_BREAKER.get();
        if (breaker != null) {
            double mult = breaker.getAttributeValue(ALObjects.Attributes.EXPERIENCE_GAINED);
            return (int) (amount * mult);
        }
        return amount;
    }
}
