package dev.shadowsoffire.apotheosis.attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * Apothic Attributes foundation module.
 * Registers custom attributes and attaches them to all LivingEntity types.
 */
public class AttributesModule {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Attributes");

    public static void init() {
        LOGGER.info("Initializing Apothic Attributes module");

        // 1. Bootstrap attribute registrations
        ALObjects.Attributes.bootstrap();

        // 2. Attach all 21 custom attributes to all LivingEntity types via FabricDefaultAttributeRegistry
        FabricDefaultAttributeRegistry.MODIFY.register(context -> {
            context.modifyAll((type, builder) -> {
                for (Holder<Attribute> attr : ALObjects.Attributes.ALL) {
                    builder.add(attr);
                }
            });
        });

        LOGGER.info("Apothic Attributes module initialized successfully (21 attributes registered and attached)");
    }
}
