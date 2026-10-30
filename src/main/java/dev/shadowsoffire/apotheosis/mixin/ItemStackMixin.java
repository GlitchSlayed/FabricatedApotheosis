package dev.shadowsoffire.apotheosis.mixin;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiers;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

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
                boolean matches = false;
                for (var s : entry.slots().slots()) {
                    var vanillaSlot = EquipmentSlotCompat.toVanilla(s);
                    if (vanillaSlot != null && group.test(vanillaSlot)) {
                        matches = true;
                        break;
                    }
                }
                if (matches) {
                    action.accept(entry.attribute(), entry.modifier(), ItemAttributeModifiers.Display.attributeModifiers());
                }
            }
        }
    }
}
