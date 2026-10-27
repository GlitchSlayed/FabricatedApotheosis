package dev.shadowsoffire.placebo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;
import net.minecraft.resources.Identifier;

public class DynamicRegistryTest {

    // Simple test record
    record TestItem(String name, int value) {
        public static final Codec<TestItem> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.STRING.fieldOf("name").forGetter(TestItem::name),
                Codec.INT.fieldOf("value").forGetter(TestItem::value))
            .apply(inst, TestItem::new));
    }

    static class TestRegistry extends DynamicRegistry<TestItem> {
        public TestRegistry(Identifier id) {
            super(LoggerFactory.getLogger("TestRegistry"), id, "test_items", RegistrySerializer.simple(TestItem.CODEC));
        }

        public void applyDirect(Map<Identifier, JsonElement> data) {
            this.apply(data, null, null);
        }
    }

    @Test
    public void testDynamicDataLoadReloadAndInvalidation() {
        Identifier regId = Identifier.fromNamespaceAndPath("test", "test_registry");
        TestRegistry registry = new TestRegistry(regId);

        Identifier key1 = Identifier.fromNamespaceAndPath("test", "item_1");
        Identifier key2 = Identifier.fromNamespaceAndPath("test", "item_2");

        DynamicHolder<TestItem> holder1 = registry.holder(key1);
        DynamicHolder<TestItem> holder2 = registry.holder(key2);

        assertFalse(holder1.isBound(), "Holder must be initially unbound");
        assertThrows(IllegalStateException.class, holder1::get);

        // Track cache invalidation callback
        AtomicBoolean cacheInvalidated = new AtomicBoolean(false);
        registry.addCallback(() -> cacheInvalidated.set(true));

        // 1. Initial Load: item_1 = { name: "alpha", value: 10 }
        Map<Identifier, JsonElement> batch1 = new HashMap<>();
        JsonObject obj1 = new JsonObject();
        obj1.addProperty("name", "alpha");
        obj1.addProperty("value", 10);
        batch1.put(key1, obj1);

        registry.applyDirect(batch1);

        // Verification of initial load
        assertTrue(holder1.isBound(), "Holder 1 should now be bound");
        assertEquals("alpha", holder1.get().name());
        assertEquals(10, holder1.get().value());
        assertFalse(holder2.isBound(), "Holder 2 should remain unbound");
        assertTrue(cacheInvalidated.get(), "Callback must have fired");

        // 2. Valid Reload: Update item_1 to value 50, add item_2 = { name: "beta", value: 99 }
        cacheInvalidated.set(false);
        Map<Identifier, JsonElement> batch2 = new HashMap<>();

        JsonObject obj1Updated = new JsonObject();
        obj1Updated.addProperty("name", "alpha_updated");
        obj1Updated.addProperty("value", 50);
        batch2.put(key1, obj1Updated);

        JsonObject obj2 = new JsonObject();
        obj2.addProperty("name", "beta");
        obj2.addProperty("value", 99);
        batch2.put(key2, obj2);

        registry.applyDirect(batch2);

        // Verification of updated behavior after reload
        assertTrue(cacheInvalidated.get(), "Callback must fire upon reload");
        assertEquals("alpha_updated", holder1.get().name(), "Behavior must change to updated value");
        assertEquals(50, holder1.get().value());
        assertTrue(holder2.isBound(), "Holder 2 must now be bound");
        assertEquals("beta", holder2.get().name());

        // 3. Invalid Reload: Supply malformed JSON for a new entry
        // The registry must report the error and NOT corrupt existing valid state partially
        Map<Identifier, JsonElement> batch3 = new HashMap<>();
        JsonObject malformed = new JsonObject();
        malformed.addProperty("name", "broken");
        // "value" field is intentionally omitted to cause a decode error!
        batch3.put(Identifier.fromNamespaceAndPath("test", "broken_item"), malformed);

        registry.applyDirect(batch3);

        // The broken item should NOT be present in registry
        assertNull(registry.get(Identifier.fromNamespaceAndPath("test", "broken_item")),
            "Malformed entry must not be loaded into active registry");
        assertEquals(0, registry.getEntries().size(), "Only valid entries from new batch are staged");

        // 4. Test holder unbinding when an entry is removed
        assertFalse(holder1.isBound(), "Holder 1 must become unbound when removed from data pack");
        assertThrows(IllegalStateException.class, holder1::get, "Getting an unbound holder must throw");
    }
}
