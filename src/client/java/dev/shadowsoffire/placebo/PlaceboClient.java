package dev.shadowsoffire.placebo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client-side tick tracker and utilities for Placebo subsystems.
 */
public class PlaceboClient {

    public static long ticks = 0;

    public static float getColorTicks() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        float partial = mc != null && mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(false) : 0F;
        return (ticks + partial) / 0.5F;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ticks++);
    }
}
