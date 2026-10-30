package dev.shadowsoffire.apotheosis.util;

import java.util.UUID;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class ItemLinking {

    private static final Object2LongMap<UUID> LAST_LINK_TIMES = new Object2LongOpenHashMap<>();
    public static Runnable clientLinkSender = () -> {};

    public static void sendHoveredItem() {
        clientLinkSender.run();
    }

    public static boolean isOnCooldown(UUID id, long gameTime) {
        return LAST_LINK_TIMES.getOrDefault(id, -AdventureConfig.itemLinkingCooldown) + AdventureConfig.itemLinkingCooldown >= gameTime;
    }

    public static void setCooldown(UUID id, long gameTime) {
        LAST_LINK_TIMES.put(id, gameTime);
    }

    public static void startCooldown(UUID id, long gameTime) {
        setCooldown(id, gameTime);
    }
}
