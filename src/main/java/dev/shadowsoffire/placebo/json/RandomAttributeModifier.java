package dev.shadowsoffire.placebo.json;

import java.util.concurrent.atomic.AtomicLong;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * A Random Attribute Modifier is a data-driven template for creating {@link AttributeModifier}s.
 */
public record RandomAttributeModifier(Holder<Attribute> attribute, Operation operation, StepFunction value, Identifier modifierId, boolean generated) {

    private static final AtomicLong GEN_COUNTER = new AtomicLong();

    public static Codec<RandomAttributeModifier> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RandomAttributeModifier::attribute),
            Operation.CODEC.fieldOf("operation").forGetter(RandomAttributeModifier::operation),
            StepFunction.CODEC.fieldOf("value").forGetter(RandomAttributeModifier::value),
            Identifier.CODEC.fieldOf("modifier_id").forGetter(RandomAttributeModifier::modifierId))
        .apply(inst, RandomAttributeModifier::new));

    public static Codec<RandomAttributeModifier> CONSTANT_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RandomAttributeModifier::attribute),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(RandomAttributeModifier::operation),
            StepFunction.CONSTANT_CODEC.fieldOf("value").forGetter(RandomAttributeModifier::value),
            Identifier.CODEC.fieldOf("modifier_id").forGetter(RandomAttributeModifier::modifierId))
        .apply(inst, RandomAttributeModifier::new));

    public RandomAttributeModifier(Holder<Attribute> attribute, Operation operation, StepFunction value, Identifier modifierId) {
        this(attribute, operation, value, modifierId, false);
    }

    public static RandomAttributeModifier generated(Holder<Attribute> attribute, Operation operation, StepFunction value, Identifier base) {
        return new RandomAttributeModifier(attribute, operation, value, base, true);
    }

    public static Codec<RandomAttributeModifier> generatedCodec(Identifier base) {
        return RecordCodecBuilder.create(inst -> inst
            .group(
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RandomAttributeModifier::attribute),
                Operation.CODEC.fieldOf("operation").forGetter(RandomAttributeModifier::operation),
                StepFunction.CODEC.fieldOf("value").forGetter(RandomAttributeModifier::value))
            .apply(inst, (attribute, operation, value) -> generated(attribute, operation, value, base)));
    }

    public static Codec<RandomAttributeModifier> constantGeneratedCodec(Identifier base) {
        return RecordCodecBuilder.create(inst -> inst
            .group(
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RandomAttributeModifier::attribute),
                PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(RandomAttributeModifier::operation),
                StepFunction.CONSTANT_CODEC.fieldOf("value").forGetter(RandomAttributeModifier::value))
            .apply(inst, (attribute, operation, value) -> generated(attribute, operation, value, base)));
    }

    public AttributeModifier create(Identifier id, RandomSource rand) {
        return new AttributeModifier(id, this.value.get(rand.nextFloat()), this.operation);
    }

    public AttributeModifier create(RandomSource rand) {
        return create(this.effectiveId(), rand);
    }

    public AttributeModifier createDeterministic(Identifier id) {
        return new AttributeModifier(id, this.value.min(), this.operation);
    }

    public AttributeModifier createDeterministic() {
        return createDeterministic(this.effectiveId());
    }

    public void apply(Identifier id, RandomSource rand, LivingEntity entity) {
        if (entity == null) {
            throw new RuntimeException("Attempted to apply a random attribute modifier to a null entity!");
        }
        AttributeModifier modif = this.create(id, rand);
        AttributeInstance inst = entity.getAttribute(this.attribute);
        if (inst == null) {
            Placebo.LOGGER
                .trace(String.format("Attempted to apply a random attribute modifier to an entity (%s) that does not have that attribute (%s)!", EntityType.getKey(entity.getType()), this.attribute.unwrapKey().get()));
            return;
        }
        inst.addPermanentModifier(modif);
    }

    public void apply(RandomSource rand, LivingEntity entity) {
        this.apply(this.effectiveId(), rand, entity);
    }

    private Identifier effectiveId() {
        return this.generated ? this.modifierId.withSuffix("_generated_" + GEN_COUNTER.getAndIncrement()) : this.modifierId;
    }

}
