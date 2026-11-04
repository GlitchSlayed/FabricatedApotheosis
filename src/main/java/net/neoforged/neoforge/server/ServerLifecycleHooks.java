package net.neoforged.neoforge.server;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerLifecycleHooks {

    private static MinecraftServer currentServer;
    private static @Nullable Boolean integratedServerOverrideForTesting;

    static {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public static boolean isIntegratedServerRunning() {
        if (integratedServerOverrideForTesting != null) {
            return integratedServerOverrideForTesting;
        }
        return currentServer != null && !currentServer.isDedicatedServer();
    }

    public static void setIntegratedServerOverrideForTesting(@Nullable Boolean override) {
        integratedServerOverrideForTesting = override;
    }
}
