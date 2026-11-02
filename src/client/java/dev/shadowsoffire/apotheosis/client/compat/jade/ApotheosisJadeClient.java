package dev.shadowsoffire.apotheosis.client.compat.jade;

import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;

public class ApotheosisJadeClient {

    public static void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new SpawnerClientProvider(), Block.class);
        registration.registerBlockComponent(new EnchantingClientProvider(), Block.class);
        registration.addConfig(SpawnerClientProvider.UID, true);
        registration.addConfig(EnchantingClientProvider.UID, true);
    }
}
