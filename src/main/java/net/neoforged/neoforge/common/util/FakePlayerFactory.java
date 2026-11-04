package net.neoforged.neoforge.common.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.level.ServerLevel;

public class FakePlayerFactory {

    private static final GameProfile MINECRAFT = new GameProfile(UUID.fromString("41C82C87-7AFB-4045-BB2B-001C6336257E"), "[Minecraft]");
    private static final Map<GameProfile, FakePlayer> FAKE_PLAYERS = new ConcurrentHashMap<>();

    public static FakePlayer getMinecraft(ServerLevel level) {
        return get(level, MINECRAFT);
    }

    public static FakePlayer get(ServerLevel level, GameProfile profile) {
        FakePlayer fake = FAKE_PLAYERS.get(profile);
        if (fake == null || fake.level() != level) {
            fake = new FakePlayer(level, profile);
            FAKE_PLAYERS.put(profile, fake);
        }
        return fake;
    }

    public static void clear() {
        FAKE_PLAYERS.clear();
    }
}
