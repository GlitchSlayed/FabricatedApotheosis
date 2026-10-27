package dev.shadowsoffire.apotheosis.placebo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.placebo.dynreg.DynamicRegistryManager;

/**
 * Internal Placebo support layer providing required utility bridges
 * for codec serialization, dynamic registries, and recipe helpers.
 */
public class PlaceboSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Placebo");

    public static void init() {
        LOGGER.info("Initializing Placebo support subsystem");
        DynamicRegistryManager.init();
    }
}
