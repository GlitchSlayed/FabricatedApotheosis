package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import net.minecraft.world.level.block.Block;

@Mixin(Block.class)
public abstract class BlockMixin implements EnchantmentStatBlock {

}
