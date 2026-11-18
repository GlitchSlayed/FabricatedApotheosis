package dev.shadowsoffire.apotheosis.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {

    @Inject(method = "onLand", at = @At("TAIL"))
    private void apoth_gemSmashing(Level level, BlockPos pos, BlockState state, BlockState replaceableState, net.minecraft.world.entity.item.FallingBlockEntity fallingBlock, CallbackInfo ci) {
        var cfg = ApotheosisConfig.getServer();
        if (level.isClientSide() || !cfg.enableAdventure || !cfg.enableGemSmashing) {
            return;
        }
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos));
        for (ItemEntity ent : items) {
            ItemStack stack = ent.getItem();
            if (stack.is(Apoth.Items.GEM)) {
                ent.setItem(new ItemStack(Apoth.Items.GEM_DUST.value(), stack.getCount()));
            }
        }
    }
}
