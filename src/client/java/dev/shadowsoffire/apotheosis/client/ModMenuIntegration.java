package dev.shadowsoffire.apotheosis.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.shadowsoffire.apotheosis.client.config.ApotheosisConfigScreen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ApotheosisConfigScreen::create;
    }
}
