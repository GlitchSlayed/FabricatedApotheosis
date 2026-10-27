package dev.shadowsoffire.placebo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

/**
 * Internal Placebo compatibility layer.
 * <p>
 * Provides core utilities, codecs, dynamic registry systems, and registration helpers
 * required by the Apotheosis ecosystem on Fabric.
 */
public class Placebo {

    public static final String MODID = "placebo";
    public static final Logger LOGGER = LoggerFactory.getLogger("Placebo");

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
