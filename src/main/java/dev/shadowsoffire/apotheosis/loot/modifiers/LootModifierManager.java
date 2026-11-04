package dev.shadowsoffire.apotheosis.loot.modifiers;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.common.loot.LootModifier;

public class LootModifierManager implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/LootModifiers");
    public static final Identifier ID = Identifier.fromNamespaceAndPath("apotheosis", "loot_modifiers");
    public static final LootModifierManager INSTANCE = new LootModifierManager();

    private static final Map<Identifier, MapCodec<? extends LootModifier>> CODECS = new ConcurrentHashMap<>();

    public static final Codec<LootModifier> DIRECT_CODEC = Identifier.CODEC.dispatch(
        "type",
        m -> {
            for (Map.Entry<Identifier, MapCodec<? extends LootModifier>> entry : CODECS.entrySet()) {
                if (entry.getValue().equals(m.codec())) {
                    return entry.getKey();
                }
            }
            throw new IllegalArgumentException("Unregistered loot modifier codec for: " + m.getClass());
        },
        id -> {
            MapCodec<? extends LootModifier> codec = CODECS.get(id);
            if (codec == null) {
                throw new IllegalArgumentException("Unknown loot modifier type: " + id);
            }
            return codec;
        }
    );

    private static volatile List<LootModifier> MODIFIERS = Collections.emptyList();

    public static void registerCodec(Identifier id, MapCodec<? extends LootModifier> codec) {
        CODECS.put(id, codec);
    }

    public static void init() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(INSTANCE);
    }

    public static boolean hasModifiers() {
        return !MODIFIERS.isEmpty();
    }

    public static ObjectArrayList<ItemStack> applyModifiers(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        for (LootModifier mod : MODIFIERS) {
            loot = mod.apply(loot, ctx);
        }
        return loot;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        List<LootModifier> loaded = new ArrayList<>();
        Map<Identifier, Resource> resources = resourceManager.listResources("loot_modifiers", path -> path.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            try (Reader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonElement json = JsonParser.parseReader(reader);
                DIRECT_CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> LOGGER.error("Failed to parse loot modifier {}: {}", id, err))
                    .ifPresent(loaded::add);
            } catch (Exception e) {
                LOGGER.error("Failed to load loot modifier from {}", id, e);
            }
        }

        // NeoForge priority order: higher priority executes first
        loaded.sort(Comparator.comparingInt(LootModifier::getPriority).reversed());
        MODIFIERS = List.copyOf(loaded);
        LOGGER.info("Loaded {} global loot modifier(s)", MODIFIERS.size());
    }
}
