package dev.shadowsoffire.apotheosis.adventure;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.mobs.InvaderSpawnRules;
import dev.shadowsoffire.apotheosis.net.LootCategoryOverridesPayload;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

/**
 * Loads Adventure datapack data maps (`invader_spawn_rules.json` and `loot_category_overrides.json`)
 * and synchronizes item loot category overrides to connected clients.
 */
public class ApothDataMapsLoader extends SimplePreparableReloadListener<ApothDataMapsLoader.Prepared> implements IdentifiableResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/DataMaps");
    public static final Identifier ID = Apotheosis.loc("data_maps_loader");
    public static final ApothDataMapsLoader INSTANCE = new ApothDataMapsLoader();

    public record Prepared(Map<Identifier, InvaderSpawnRules> invaderRules, Map<Item, LootCategory> lootOverrides, int errorCount) {}

    public static void init() {
        PayloadHelper.registerPayload(new LootCategoryOverridesPayload.Provider());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(INSTANCE);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PayloadHelper.sendToPlayer(handler.player, LootCategoryOverridesPayload.fromActiveOverrides());
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected Prepared prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, InvaderSpawnRules> invaderRules = new HashMap<>();
        Map<Item, LootCategory> lootOverrides = new HashMap<>();
        int errors = 0;

        errors += loadInvaderSpawnRules(manager, invaderRules);
        errors += loadLootCategoryOverrides(manager, lootOverrides);

        return new Prepared(invaderRules, lootOverrides, errors);
    }

    @Override
    protected void apply(Prepared prepared, ResourceManager manager, ProfilerFiller profiler) {
        if (prepared.errorCount() > 0 && !LootCategory.getOverrides().isEmpty()) {
            LOGGER.error("Encountered {} error(s) reloading Adventure data maps; preserving existing data maps.", prepared.errorCount());
            return;
        }
        if (!prepared.invaderRules().isEmpty()) {
            InvaderSpawnRules.replaceRules(prepared.invaderRules());
        }
        LootCategory.replaceOverrides(prepared.lootOverrides());
        LOGGER.info("Loaded {} invader spawn rule(s) and {} loot category override(s) from data maps.", prepared.invaderRules().size(), prepared.lootOverrides().size());
    }

    private static int loadInvaderSpawnRules(ResourceManager manager, Map<Identifier, InvaderSpawnRules> target) {
        int errors = 0;
        Map<Identifier, List<Resource>> stacks = manager.listResourceStacks("data_maps/dimension_type", id -> id.getPath().endsWith("invader_spawn_rules.json"));
        for (Map.Entry<Identifier, List<Resource>> stack : stacks.entrySet()) {
            for (Resource res : stack.getValue()) {
                try (Reader reader = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                    JsonElement json = JsonParser.parseReader(reader);
                    if (!json.isJsonObject()) continue;
                    JsonObject obj = json.getAsJsonObject();
                    if (obj.has("replace") && obj.get("replace").getAsBoolean()) {
                        target.clear();
                    }
                    if (obj.has("remove") && obj.get("remove").isJsonArray()) {
                        JsonArray rem = obj.getAsJsonArray("remove");
                        for (JsonElement e : rem) {
                            Identifier id = Identifier.tryParse(e.getAsString());
                            if (id != null) target.remove(id);
                        }
                    }
                    if (obj.has("values") && obj.get("values").isJsonObject()) {
                        for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("values").entrySet()) {
                            Identifier dimId = Identifier.tryParse(entry.getKey());
                            if (dimId == null) {
                                LOGGER.warn("Invalid dimension id '{}' in {}", entry.getKey(), stack.getKey());
                                errors++;
                                continue;
                            }
                            InvaderSpawnRules.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(err -> LOGGER.error("Failed to parse invader spawn rule for {} in {}: {}", dimId, stack.getKey(), err))
                                .ifPresent(rule -> target.put(dimId, rule));
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed reading invader spawn rules from {}", stack.getKey(), e);
                    errors++;
                }
            }
        }
        return errors;
    }

    private static int loadLootCategoryOverrides(ResourceManager manager, Map<Item, LootCategory> target) {
        int errors = 0;
        Map<Identifier, List<Resource>> stacks = manager.listResourceStacks("data_maps/item", id -> id.getPath().endsWith("loot_category_overrides.json"));
        for (Map.Entry<Identifier, List<Resource>> stack : stacks.entrySet()) {
            for (Resource res : stack.getValue()) {
                try (Reader reader = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                    JsonElement json = JsonParser.parseReader(reader);
                    if (!json.isJsonObject()) continue;
                    JsonObject obj = json.getAsJsonObject();
                    if (obj.has("replace") && obj.get("replace").getAsBoolean()) {
                        target.clear();
                    }
                    if (obj.has("remove") && obj.get("remove").isJsonArray()) {
                        for (JsonElement e : obj.getAsJsonArray("remove")) {
                            Identifier id = Identifier.tryParse(e.getAsString());
                            if (id != null) BuiltInRegistries.ITEM.getOptional(id).ifPresent(target::remove);
                        }
                    }
                    if (obj.has("values") && obj.get("values").isJsonObject()) {
                        for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("values").entrySet()) {
                            if (entry.getKey().startsWith("#")) continue;
                            Identifier itemId = Identifier.tryParse(entry.getKey());
                            if (itemId == null) {
                                errors++;
                                continue;
                            }
                            Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
                            if (item == null) {
                                continue;
                            }
                            LootCategory.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(err -> LOGGER.error("Failed to parse loot category override for {} in {}: {}", itemId, stack.getKey(), err))
                                .ifPresent(cat -> target.put(item, cat));
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed reading loot category overrides from {}", stack.getKey(), e);
                    errors++;
                }
            }
        }
        return errors;
    }
}
