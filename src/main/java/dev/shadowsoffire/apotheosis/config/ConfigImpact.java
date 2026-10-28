package dev.shadowsoffire.apotheosis.config;

/**
 * Defines when a configuration option takes effect.
 */
public enum ConfigImpact {
    /**
     * Option takes effect immediately at runtime.
     */
    LIVE("Takes effect immediately without restart or reload."),

    /**
     * Option requires a resource or datapack reload (/reload).
     */
    RELOAD_REQUIRED("Takes effect on datapack / resource reload (/reload)."),

    /**
     * Option requires a game restart to take effect.
     */
    RESTART_REQUIRED("Requires game restart to take effect.");

    private final String description;

    ConfigImpact(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
