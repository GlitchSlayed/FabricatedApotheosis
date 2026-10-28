package dev.shadowsoffire.apothic_enchanting;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;

public class ApothEnchConfig {

    public static boolean showEnchantedBookMetadata = true;
    public static int sculkShelfNoiseChance = 200;
    public static boolean enableInlineEnchDescs = false;

    public static void syncFromClientConfig() {
        showEnchantedBookMetadata = ApotheosisConfig.getClient().showEnchantedBookMetadata;
        sculkShelfNoiseChance = ApotheosisConfig.getClient().sculkShelfNoiseChance;
        enableInlineEnchDescs = ApotheosisConfig.getClient().enableInlineEnchDescs;
    }
}
