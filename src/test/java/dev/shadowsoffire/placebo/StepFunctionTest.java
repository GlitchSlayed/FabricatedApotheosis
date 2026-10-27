package dev.shadowsoffire.placebo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.util.StepFunction;

public class StepFunctionTest {

    @Test
    public void testStepFunctionMath() {
        // min = 10, max = 20, step = 2 (5 steps)
        StepFunction fn = StepFunction.fromBounds(10.0F, 20.0F, 2.0F);

        assertEquals(10.0F, fn.min());
        assertEquals(20.0F, fn.max());
        assertEquals(2.0F, fn.step());
        assertEquals(5, fn.steps());

        // At level 0: should be 10.0
        assertEquals(10.0F, fn.get(0.0F), 0.001F);
        // At level 1: should be 20.0
        assertEquals(20.0F, fn.get(1.0F), 0.001F);
        // At level 0.5: should be 16.0
        assertEquals(16.0F, fn.get(0.5F), 0.001F);
    }

    @Test
    public void testCodecSerialization() {
        StepFunction original = StepFunction.fromBounds(5.0F, 15.0F, 2.5F);
        DataResult<JsonElement> encoded = StepFunction.CODEC.encodeStart(JsonOps.INSTANCE, original);
        assertTrue(encoded.isSuccess(), "Encoding must succeed");

        DataResult<StepFunction> decoded = StepFunction.CODEC.parse(JsonOps.INSTANCE, encoded.getOrThrow());
        assertTrue(decoded.isSuccess(), "Decoding must succeed");

        StepFunction result = decoded.getOrThrow();
        assertEquals(original.min(), result.min(), 0.001F);
        assertEquals(original.max(), result.max(), 0.001F);
        assertEquals(original.step(), result.step(), 0.001F);
    }
}
