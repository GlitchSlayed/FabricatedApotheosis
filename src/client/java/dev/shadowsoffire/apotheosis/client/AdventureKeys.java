package dev.shadowsoffire.apotheosis.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.effect.AttributeToggleAffix;
import dev.shadowsoffire.apotheosis.net.AttributeTogglesPayload;
import dev.shadowsoffire.apotheosis.net.RadialStatePayload;
import dev.shadowsoffire.apotheosis.util.ItemLinking;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class AdventureKeys {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Apotheosis.loc("keys"));

    public static final KeyMapping TOGGLE_RADIAL = new KeyMapping(
        Apotheosis.langKey("key", "toggle_radial_mining"),
        Type.KEYBOARD, InputConstants.KEY_O, CATEGORY);

    public static final KeyMapping TOGGLE_ATTRIBUTE_BONUSES = new KeyMapping(
        AttributeToggleAffix.TOGGLE_KEY,
        Type.KEYBOARD, InputConstants.KEY_K, CATEGORY);

    public static final KeyMapping OPEN_WORLD_TIER_SELECT = new KeyMapping(
        Apotheosis.langKey("key", "open_world_tier_select"),
        Type.KEYBOARD, InputConstants.KEY_T, CATEGORY);

    public static final KeyMapping LINK_ITEM_TO_CHAT = new KeyMapping(
        Apotheosis.langKey("key", "link_item_to_chat"),
        Type.KEYBOARD, InputConstants.KEY_T, CATEGORY);

    public static final KeyMapping COMPARE_EQUIPMENT = new KeyMapping(
        Apotheosis.langKey("key", "compare_equipment"),
        Type.KEYBOARD, InputConstants.KEY_LSHIFT, CATEGORY);

    public static void init() {
        KeyMappingHelper.registerKeyMapping(TOGGLE_RADIAL);
        KeyMappingHelper.registerKeyMapping(TOGGLE_ATTRIBUTE_BONUSES);
        KeyMappingHelper.registerKeyMapping(OPEN_WORLD_TIER_SELECT);
        KeyMappingHelper.registerKeyMapping(LINK_ITEM_TO_CHAT);
        KeyMappingHelper.registerKeyMapping(COMPARE_EQUIPMENT);

        ClientTickEvents.END_CLIENT_TICK.register(AdventureKeys::handleKeys);
    }

    public static void handleKeys(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (TOGGLE_RADIAL.consumeClick()) {
            if (client.gui.screen() == null) {
                PayloadHelper.sendToServer(new RadialStatePayload());
            }
        }

        while (TOGGLE_ATTRIBUTE_BONUSES.consumeClick()) {
            if (client.gui.screen() == null) {
                PayloadHelper.sendToServer(new AttributeTogglesPayload());
            }
        }

        while (OPEN_WORLD_TIER_SELECT.consumeClick()) {
            if (client.gui.screen() == null) {
                client.gui.setScreen(new WorldTierSelectScreen());
            }
        }
    }
}
