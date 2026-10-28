package dev.shadowsoffire.apotheosis.attributes.effects;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class VitalityEffect extends MobEffect {

    public VitalityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF5555);
        this.addAttributeModifier(ALObjects.Attributes.HEALING_RECEIVED, Identifier.fromNamespaceAndPath(ALObjects.MODID, "bursting_vitality"), 0.2, Operation.ADD_VALUE);
    }
}
