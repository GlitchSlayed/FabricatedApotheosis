package dev.shadowsoffire.apotheosis.attributes.effects;

import java.util.function.BiConsumer;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class KnowledgeEffect extends MobEffect {

    public static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath(ALObjects.MODID, "ancient_knowledge");
    public static final double DEFAULT_KNOWLEDGE_MULT = 4.0D;

    public KnowledgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4EE42);
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        double multiplier = DEFAULT_KNOWLEDGE_MULT * (amplifier + 1) - 1.0D;
        consumer.accept(ALObjects.Attributes.EXPERIENCE_GAINED, new AttributeModifier(MODIFIER_ID, multiplier, Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void addAttributeModifiers(AttributeMap map, int amplifier) {
        var inst = map.getInstance(ALObjects.Attributes.EXPERIENCE_GAINED);
        if (inst != null) {
            inst.removeModifier(MODIFIER_ID);
            double multiplier = DEFAULT_KNOWLEDGE_MULT * (amplifier + 1) - 1.0D;
            inst.addPermanentModifier(new AttributeModifier(MODIFIER_ID, multiplier, Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap map) {
        var inst = map.getInstance(ALObjects.Attributes.EXPERIENCE_GAINED);
        if (inst != null) {
            inst.removeModifier(MODIFIER_ID);
        }
    }
}
