package dev.shadowsoffire.apotheosis.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public record AttributeTooltipContext(
    @Nullable Player player,
    Item.TooltipContext context,
    TooltipDisplay display,
    TooltipFlag flag
) {

    public static AttributeTooltipContext of(@Nullable Player player, Item.TooltipContext context, TooltipDisplay display, TooltipFlag flag) {
        return new AttributeTooltipContext(player, context, display, flag);
    }

    public static AttributeTooltipContext of(@Nullable Player player, TooltipFlag flag) {
        Level level = player != null ? player.level() : null;
        return new AttributeTooltipContext(player, Item.TooltipContext.of(level), TooltipDisplay.DEFAULT, flag);
    }

    public float tickRate() {
        if (this.player != null && this.player.level() != null) {
            return this.player.level().tickRateManager().tickrate();
        }
        return 20.0f;
    }

    public HolderLookup.Provider registries() {
        return this.context.registries();
    }
}
