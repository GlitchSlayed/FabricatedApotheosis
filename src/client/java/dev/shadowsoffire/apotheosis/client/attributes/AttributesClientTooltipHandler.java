package dev.shadowsoffire.apotheosis.client.attributes;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import dev.shadowsoffire.apotheosis.attributes.api.AttributeHelper;
import dev.shadowsoffire.apotheosis.attributes.format.AttributeFormatting;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * Handles tooltip enhancements on the Fabric client, including potion effect descriptions
 * and automatic migration of obsolete attribute identifiers in item tooltips.
 */
public class AttributesClientTooltipHandler {

    public static void init() {
        ItemTooltipCallback.EVENT.register(AttributesClientTooltipHandler::onItemTooltip);
    }

    private static void onItemTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> lines) {
        if (stack.isEmpty()) return;

        // Migrate any legacy/obsolete attributes (e.g. apothic_attributes:mining_speed -> minecraft:mining_efficiency)
        AttributeHelper.migrateItemModifiers(stack);

        // Potion descriptions if enabled
        if (ApotheosisConfig.getClient().enablePotionTooltips && stack.getItem() instanceof PotionItem) {
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            List<MobEffectInstance> effects = contents.customEffects();
            if (effects.size() == 1 && lines.size() >= 2) {
                MobEffect effect = effects.get(0).getEffect().value();
                String key = effect.getDescriptionId() + ".desc";
                if (Language.getInstance().has(key)) {
                    lines.add(2, Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
                } else if (flag.isAdvanced()) {
                    lines.add(2, Component.literal(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
            }
        }
    }

    /**
     * Enriches inventory status-effect tooltips with duration formatting, localization descriptions,
     * and active attribute modifier lines matching upstream Apothic Attributes behavior.
     */
    public static List<Component> enrichEffectTooltip(MobEffectInstance effectInst, List<Component> original) {
        if (effectInst == null || original == null || original.size() < 2) {
            return original;
        }

        List<Component> tooltips = new ArrayList<>(original);
        Holder<MobEffect> effect = effectInst.getEffect();

        MutableComponent name = tooltips.get(0).copy();
        Component duration = tooltips.remove(1);
        duration = Component.translatable("(%s)", duration).withStyle(ChatFormatting.WHITE);
        name.append(" ").append(duration);

        Minecraft mc = Minecraft.getInstance();
        TooltipFlag flag = mc != null && mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        if (flag.isAdvanced()) {
            effect.unwrapKey().ifPresent(k ->
                name.append(" ").append(Component.translatable("[%s]", k.identifier().toString()).withStyle(ChatFormatting.GRAY)));
        }
        tooltips.set(0, name);

        List<Pair<Holder<Attribute>, AttributeModifier>> modifiers = new ArrayList<>();
        effect.value().createModifiers(effectInst.getAmplifier(), (attr, modif) -> modifiers.add(new Pair<>(attr, modif)));

        String key = effect.value().getDescriptionId() + ".desc";
        if (Language.getInstance().has(key)) {
            tooltips.add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
        } else if (flag.isAdvanced() && modifiers.isEmpty()) {
            tooltips.add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        for (Pair<Holder<Attribute>, AttributeModifier> pair : modifiers) {
            tooltips.add(AttributeFormatting.toComponent(pair.getFirst(), pair.getSecond(), flag));
        }

        return tooltips;
    }
}
