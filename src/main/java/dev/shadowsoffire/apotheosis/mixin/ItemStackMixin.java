package dev.shadowsoffire.apotheosis.mixin;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketTooltipComponent;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiers;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    public void apoth_affixItemName(CallbackInfoReturnable<Component> cir) {
        ItemStack ths = (ItemStack) (Object) this;
        Component afxName = AffixHelper.getModifiedStackName(ths, cir.getReturnValue());
        if (afxName != null) {
            cir.setReturnValue(afxName);
        }

        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(ths);
        if (rarity.isBound()) {
            Component recolored = cir.getReturnValue().copy().withStyle(s -> s.withColor(rarity.get().color()));
            cir.setReturnValue(recolored);
        }
    }

    @Inject(method = "getStyledHoverName", at = @At("RETURN"), cancellable = true)
    public void apoth_affixStyledHoverName(CallbackInfoReturnable<Component> cir) {
        ItemStack ths = (ItemStack) (Object) this;
        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(ths);
        if (rarity.isBound()) {
            Component recolored = cir.getReturnValue().copy().withStyle(s -> s.withColor(rarity.get().color()));
            cir.setReturnValue(recolored);
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void apoth_socketTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        ItemStack ths = (ItemStack) (Object) this;
        Player player = ApothMiscUtil.getClientPlayer();
        if (SocketHelper.getSockets(ths) > 0 && (player == null || !WorldTier.isTutorialActive(player))) {
            cir.setReturnValue(Optional.of(new SocketTooltipComponent(ths, SocketHelper.getGems(ths))));
        }
    }

    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    public void apoth_useItemOnBlockPost(net.minecraft.world.item.context.UseOnContext ctx, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) {
            ItemStack s = (ItemStack) (Object) this;
            net.minecraft.world.InteractionResult socketRes = SocketHelper.getGems(s).onItemUse(ctx);
            if (socketRes != null) {
                cir.setReturnValue(socketRes);
                return;
            }

            net.minecraft.world.InteractionResult afxRes = AffixHelper.streamAffixes(s)
                .map(afx -> afx.onItemUse(ctx))
                .filter(com.google.common.base.Predicates.notNull())
                .findFirst()
                .orElse(null);
            if (afxRes != null) {
                cir.setReturnValue(afxRes);
            }
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void apoth_tryTickMalice(Level level, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack ths = (ItemStack) (Object) this;
        if (!level.isClientSide() && ths.has(Apoth.Components.MALICE_MARKER) && entity instanceof Player player) {
            AffixHelper.applyMalice(player, ths);
            ths.remove(Apoth.Components.MALICE_MARKER);
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void apoth_forEachModifier(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        ItemStack ths = (ItemStack) (Object) this;
        if (!SocketHelper.getGems(ths).isEmpty() || AffixHelper.hasAffixes(ths)) {
            var event = new StackAttributeModifiersEvent(ths, StackAttributeModifiers.EMPTY);
            SocketHelper.getGems(ths).addModifiers(event);
            AffixHelper.streamAffixes(ths).forEach(inst -> inst.addModifiers(event));
            var apothSlot = EquipmentSlotCompat.fromVanilla(slot);
            if (apothSlot != null) {
                for (var entry : event.getModifiers()) {
                    if (entry.slots().test(apothSlot)) {
                        action.accept(entry.attribute(), entry.modifier());
                    }
                }
            }
        }
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("TAIL"))
    private void apoth_forEachModifierGroup(EquipmentSlotGroup group, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> action, CallbackInfo ci) {
        ItemStack ths = (ItemStack) (Object) this;
        if (!SocketHelper.getGems(ths).isEmpty() || AffixHelper.hasAffixes(ths)) {
            var event = new StackAttributeModifiersEvent(ths, StackAttributeModifiers.EMPTY);
            SocketHelper.getGems(ths).addModifiers(event);
            AffixHelper.streamAffixes(ths).forEach(inst -> inst.addModifiers(event));
            for (var entry : event.getModifiers()) {
                EquipmentSlotGroup vanillaGroup = EquipmentSlotCompat.toVanilla(entry.slots());
                if (vanillaGroup == group || (vanillaGroup == null && group == EquipmentSlotGroup.ANY)) {
                    action.accept(entry.attribute(), entry.modifier(), ItemAttributeModifiers.Display.attributeModifiers());
                }
            }
        }
    }
}
