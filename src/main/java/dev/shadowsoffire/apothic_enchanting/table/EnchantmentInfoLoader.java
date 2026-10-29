package dev.shadowsoffire.apothic_enchanting.table;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.EnchantmentInfo;
import dev.shadowsoffire.apothic_enchanting.payloads.EnchantmentInfoPayload;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Loads per-enchantment overrides from `data_maps/enchantment/enchantment_info.json` across all namespaces.
 * Uses staged atomic validation so a failed reload does not publish partial or corrupted state.
 */
public class EnchantmentInfoLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/EnchantmentInfo");
    public static final Identifier ID = Identifier.fromNamespaceAndPath("apothic_enchanting", "enchantment_info_loader");
    private static volatile Map<Identifier, EnchantmentInfo> INFO_MAP = Collections.emptyMap();
    private static volatile Map<Identifier, EnchantmentInfo> LOCAL_FALLBACK_MAP = Collections.emptyMap();

    public static final EnchantmentInfoLoader INSTANCE = new EnchantmentInfoLoader();

    public static void init() {
        PayloadHelper.registerPayload(new EnchantmentInfoPayload.Provider());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            syncToPlayer(handler.player);
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        LOGGER.info("Loading enchantment info data maps with staged validation...");
        Map<Identifier, EnchantmentInfo> staged = new HashMap<>();

        try {
            Map<Identifier, List<Resource>> resourceStacks = manager.listResourceStacks("data_maps/enchantment", id -> id.getPath().endsWith("enchantment_info.json"));

            for (Map.Entry<Identifier, List<Resource>> stackEntry : resourceStacks.entrySet()) {
                Identifier fileId = stackEntry.getKey();
                for (Resource res : stackEntry.getValue()) {
                    try (Reader reader = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                        JsonElement json = JsonParser.parseReader(reader);
                        if (!json.isJsonObject()) continue;
                        JsonObject obj = json.getAsJsonObject();

                        // Top-level "replace": true clears previous entries from lower packs
                        if (obj.has("replace") && obj.get("replace").getAsBoolean()) {
                            staged.clear();
                        }

                        // Top-level "remove": ["id1", "id2"] removes specific entries
                        if (obj.has("remove") && obj.get("remove").isJsonArray()) {
                            JsonArray removeArray = obj.getAsJsonArray("remove");
                            for (JsonElement elem : removeArray) {
                                Identifier toRemove = Identifier.tryParse(elem.getAsString());
                                if (toRemove != null) {
                                    staged.remove(toRemove);
                                }
                            }
                        }

                        // Values dictionary
                        if (obj.has("values") && obj.get("values").isJsonObject()) {
                            JsonObject values = obj.getAsJsonObject("values");
                            for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                                Identifier enchId = Identifier.tryParse(entry.getKey());
                                if (enchId == null) {
                                    LOGGER.warn("Malformed enchantment identifier '{}' in {}", entry.getKey(), fileId);
                                    continue;
                                }

                                JsonElement val = entry.getValue();
                                if (val.isJsonObject() && val.getAsJsonObject().has("remove") && val.getAsJsonObject().get("remove").getAsBoolean()) {
                                    staged.remove(enchId);
                                    continue;
                                }

                                EnchantmentInfo.CODEC.codec().parse(JsonOps.INSTANCE, val)
                                    .resultOrPartial(err -> LOGGER.error("Failed to parse enchantment info for {} in {}: {}", enchId, fileId, err))
                                    .ifPresent(info -> staged.put(enchId, info));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error reading enchantment info resource {} from {}", fileId, res.sourcePackId(), e);
                    }
                }
            }

            // Atomic publication
            INFO_MAP = Map.copyOf(staged);
            LOCAL_FALLBACK_MAP = INFO_MAP;
            ApothicEnchanting.clearEnchantmentCaches();
            LOGGER.info("Successfully loaded and published {} enchantment info override(s)", INFO_MAP.size());
        } catch (Exception e) {
            LOGGER.error("Fatal error during enchantment info reload; keeping previous state ({} entries)", INFO_MAP.size(), e);
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        PayloadHelper.sendToPlayer(player, new EnchantmentInfoPayload(INFO_MAP));
    }

    public static void applyClientSync(Map<Identifier, EnchantmentInfo> synced) {
        INFO_MAP = Map.copyOf(synced);
        ApothicEnchanting.clearEnchantmentCaches();
        LOGGER.info("Applied server-synchronized enchantment info ({} entries)", synced.size());
    }

    public static void resetClient() {
        INFO_MAP = LOCAL_FALLBACK_MAP;
        ApothicEnchanting.clearEnchantmentCaches();
        LOGGER.info("Reset enchantment info to local configuration");
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
        Map<Identifier, EnchantmentInfo> copy = new HashMap<>(INFO_MAP);
        copy.put(id, info);
        INFO_MAP = Map.copyOf(copy);
        ApothicEnchanting.clearEnchantmentCaches();
    }
}
