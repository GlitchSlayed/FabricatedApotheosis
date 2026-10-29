package dev.shadowsoffire.apothic_spawners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class ApothicSpawners {

    public static final String MODID = "apothic_spawners";
    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis : Spawner");

    public static void init() {
        SpawnerStats.bootstrap();
        ASObjects.bootstrap();
        ASConfig.load();

        BlockEntityTypes.MOB_SPAWNER.factory = ApothSpawnerTile::new;

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.getBlockEntity(hitResult.getBlockPos()) instanceof ApothSpawnerTile) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof SpawnEggItem) {
                    EntityType<?> type = SpawnEggItem.getType(stack);
                    if (type != null && type.builtInRegistryHolder().is(ASObjects.BLACKLISTED_FROM_SPAWNERS)) {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        LOGGER.info("Apothic Spawners module initialized");
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(type + "." + MODID + "." + path, args);
    }
}
