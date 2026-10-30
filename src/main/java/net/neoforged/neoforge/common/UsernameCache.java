package net.neoforged.neoforge.common;

import java.util.UUID;

public class UsernameCache {

    public static String getLastKnownUsername(UUID uuid) {
        return uuid == null ? "Player" : uuid.toString().substring(0, 8);
    }
}
