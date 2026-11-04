package net.neoforged.neoforge.common;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class CommonHooks {

    public static <T> HolderLookup.RegistryLookup<T> resolveLookup(ResourceKey<? extends Registry<T>> key) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess().lookupOrThrow(key);
        }
        return null;
    }

    public static net.neoforged.bus.api.Event fireBlockBreak(net.minecraft.server.level.ServerLevel level, net.minecraft.world.level.GameType gameType, net.minecraft.server.level.ServerPlayer player, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        net.neoforged.bus.api.Event event = new net.neoforged.bus.api.Event();
        if (!level.mayInteract(player, pos)) {
            event.setCanceled(true);
            return event;
        }
        boolean allowed = net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.invoker()
            .beforeBlockBreak(level, player, pos, state, level.getBlockEntity(pos));
        if (!allowed) {
            event.setCanceled(true);
        }
        return event;
    }
}
