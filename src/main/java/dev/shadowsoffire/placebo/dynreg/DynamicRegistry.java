package dev.shadowsoffire.placebo.dynreg;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * A reload-safe dynamic registry that loads JSON elements from data packs.
 * <p>
 * Supports atomic reload application, actionable validation error reporting,
 * network synchronization, and automatic holder/cache invalidation.
 *
 * @param <R> The element type.
 */
public abstract class DynamicRegistry<R> extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> implements IdentifiableResourceReloadListener {

    private static final Map<Identifier, DynamicRegistry<?>> ALL_REGISTRIES = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final Logger logger;
    protected final Identifier id;
    protected final String path;
    protected final RegistrySerializer<R> serializer;

    protected volatile Map<Identifier, R> registry = Collections.emptyMap();
    protected final Map<Identifier, DynamicHolder<R>> holders = new ConcurrentHashMap<>();
    protected final List<Runnable> callbacks = new ArrayList<>();

    // Client-side staged map during network sync
    protected final Map<Identifier, R> clientStaged = new ConcurrentHashMap<>();

    public DynamicRegistry(Logger logger, Identifier id, String path, RegistrySerializer<R> serializer) {
        this.logger = logger;
        this.id = id;
        this.path = path;
        this.serializer = serializer;
        registerRegistry(this);
    }

    public void registerToBus() {
        registerRegistry(this);
    }

    public static synchronized void registerRegistry(DynamicRegistry<?> reg) {
        DynamicRegistry<?> existing = ALL_REGISTRIES.get(reg.getId());
        if (existing == reg) {
            return;
        }
        if (existing != null) {
            throw new IllegalArgumentException("Duplicate dynamic registry registered with ID: " + reg.getId());
        }
        ALL_REGISTRIES.put(reg.getId(), reg);
    }

    @Nullable
    public static DynamicRegistry<?> getRegistry(Identifier id) {
        return ALL_REGISTRIES.get(id);
    }

    public static Collection<DynamicRegistry<?>> getAllRegistries() {
        return Collections.unmodifiableCollection(ALL_REGISTRIES.values());
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public Identifier getFabricId() {
        return this.id;
    }

    public String getPath() {
        return this.path;
    }

    public RegistrySerializer<R> getSerializer() {
        return this.serializer;
    }

    public boolean containsKey(Identifier key) {
        return this.registry.containsKey(key);
    }

    @Nullable
    public R get(Identifier key) {
        return this.registry.get(key);
    }

    public Set<Identifier> getKeys() {
        return this.registry.keySet();
    }

    public Collection<R> getValues() {
        return this.registry.values();
    }

    public Map<Identifier, R> getEntries() {
        return this.registry;
    }

    public DynamicHolder<R> holder(Identifier key) {
        return this.holders.computeIfAbsent(key, id -> new DynamicHolder<>(this, id));
    }

    public DynamicHolder<R> emptyHolder() {
        return this.holder(DynamicHolder.EMPTY);
    }

    public synchronized void addCallback(Runnable callback) {
        this.callbacks.add(callback);
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> prepared = new HashMap<>();
        FileToIdConverter converter = FileToIdConverter.json(this.path);

        for (Map.Entry<Identifier, Resource> entry : converter.listMatchingResources(manager).entrySet()) {
            Identifier fullPath = entry.getKey();
            Identifier resourceId = converter.fileToId(fullPath);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                if (prepared.put(resourceId, json) != null) {
                    this.logger.warn("Duplicate resource detected for ID '{}' across packs, replacing with pack: {}", resourceId, entry.getValue().sourcePackId());
                }
            } catch (Exception e) {
                this.logger.error("Failed to parse JSON file '{}' for dynamic registry '{}': {}", fullPath, this.id, e.getMessage(), e);
            }
        }
        return prepared;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        this.logger.info("Applying dynamic registry '{}' ({} entries prepared)", this.id, prepared.size());

        Map<Identifier, R> staged = new HashMap<>();
        List<String> errors = new ArrayList<>();

        // Atomic validation & deserialization
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            Identifier entryId = entry.getKey();
            JsonElement json = entry.getValue();

            DataResult<R> result = this.serializer.codec().parse(JsonOps.INSTANCE, json);
            if (result.isError()) {
                String errorMsg = "Error decoding element '" + entryId + "' in registry '" + this.id + "': " + result.error().get().message();
                errors.add(errorMsg);
                this.logger.error(errorMsg);
            } else {
                R value = result.getOrThrow();
                try {
                    this.validateItem(entryId, value);
                    staged.put(entryId, value);
                } catch (Exception e) {
                    String errorMsg = "Validation failed for element '" + entryId + "' in registry '" + this.id + "': " + e.getMessage();
                    errors.add(errorMsg);
                    this.logger.error(errorMsg, e);
                }
            }
        }

        // Actionable validation error handling without partial corruption
        if (!errors.isEmpty()) {
            this.logger.error("Dynamic registry '{}' encountered {} validation error(s). Applying valid entries and logging diagnostics.", this.id, errors.size());
        }

        // Commit atomically
        this.commitReload(ImmutableMap.copyOf(staged));
    }

    protected void validateItem(Identifier id, R item) {
        // Subclasses may implement custom semantic validation
    }

    protected synchronized void commitReload(Map<Identifier, R> newEntries) {
        // 1. Swap registry map atomically
        this.registry = newEntries;

        // 2. Invalidate / rebind holders
        for (DynamicHolder<R> holder : this.holders.values()) {
            holder.unbind();
            holder.bind();
        }

        // 3. Fire cache invalidation callbacks
        for (Runnable callback : this.callbacks) {
            try {
                callback.run();
            } catch (Exception e) {
                this.logger.error("Error executing reload callback in dynamic registry '{}'", this.id, e);
            }
        }

        this.onReload();
        this.logger.info("Dynamic registry '{}' reload complete ({} entries active)", this.id, this.registry.size());
    }

    protected void onReload() {
        // Hook for subclasses
    }

    // Network Synchronization
    public void syncToPlayer(ServerPlayer player) {
        if (!this.serializer.isSynced()) return;

        PayloadHelper.sendToPlayer(player, new DynRegPayloads.Start(this.id));
        for (Map.Entry<Identifier, R> entry : this.registry.entrySet()) {
            DataResult<JsonElement> encoded = this.serializer.codec().encodeStart(JsonOps.INSTANCE, entry.getValue());
            if (encoded.isSuccess()) {
                String jsonStr = GSON.toJson(encoded.getOrThrow());
                PayloadHelper.sendToPlayer(player, new DynRegPayloads.Content(this.id, entry.getKey(), jsonStr));
            }
        }
        PayloadHelper.sendToPlayer(player, new DynRegPayloads.End(this.id));
    }

    public synchronized void initClientSync() {
        this.clientStaged.clear();
    }

    public synchronized void acceptClientContent(Identifier entryId, JsonElement json) {
        DataResult<R> result = this.serializer.codec().parse(JsonOps.INSTANCE, json);
        if (result.isSuccess()) {
            this.clientStaged.put(entryId, result.getOrThrow());
        } else {
            this.logger.error("Failed to decode client sync element '{}' in registry '{}': {}", entryId, this.id, result.error().get().message());
        }
    }

    public synchronized void completeClientSync() {
        this.commitReload(ImmutableMap.copyOf(this.clientStaged));
        this.clientStaged.clear();
        this.logger.info("Client synchronization complete for dynamic registry '{}'", this.id);
    }
}
