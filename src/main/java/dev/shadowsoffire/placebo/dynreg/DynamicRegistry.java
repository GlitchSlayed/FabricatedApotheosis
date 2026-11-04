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

import dev.shadowsoffire.placebo.dynreg.tag.DynamicHolderSet;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicTagKey;
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
    protected final Map<Identifier, DynamicHolderSet.Named<R>> tags = new ConcurrentHashMap<>();
    protected final List<Runnable> callbacks = new ArrayList<>();

    // Client-side staged map during network sync
    protected final Map<Identifier, R> clientStaged = new ConcurrentHashMap<>();
    protected final Set<Identifier> dependencies = new java.util.concurrent.CopyOnWriteArraySet<>();

    public DynamicRegistry(Logger logger, Identifier id, RegistrySerializer<R> serializer) {
        this(logger, id, id.getPath(), serializer);
    }

    public DynamicRegistry(Logger logger, Identifier id, String path, RegistrySerializer<R> serializer) {
        this.logger = logger;
        this.id = id;
        this.path = path;
        this.serializer = serializer;

        if (!this.id.equals(Identifier.fromNamespaceAndPath("apotheosis", "rarities"))
            && !this.id.equals(Identifier.fromNamespaceAndPath("apotheosis", "gear_sets"))
            && !this.id.getNamespace().equals("apothic_enchanting")
            && !this.id.getNamespace().equals("test")) {
            this.dependencies.add(Identifier.fromNamespaceAndPath("apotheosis", "rarities"));
        }

        registerRegistry(this);
    }

    public DynamicRegistry<R> dependsOn(Identifier id) {
        this.dependencies.add(id);
        return this;
    }

    public DynamicRegistry<R> dependsOn(DynamicRegistry<?> other) {
        this.dependencies.add(other.getId());
        return this;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.unmodifiableCollection(this.dependencies);
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
        try {
            net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.SERVER_DATA).registerReloadListener(reg);
            reg.logger.info("Registered dynamic registry '{}' as server data reload listener", reg.getId());
        } catch (Throwable t) {
            reg.logger.error("Failed to register reload listener for dynamic registry '{}'", reg.getId(), t);
        }
    }

    @Nullable
    public static DynamicRegistry<?> getRegistry(Identifier id) {
        return ALL_REGISTRIES.get(id);
    }

    public static Collection<DynamicRegistry<?>> getAllRegistries() {
        return Collections.unmodifiableCollection(ALL_REGISTRIES.values());
    }

    public static Map<Identifier, DynamicRegistry<?>> allRegistries() {
        return Collections.unmodifiableMap(ALL_REGISTRIES);
    }

    public Logger getLogger() {
        return this.logger;
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

    @Nullable
    public R getValue(Identifier key) {
        return this.get(key);
    }

    public DynamicHolderSet.Named<R> getOrCreateTag(DynamicTagKey<R> key) {
        return this.tags.computeIfAbsent(key.id(), id -> new DynamicHolderSet.Named<>(this, key));
    }

    @Nullable
    public DynamicHolderSet.Named<R> getTag(DynamicTagKey<R> key) {
        return this.tags.get(key.id());
    }

    public void bindTags(Map<Identifier, List<Identifier>> resolvedTags) {
        for (DynamicHolderSet.Named<R> named : this.tags.values()) {
            named.unbind();
        }
        for (Map.Entry<Identifier, List<Identifier>> entry : resolvedTags.entrySet()) {
            DynamicTagKey<R> key = DynamicTagKey.create(this, entry.getKey());
            DynamicHolderSet.Named<R> named = this.getOrCreateTag(key);
            List<DynamicHolder<R>> holderList = new ArrayList<>();
            for (Identifier id : entry.getValue()) {
                holderList.add(this.holder(id));
            }
            named.bind(holderList);
        }
    }

    public Set<Identifier> getKeys() {
        return this.registry.keySet();
    }

    @Nullable
    public Identifier getKey(R value) {
        for (Map.Entry<Identifier, R> entry : this.registry.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
        }
        return null;
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

    public DynamicHolder<R> holder(R value) {
        Identifier key = this.getKey(value);
        return key != null ? this.holder(key) : this.emptyHolder();
    }

    public DynamicHolder<R> emptyHolder() {
        return this.holder(DynamicHolder.EMPTY);
    }

    public com.mojang.serialization.Codec<DynamicHolder<R>> holderCodec() {
        return Identifier.CODEC.xmap(this::holder, DynamicHolder::getId);
    }

    public net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, DynamicHolder<R>> holderStreamCodec() {
        return Identifier.STREAM_CODEC.map(this::holder, DynamicHolder::getId);
    }

    public synchronized void addCallback(Runnable callback) {
        this.callbacks.add(callback);
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> prepared = new HashMap<>();
        Set<String> searchPaths = new java.util.LinkedHashSet<>();
        searchPaths.add(this.id.getNamespace() + "/" + this.id.getPath());
        if (this.path != null && !this.path.equals(this.id.getPath())) {
            searchPaths.add(this.path);
        }
        searchPaths.add(this.id.getPath());

        for (String searchPath : searchPaths) {
            FileToIdConverter converter = FileToIdConverter.json(searchPath);
            for (Map.Entry<Identifier, Resource> entry : converter.listMatchingResources(manager).entrySet()) {
                Identifier fullPath = entry.getKey();
                Identifier resourceId = converter.fileToId(fullPath);

                if (resourceId.getPath().startsWith(this.id.getNamespace() + "/")) {
                    resourceId = Identifier.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath().substring(this.id.getNamespace().length() + 1));
                }

                if (prepared.containsKey(resourceId)) {
                    continue;
                }

                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);
                    prepared.put(resourceId, json);
                } catch (Exception e) {
                    this.logger.error("Failed to parse JSON file '{}' for dynamic registry '{}': {}", fullPath, this.id, e.getMessage(), e);
                }
            }
        }
        this.logger.info("Dynamic registry '{}' discovered {} entries from datapacks across search paths {}", this.id, prepared.size(), searchPaths);
        return prepared;
    }

    protected static volatile net.minecraft.core.HolderLookup.Provider activeLookup = null;
    private static volatile java.util.function.Supplier<net.minecraft.core.HolderLookup.Provider> clientLookupSupplier = null;

    public static void setActiveLookup(net.minecraft.core.HolderLookup.Provider lookup) {
        activeLookup = lookup;
    }

    public synchronized void registerForTesting(Identifier id, R value) {
        Map<Identifier, R> next = new HashMap<>(this.registry);
        next.put(id, value);
        this.commitReload(ImmutableMap.copyOf(next), ReloadType.SERVER);
    }

    public static void setClientLookupSupplier(java.util.function.Supplier<net.minecraft.core.HolderLookup.Provider> supplier) {
        clientLookupSupplier = supplier;
    }

    public static void clearActiveLookup() {
        activeLookup = null;
    }

    public static net.minecraft.core.HolderLookup.Provider getActiveLookup() {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            if (activeLookup != null) {
                return activeLookup;
            }
            return server.registryAccess();
        }
        if (clientLookupSupplier != null) {
            try {
                var lookup = clientLookupSupplier.get();
                if (lookup != null) {
                    return lookup;
                }
            } catch (Throwable ignored) {
                // Client not initialized or running in dedicated server environment
            }
        }
        if (activeLookup != null) {
            return activeLookup;
        }
        return net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
    }

    protected com.mojang.serialization.DynamicOps<JsonElement> createOps() {
        return net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, getActiveLookup());
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        this.beginReload(ReloadType.SERVER);
        this.logger.info("Applying dynamic registry '{}' ({} entries prepared)", this.id, prepared.size());

        Map<Identifier, R> staged = new HashMap<>();
        List<String> errors = new ArrayList<>();
        com.mojang.serialization.DynamicOps<JsonElement> ops = this.createOps();

        // Atomic validation & deserialization
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            Identifier entryId = entry.getKey();
            JsonElement json = entry.getValue();

            DataResult<R> result = this.serializer.codec().parse(ops, json);
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

        boolean generationValid = true;
        try {
            this.validateGeneration(staged, ReloadType.SERVER);
        } catch (Exception e) {
            generationValid = false;
            String errorMsg = "Whole-generation validation failed for registry '" + this.id + "': " + e.getMessage();
            errors.add(errorMsg);
            this.logger.error(errorMsg, e);
        }

        // Actionable validation error handling without partial corruption
        if (!errors.isEmpty()) {
            this.logger.error("Dynamic registry '{}' encountered {} validation error(s).", this.id, errors.size());
            if (!generationValid && !this.registry.isEmpty()) {
                this.logger.error("Preserving previous valid generation for registry '{}' ({} entries) due to critical validation errors.", this.id, this.registry.size());
                return;
            }
            this.logger.warn("Dynamic registry '{}': applying {} valid entries and logging diagnostics.", this.id, staged.size());
        }

        // Commit atomically
        this.commitReload(ImmutableMap.copyOf(staged));
    }

    protected void validateGeneration(Map<Identifier, R> staged, ReloadType type) {
        // Subclasses may implement whole-generation or cross-entry validation
    }

    protected void validateItem(Identifier id, R item) {
        // Subclasses may implement custom semantic validation
    }

    protected synchronized void commitReload(Map<Identifier, R> newEntries, ReloadType reloadType) {
        // 1. Swap registry map atomically
        this.registry = newEntries;

        // 2. Invalidate / rebind holders
        for (DynamicHolder<R> holder : this.holders.values()) {
            holder.unbind();
            holder.bind();
        }
        for (DynamicHolderSet.Named<R> named : this.tags.values()) {
            named.unbind();
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
        this.onReload(reloadType);
        this.logger.info("Dynamic registry '{}' reload complete ({} entries active)", this.id, this.registry.size());
    }

    protected synchronized void commitReload(Map<Identifier, R> newEntries) {
        this.commitReload(newEntries, ReloadType.SERVER);
    }

    public enum ReloadType {
        SERVER,
        CLIENT,
        INTEGRATED_CLIENT
    }

    protected void beginReload(ReloadType type) {
    }

    protected void onReload(ReloadType type) {
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
        Map<Identifier, List<Identifier>> tagMap = new HashMap<>();
        for (Map.Entry<Identifier, DynamicHolderSet.Named<R>> entry : this.tags.entrySet()) {
            if (entry.getValue().isBound()) {
                tagMap.put(entry.getKey(), entry.getValue().stream().map(DynamicHolder::getId).toList());
            }
        }
        if (!tagMap.isEmpty()) {
            PayloadHelper.sendToPlayer(player, new DynRegPayloads.Tags(this.id, tagMap));
        }

        PayloadHelper.sendToPlayer(player, new DynRegPayloads.End(this.id));
    }

    private final Map<Identifier, List<Identifier>> clientStagedTags = new HashMap<>();

    public synchronized void initClientSync() {
        this.clientStaged.clear();
        this.clientStagedTags.clear();
    }

    public synchronized void acceptClientTags(Map<Identifier, List<Identifier>> tags) {
        this.clientStagedTags.clear();
        this.clientStagedTags.putAll(tags);
    }

    public synchronized void acceptClientContent(Identifier entryId, JsonElement json) {
        if (net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
            return;
        }
        DataResult<R> result = this.serializer.codec().parse(this.createOps(), json);
        if (result.isSuccess()) {
            this.clientStaged.put(entryId, result.getOrThrow());
        } else {
            this.logger.error("Failed to decode client sync element '{}' in registry '{}': {}", entryId, this.id, result.error().get().message());
        }
    }

    public synchronized void completeClientSync() {
        if (net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
            this.clientStaged.clear();
            this.clientStagedTags.clear();
            this.logger.info("Integrated client sync preserved authoritative server registry for '{}'", this.id);
            return;
        }
        this.beginReload(ReloadType.CLIENT);
        this.commitReload(ImmutableMap.copyOf(this.clientStaged), ReloadType.CLIENT);
        if (!this.clientStagedTags.isEmpty()) {
            this.bindTags(this.clientStagedTags);
            this.clientStagedTags.clear();
        }
        this.clientStaged.clear();
        this.logger.info("Client synchronization complete for dynamic registry '{}'", this.id);
    }
}
