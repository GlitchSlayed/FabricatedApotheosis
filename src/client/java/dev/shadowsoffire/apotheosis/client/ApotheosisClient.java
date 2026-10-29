package dev.shadowsoffire.apotheosis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.payload.CritParticlePayload;
import dev.shadowsoffire.apotheosis.client.attributes.AttributesClientTooltipHandler;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.placebo.PlaceboClient;
import dev.shadowsoffire.placebo.client.PlaceboClientPayloadHandler;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.world.entity.Entity;

/**
 * Client entrypoint for Fabricated Apotheosis.
 */
public class ApotheosisClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Fabricated Apotheosis client subsystems");
        PlaceboClient.init();
        PayloadHelper.setClientHandler(new PlaceboClientPayloadHandler());
        AttributesClientTooltipHandler.init();
        dev.shadowsoffire.apothic_enchanting.ApothEnchClient.init();
        dev.shadowsoffire.apothic_spawners.client.ASClient.init();

        // Register crit particle payload receiver
        ClientPlayNetworking.registerGlobalReceiver(CritParticlePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().level != null) {
                    Entity entity = context.client().level.getEntity(payload.entityId());
                    if (entity != null) {
                        context.client().particleEngine.createTrackingEmitter(entity, ALObjects.Particles.APOTH_CRIT.value());
                    }
                }
            });
        });

        // Register particle factory
        ParticleProviderRegistry.getInstance().register(ALObjects.Particles.APOTH_CRIT.value(), CritParticle.Provider::new);

        // Clear remote server settings on disconnect to guarantee no stale state persists
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ApotheosisConfig.clearRemoteServerConfig();
        });
    }
}
