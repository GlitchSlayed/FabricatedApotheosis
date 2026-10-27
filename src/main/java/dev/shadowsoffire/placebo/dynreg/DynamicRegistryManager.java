package dev.shadowsoffire.placebo.dynreg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

/**
 * Coordinates dynamic registry registration, resource reload listener registration,
 * and server-to-client data synchronization.
 */
public class DynamicRegistryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Placebo/DynRegManager");
    private static boolean INITIALIZED = false;

    public static synchronized void init() {
        if (INITIALIZED) return;
        INITIALIZED = true;

        // Register payloads for registry synchronization
        PayloadHelper.registerPayload(new DynRegPayloads.Start.Provider());
        PayloadHelper.registerPayload(new DynRegPayloads.Content.Provider());
        PayloadHelper.registerPayload(new DynRegPayloads.End.Provider());

        // Register all dynamic registries as server resource reload listeners
        for (DynamicRegistry<?> registry : DynamicRegistry.getAllRegistries()) {
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(registry);
            LOGGER.debug("Registered reload listener for dynamic registry '{}'", registry.getId());
        }

        // Synchronize dynamic registries when a player joins the server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (DynamicRegistry<?> registry : DynamicRegistry.getAllRegistries()) {
                registry.syncToPlayer(handler.player);
            }
        });

        LOGGER.info("Dynamic registry manager initialized successfully");
    }
}
