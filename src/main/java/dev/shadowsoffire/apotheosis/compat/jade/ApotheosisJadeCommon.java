package dev.shadowsoffire.apotheosis.compat.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaCommonRegistration;

public class ApotheosisJadeCommon {

    public static void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new SpawnerServerDataProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new EnchantingServerDataProvider(), Block.class);
    }
}
