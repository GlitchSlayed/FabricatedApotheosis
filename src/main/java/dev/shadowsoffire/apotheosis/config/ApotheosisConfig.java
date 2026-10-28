package dev.shadowsoffire.apotheosis.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Master configuration manager for Fabricated Apotheosis.
 * <p>
 * Separates server gameplay rules from client preferences, guarantees atomic file
 * persistence, provides schema versioning and legacy migration, and coordinates
 * remote server-authoritative synchronization with clean disconnect clearing.
 * <p>
 * Dedicated-server safe: contains zero client or UI classes.
 */
public class ApotheosisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configDir = resolveConfigDir();

    private static Path resolveConfigDir() {
        try {
            if (FabricLoader.getInstance() != null) {
                Path dir = FabricLoader.getInstance().getConfigDir();
                if (dir != null) {
                    return dir;
                }
            }
        } catch (Throwable ignored) {}
        return Path.of("config");
    }

    public static void setConfigDir(Path dir) {
        configDir = dir;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static File getServerFile() {
        return configDir.resolve("apotheosis.json").toFile();
    }

    public static File getClientFile() {
        return configDir.resolve("apotheosis-client.json").toFile();
    }

    public static File getLegacyDir() {
        return configDir.resolve("apotheosis").toFile();
    }

    private static volatile ServerConfig localServerConfig = new ServerConfig();
    private static volatile ClientConfig clientConfig = new ClientConfig();

    @Nullable
    private static volatile ServerConfig remoteServerConfig = null;

    /**
     * Gets the active authoritative gameplay configuration.
     * When connected to a remote server, returns the synced remote configuration;
     * otherwise returns the local server configuration.
     */
    public static ServerConfig getServer() {
        ServerConfig remote = remoteServerConfig;
        return remote != null ? remote : localServerConfig;
    }

    /**
     * Gets the local server configuration file instance (editable on main menu / singleplayer).
     */
    public static ServerConfig getLocalServer() {
        return localServerConfig;
    }

    /**
     * Gets the local client preferences configuration instance.
     */
    public static ClientConfig getClient() {
        return clientConfig;
    }

    /**
     * Returns true if currently connected to a remote server with authoritative settings.
     */
    public static boolean isRemoteServer() {
        return remoteServerConfig != null;
    }

    /**
     * Sets the remote server gameplay configuration received over the network.
     */
    public static void setRemoteServerConfig(ServerConfig config) {
        config.validate();
        remoteServerConfig = config;
        LOGGER.info("Applied authoritative server configuration received from remote server.");
    }

    /**
     * Clears any remote server configuration upon disconnect, restoring local settings.
     */
    public static void clearRemoteServerConfig() {
        if (remoteServerConfig != null) {
            remoteServerConfig = null;
            LOGGER.info("Cleared remote server configuration; restored local configuration.");
        }
    }

    /**
     * Loads both server gameplay config and client preferences from disk, performing
     * atomic validation, schema migrations, and legacy .cfg conversions.
     */
    public static void load() {
        loadServerConfig();
        loadClientConfig();

        // Check for legacy NeoForge .cfg files
        if (ConfigMigration.migrateLegacyCfg(getLegacyDir(), localServerConfig, clientConfig)) {
            LOGGER.info("Successfully migrated legacy configuration settings. Saving updated configs...");
            saveLocalServer();
            saveClient();
        }
    }

    private static void loadServerConfig() {
        File serverFile = getServerFile();
        if (serverFile.exists()) {
            try (FileReader reader = new FileReader(serverFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                ServerConfig loaded = GSON.fromJson(json, ServerConfig.class);
                if (loaded != null) {
                    boolean migrated = ConfigMigration.migrateServerJson(json, loaded);
                    loaded.validate();
                    localServerConfig = loaded;
                    LOGGER.info("Loaded server configuration from {}", serverFile.getName());
                    if (migrated) {
                        saveLocalServer();
                    }
                    return;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse server configuration {}, falling back to defaults with diagnostics", serverFile.getName(), e);
            }
        }
        // Save initial default server configuration
        localServerConfig = new ServerConfig();
        localServerConfig.validate();
        saveLocalServer();
    }

    private static void loadClientConfig() {
        File clientFile = getClientFile();
        if (clientFile.exists()) {
            try (FileReader reader = new FileReader(clientFile)) {
                ClientConfig loaded = GSON.fromJson(reader, ClientConfig.class);
                if (loaded != null) {
                    loaded.validate();
                    clientConfig = loaded;
                    LOGGER.info("Loaded client preferences from {}", clientFile.getName());
                    return;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse client preferences {}, falling back to defaults", clientFile.getName(), e);
            }
        }
        // Save initial default client configuration
        clientConfig = new ClientConfig();
        clientConfig.validate();
        saveClient();
    }

    /**
     * Saves local server gameplay settings atomically to disk.
     */
    public static void saveLocalServer() {
        localServerConfig.validate();
        File serverFile = getServerFile();
        try {
            atomicWrite(serverFile.toPath(), GSON.toJson(localServerConfig));
            LOGGER.debug("Atomically saved server configuration to {}", serverFile.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to atomically save server configuration to {}", serverFile.getName(), e);
        }
    }

    /**
     * Saves client preferences atomically to disk.
     */
    public static void saveClient() {
        clientConfig.validate();
        File clientFile = getClientFile();
        try {
            atomicWrite(clientFile.toPath(), GSON.toJson(clientConfig));
            LOGGER.debug("Atomically saved client preferences to {}", clientFile.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to atomically save client preferences to {}", clientFile.getName(), e);
        }
    }

    /**
     * Performs an atomic write using a sibling temporary file and atomic replace.
     */
    public static void atomicWrite(Path target, String content) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        Files.writeString(temp, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
