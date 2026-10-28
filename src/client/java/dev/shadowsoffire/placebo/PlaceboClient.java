package dev.shadowsoffire.placebo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client-side tick tracker and utilities for Placebo subsystems.
 */
public class PlaceboClient {

    public static long ticks = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ticks++);
    }
}
