package dev.shadowsoffire.placebo.util;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Interpolation function producing stepped numeric values between min and max.
 */
public record StepFunction(float min, int steps, float step, float max) {

    public static final Codec<StepFunction> STRICT_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("min").forGetter(StepFunction::min),
            Codec.INT.fieldOf("steps").forGetter(StepFunction::steps),
            Codec.FLOAT.fieldOf("step").forGetter(StepFunction::step))
        .apply(inst, StepFunction::new));

    public static final Codec<StepFunction> BOUNDS_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("min").forGetter(StepFunction::min),
            Codec.FLOAT.fieldOf("max").forGetter(StepFunction::max),
            Codec.FLOAT.optionalFieldOf("step", 0.01F).forGetter(StepFunction::step))
        .apply(inst, StepFunction::fromBounds));

    public static final Codec<StepFunction> CONSTANT_CODEC = Codec.FLOAT.xmap(StepFunction::constant, StepFunction::min);

    public static final Codec<StepFunction> TRANSITION_CODEC = Codec.either(STRICT_CODEC, BOUNDS_CODEC).xmap(Either::unwrap, Either::right);

    public static final Codec<StepFunction> CODEC = Codec.either(CONSTANT_CODEC, TRANSITION_CODEC).xmap(Either::unwrap, StepFunction::toEither);

    public static final StreamCodec<ByteBuf, StepFunction> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, StepFunction::min,
        ByteBufCodecs.INT, StepFunction::steps,
        ByteBufCodecs.FLOAT, StepFunction::step,
        ByteBufCodecs.FLOAT, StepFunction::max,
        StepFunction::new);

    public StepFunction(float min, int steps, float step) {
        this(min, steps, step, min + steps * step);
    }

    public StepFunction(float min, int steps, float step, float max) {
        this.min = min;
        this.steps = steps;
        this.step = step;
        this.max = max;
        Preconditions.checkArgument(steps > 0, "Steps must be a positive integer");
        Preconditions.checkArgument(Math.abs((min + steps * step) - max) <= 0.001F, "Max value is out-of-sync with min/steps/step fields.");
    }

    public static StepFunction constant(float val) {
        return new StepFunction(val, 1, 0, val);
    }

    public static StepFunction fromBounds(float min, float max, float step) {
        int steps = Math.max(1, Math.round((max - min) / step));
        return new StepFunction(min, steps, step, min + steps * step);
    }

    public float get(float level) {
        return this.min + (int) (this.steps * (level + 0.5F / this.steps)) * this.step;
    }

    public int getInt(float level) {
        return (int) this.get(level);
    }

    private Either<StepFunction, StepFunction> toEither() {
        if (this.steps == 1 && this.step == 0) {
            return Either.left(this);
        }
        return Either.right(this);
    }
}
