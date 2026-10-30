package dev.shadowsoffire.apotheosis.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record InvaderSpawnRules() {
    public static final Codec<InvaderSpawnRules> CODEC = MapCodec.unitCodec(new InvaderSpawnRules());
}
