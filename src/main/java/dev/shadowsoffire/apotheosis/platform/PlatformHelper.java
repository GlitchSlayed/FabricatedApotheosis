package dev.shadowsoffire.apotheosis.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Platform helper isolating Fabric Loader environment queries and services.
 */
public class PlatformHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Platform");

    public static void init() {
        LOGGER.debug("Platform helper initialized (Environment: {})", getEnvironment());
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public static EnvType getEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType();
    }
}
