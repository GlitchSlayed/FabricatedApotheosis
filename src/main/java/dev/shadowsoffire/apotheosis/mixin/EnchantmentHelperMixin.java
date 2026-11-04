package dev.shadowsoffire.apotheosis.mixin;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Unique
    private static final ThreadLocal<Boolean> apoth$reentrantEnchLock = ThreadLocal.withInitial(() -> false);

    @Inject(at = @At("RETURN"), method = "getDamageProtection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)F", cancellable = true)
    private static void apoth_getDamageProtection(ServerLevel level, LivingEntity entity, DamageSource source, CallbackInfoReturnable<Float> cir) {
        float prot = cir.getReturnValueF();
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            ItemStack s = entity.getItemBySlot(slot);
            prot += SocketHelper.getGems(s).getDamageProtection(source);

            var affixes = AffixHelper.getAffixes(s);
            for (AffixInstance inst : affixes.values()) {
                prot += inst.getDamageProtection(source);
            }
        }
        cir.setReturnValue(prot);
    }

    @Inject(at = @At("RETURN"), method = "modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F", cancellable = true)
    private static void apoth_modifyDamage(ServerLevel level, ItemStack tool, Entity entity, DamageSource damageSource, float damage, CallbackInfoReturnable<Float> cir) {
        float dmg = cir.getReturnValueF();

        dmg += SocketHelper.getGems(tool).getDamageBonus(entity);

        var affixes = AffixHelper.getAffixes(tool);
        for (AffixInstance inst : affixes.values()) {
            dmg += inst.getDamageBonus(entity);
        }

        cir.setReturnValue(dmg);
    }

    @Inject(at = @At("TAIL"), method = "doPostAttackEffectsWithItemSource(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/item/ItemStack;)V")
    private static void apoth_doPostAttackEffectsWithItemSource(ServerLevel level, Entity target, DamageSource damageSource, @Nullable ItemStack itemSource, CallbackInfo ci) {
        if (damageSource.getEntity() instanceof LivingEntity user) {
            ItemStack attackingItem = (itemSource != null && !itemSource.isEmpty()) ? itemSource : user.getMainHandItem();
            if (!attackingItem.isEmpty()) {
                SocketHelper.getGems(attackingItem).doPostAttack(user, target);

                var affixes = AffixHelper.getAffixes(attackingItem);
                for (AffixInstance inst : affixes.values()) {
                    int old = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    try {
                        inst.doPostAttack(user, target);
                    } finally {
                        target.invulnerableTime = old;
                    }
                }
            }
        }

        if (target instanceof LivingEntity livingTarget) {
            for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                ItemStack s = livingTarget.getItemBySlot(slot);
                SocketHelper.getGems(s).doPostHurt(livingTarget, damageSource);

                var affixes = AffixHelper.getAffixes(s);
                for (AffixInstance inst : affixes.values()) {
                    inst.doPostHurt(livingTarget, damageSource);
                }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I", cancellable = true)
    private static void apoth_processAffixDurability(ServerLevel level, ItemStack stack, int damage, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() <= 0) {
            return;
        }

        int amount = cir.getReturnValueI();
        double chance = stack.getOrDefault(Components.DURABILITY_BONUS, 0F);

        if (stack.has(Components.SOCKETED_GEMS)) {
            double socketBonus = SocketHelper.getGems(stack).getDurabilityBonusPercentage().reduce(0, ApothMiscUtil::duraProd);
            chance = ApothMiscUtil.duraProd(chance, socketBonus);
        }

        if (stack.has(Components.AFFIXES)) {
            double afxBonus = AffixHelper.streamAffixes(stack).mapToDouble(AffixInstance::getDurabilityBonusPercentage).reduce(0, ApothMiscUtil::duraProd);
            chance = ApothMiscUtil.duraProd(chance, afxBonus);
        }

        int delta = 1;
        int blocked = 0;

        if (chance < 0) {
            delta = -1;
            chance = -chance;
        }

        if (chance > 0) {
            for (int i = 0; i < amount; i++) {
                if (level.getRandom().nextFloat() <= chance) {
                    blocked += delta;
                }
            }
        }

        cir.setReturnValue(amount - blocked);
    }

    @Inject(at = @At("RETURN"), method = "getItemEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemInstance;)I", cancellable = true)
    private static void apoth_getItemEnchantmentLevel(Holder<Enchantment> ench, ItemInstance itemInst, CallbackInfoReturnable<Integer> cir) {
        if (apoth$reentrantEnchLock.get()) {
            return;
        }
        if (itemInst instanceof ItemStack stack && !stack.isEmpty()) {
            apoth$reentrantEnchLock.set(true);
            try {
                int baseLevel = cir.getReturnValueI();
                net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                if (baseLevel > 0) {
                    mutable.upgrade(ench, baseLevel);
                }
                GetEnchantmentLevelEvent event = new GetEnchantmentLevelEvent(stack, mutable);
                SocketHelper.getGems(stack).getEnchantmentLevels(event);
                AffixHelper.streamAffixes(stack).forEach(inst -> inst.getEnchantmentLevels(event));
                cir.setReturnValue(event.getEnchantments().getLevel(ench));
            } finally {
                apoth$reentrantEnchLock.set(false);
            }
        }
    }
}
