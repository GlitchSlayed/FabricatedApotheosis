package dev.shadowsoffire.apotheosis.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

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

    @WrapOperation(
        method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private static boolean apoth_telepathicBlockDrops(Level level, Entity entity, Operation<Boolean> original, Level pLevel, Supplier<ItemEntity> supplier, ItemStack stack) {
        ServerPlayer breaker = AttributesUtil.CURRENT_BREAKER.get();
        if (breaker != null && entity instanceof ItemEntity item && !breaker.position().equals(Vec3.ZERO)) {
            ItemStack tool = breaker.getMainHandItem();
            if (!tool.isEmpty() && AffixHelper.streamAffixes(tool).anyMatch(AffixInstance::enablesTelepathy)) {
                item.setPos(breaker.getX(), breaker.getY(), breaker.getZ());
                item.setNoPickUpDelay();
            }
        }
        return original.call(level, entity);
    }
}
