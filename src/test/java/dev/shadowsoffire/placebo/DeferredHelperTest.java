package dev.shadowsoffire.placebo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.Lifecycle;

import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class DeferredHelperTest {

    @Test
    public void testDuplicateRegistrationThrowsClearError() {
        DeferredHelper helper = DeferredHelper.create("test_mod");
        MappedRegistry<String> testRegistry = new MappedRegistry<>(
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("test_mod", "custom_registry")),
            Lifecycle.stable()
        );

        // First registration succeeds
        helper.custom(testRegistry, "entry_one", () -> "first");

        // Attempting to register the same key in the same helper must throw IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            helper.custom(testRegistry, "entry_one", () -> "second");
        });

        assertTrue(ex.getMessage().contains("Duplicate registration detected for key 'test_mod:entry_one'"),
            "Error message must clearly identify the duplicate key: " + ex.getMessage());
    }
}
