package dev.shadowsoffire.apothic_spawners.client;

import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerItem;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class ASClient {

    public static void init() {
        ApothSpawnerItem.isShiftDown = () -> {
            Minecraft mc = Minecraft.getInstance();
            return mc != null && mc.hasShiftDown();
        };

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            if (stack.getItem() instanceof SpawnEggItem) {
                EntityType<?> type = SpawnEggItem.getType(stack);
                if (type != null && type.builtInRegistryHolder().is(ASObjects.BLACKLISTED_FROM_SPAWNERS)) {
                    lines.add(ApothicSpawners.lang("misc", "banned").withStyle(ChatFormatting.GRAY));
                }
            }
        });
    }
}
