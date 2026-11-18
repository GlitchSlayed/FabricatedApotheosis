package dev.shadowsoffire.apotheosis.util;

import java.util.Objects;
import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.apotheosis.attributes.format.AttributeFormatting;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.mobs.util.BossStats;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;

public class CommonTooltipUtil {

    public static void appendBossData(Level level, LivingEntity entity, Consumer<Component> tooltip) {
        String rarityStr = AttributesUtil.getPersistentData(entity).getStringOr(Invader.RARITY_KEY, "");
        Identifier rarityId = rarityStr.isEmpty() ? null : Identifier.tryParse(rarityStr);
        if (rarityId == null) {
            return;
        }
        DynamicHolder<LootRarity> rarity = RarityRegistry.INSTANCE.holder(rarityId);
        if (!rarity.isBound()) {
            return;
        }
        tooltip.accept(Component.translatable("info.apotheosis.boss", rarity.get().toComponent()).withStyle(ChatFormatting.GRAY));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            tooltip.accept(CommonComponents.EMPTY);
            tooltip.accept(Component.translatable("info.apotheosis.boss_modifiers").withStyle(ChatFormatting.GRAY));
            AttributeMap map = entity.getAttributes();
            BuiltInRegistries.ATTRIBUTE.listElements().map(map::getInstance).filter(Objects::nonNull).forEach(inst -> {
                for (AttributeModifier modif : inst.getModifiers()) {
                    if (modif.id().getPath().startsWith(BossStats.MODIFIER_BASE.getPath())) {
                        tooltip.accept(AttributeFormatting.toComponent(inst.getAttribute(), modif, ApothicAttributes.getTooltipFlag()));
                    }
                }
            });
        }
    }

}
