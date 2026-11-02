package dev.shadowsoffire.apotheosis.client;

import org.jspecify.annotations.Nullable;

import dev.shadowsoffire.apotheosis.loot.RarityRenderData;

public interface AffixItemRenderState {

    @Nullable
    RarityRenderData apoth$getRarityRenderData();

    void apoth$setRarityRenderData(@Nullable RarityRenderData data);

    int apoth$getColor();

    void apoth$setColor(int color);

    boolean apoth$isOnGround();

    void apoth$setOnGround(boolean onGround);
}
