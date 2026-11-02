package dev.shadowsoffire.apotheosis.client.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.shadowsoffire.apotheosis.client.AffixItemRenderState;
import dev.shadowsoffire.apotheosis.loot.RarityRenderData;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;

@Mixin(ItemEntityRenderState.class)
public class ItemEntityRenderStateMixin implements AffixItemRenderState {

    @Unique
    @Nullable
    private RarityRenderData apoth$renderData;

    @Unique
    private int apoth$color;

    @Unique
    private boolean apoth$onGround;

    @Override
    @Nullable
    public RarityRenderData apoth$getRarityRenderData() {
        return this.apoth$renderData;
    }

    @Override
    public void apoth$setRarityRenderData(@Nullable RarityRenderData data) {
        this.apoth$renderData = data;
    }

    @Override
    public int apoth$getColor() {
        return this.apoth$color;
    }

    @Override
    public void apoth$setColor(int color) {
        this.apoth$color = color;
    }

    @Override
    public boolean apoth$isOnGround() {
        return this.apoth$onGround;
    }

    @Override
    public void apoth$setOnGround(boolean onGround) {
        this.apoth$onGround = onGround;
    }
}
