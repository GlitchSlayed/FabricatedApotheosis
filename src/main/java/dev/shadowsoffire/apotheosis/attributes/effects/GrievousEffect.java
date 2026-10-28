package dev.shadowsoffire.apotheosis.attributes.effects;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class GrievousEffect extends MobEffect {

    public GrievousEffect() {
        super(MobEffectCategory.HARMFUL, 0xAA0000);
        this.addAttributeModifier(ALObjects.Attributes.HEALING_RECEIVED, Identifier.fromNamespaceAndPath(ALObjects.MODID, "grievous_wounds"), -0.4, Operation.ADD_VALUE);
    }
}
