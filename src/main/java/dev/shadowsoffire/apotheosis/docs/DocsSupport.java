package dev.shadowsoffire.apotheosis.docs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Documentation support subsystem (Chronicle of Shadows).
 */
public class DocsSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Docs");

    public static void init() {
        LOGGER.debug("Documentation support subsystem initialized");
    }
}
