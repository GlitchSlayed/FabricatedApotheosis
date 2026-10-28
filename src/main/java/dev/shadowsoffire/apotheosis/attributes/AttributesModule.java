package dev.shadowsoffire.apotheosis.attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.payload.CritParticlePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * Apothic Attributes foundation module.
 * Registers custom attributes, mob effects, damage types, attachments, and attaches attributes to all LivingEntity types.
 */
public class AttributesModule {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Attributes");

    public static void init() {
        LOGGER.info("Initializing Apothic Attributes module");

        // 1. Bootstrap registrations
        ALObjects.bootstrap();

        // 2. Register network payloads
        PayloadTypeRegistry.clientboundPlay().register(CritParticlePayload.TYPE, CritParticlePayload.CODEC);

        // 3. Attach all custom attributes to all LivingEntity types via FabricDefaultAttributeRegistry
        FabricDefaultAttributeRegistry.MODIFY.register(context -> {
            context.modifyAll((type, builder) -> {
                for (Holder<Attribute> attr : ALObjects.Attributes.ALL) {
                    builder.add(attr);
                }
            });
        });

        LOGGER.info("Apothic Attributes module initialized successfully (all attributes registered and attached)");
    }
}
