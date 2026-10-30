package dev.shadowsoffire.apotheosis.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class BossSummonerItem extends Item {

    public BossSummonerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        // Milestone 9: invader mob spawning logic
        return InteractionResult.PASS;
    }
}
