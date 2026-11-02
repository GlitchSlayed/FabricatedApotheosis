package dev.shadowsoffire.apotheosis.compat.jade;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ApotheosisJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        ApotheosisJadeCommon.register(registration);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                Class<?> clientClazz = Class.forName("dev.shadowsoffire.apotheosis.client.compat.jade.ApotheosisJadeClient");
                clientClazz.getMethod("registerClient", IWailaClientRegistration.class).invoke(null, registration);
            }
            catch (Throwable t) {
                // Jade client registration failed or not present
            }
        }
    }
}
