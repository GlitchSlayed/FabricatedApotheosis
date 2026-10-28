package dev.shadowsoffire.apothic_enchanting.table;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apothic_enchanting.EnchantmentInfo;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Loads per-enchantment overrides from `data_maps/enchantment/enchantment_info.json` across all namespaces.
 */
public class EnchantmentInfoLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/EnchantmentInfo");
    public static final Identifier ID = Identifier.fromNamespaceAndPath("apothic_enchanting", "enchantment_info_loader");
    private static final Map<Identifier, EnchantmentInfo> INFO_MAP = new HashMap<>();

    public static final EnchantmentInfoLoader INSTANCE = new EnchantmentInfoLoader();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        INFO_MAP.clear();
        LOGGER.info("Loading enchantment info data maps...");

        manager.listResources("data_maps/enchantment", id -> id.getPath().endsWith("enchantment_info.json")).forEach((id, res) -> {
            try (Reader reader = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                JsonElement json = JsonParser.parseReader(reader);
                if (json.isJsonObject() && json.getAsJsonObject().has("values")) {
                    JsonObject values = json.getAsJsonObject().getAsJsonObject("values");
                    for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                        Identifier enchId = Identifier.tryParse(entry.getKey());
                        if (enchId != null) {
                            EnchantmentInfo.CODEC.codec().parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(err -> LOGGER.error("Failed to parse enchantment info for {}: {}", enchId, err))
                                .ifPresent(info -> INFO_MAP.put(enchId, info));
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error reading enchantment info data map from {}", id, e);
            }
        });

        LOGGER.info("Loaded {} enchantment info override(s)", INFO_MAP.size());
    }

    public static EnchantmentInfo get(Holder<Enchantment> ench) {
        if (ench == null) return EnchantmentInfo.EMPTY;
        Optional<ResourceKey<Enchantment>> key = ench.unwrapKey();
        if (key.isPresent()) {
            EnchantmentInfo info = INFO_MAP.get(key.get().identifier());
            if (info != null) return info;
        }
        return null;
    }

    public static void putDirect(Identifier id, EnchantmentInfo info) {
        INFO_MAP.put(id, info);
    }
}
