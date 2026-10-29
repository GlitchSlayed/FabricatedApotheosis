package dev.shadowsoffire.apothic_spawners.stats;

import java.util.function.Consumer;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public class SpawnerStats {

    public static final ResourceKey<Registry<SpawnerStat<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ApothicSpawners.loc("spawner_stat"));

    public static final Registry<SpawnerStat<?>> REGISTRY = createRegistry();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Registry<SpawnerStat<?>> createRegistry() {
        try {
            return FabricRegistryBuilder.create(REGISTRY_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();
        }
        catch (Throwable t) {
            net.minecraft.core.MappedRegistry<SpawnerStat<?>> mapped = new net.minecraft.core.MappedRegistry<>(REGISTRY_KEY, com.mojang.serialization.Lifecycle.stable());
            try {
                java.lang.reflect.Field regField = net.minecraft.core.registries.BuiltInRegistries.class.getDeclaredField("WRITABLE_REGISTRY");
                regField.setAccessible(true);
                net.minecraft.core.WritableRegistry root = (net.minecraft.core.WritableRegistry) regField.get(null);
                root.register((ResourceKey) REGISTRY_KEY, mapped, net.minecraft.core.RegistrationInfo.BUILT_IN);
            }
            catch (Exception ignored) {}
            return mapped;
        }
    }

    public static final SpawnerStat<Integer> MIN_DELAY = register("min_delay", new VanillaStat(s -> s.getSpawner().minSpawnDelay, (s, v) -> s.getSpawner().minSpawnDelay = v));

    public static final SpawnerStat<Integer> MAX_DELAY = register("max_delay", new VanillaStat(s -> s.getSpawner().maxSpawnDelay, (s, v) -> s.getSpawner().maxSpawnDelay = v));

    public static final SpawnerStat<Integer> SPAWN_COUNT = register("spawn_count", new VanillaStat(s -> s.getSpawner().spawnCount, (s, v) -> s.getSpawner().spawnCount = v));

    public static final SpawnerStat<Integer> MAX_NEARBY_ENTITIES = register("max_nearby_entities", new VanillaStat(s -> s.getSpawner().maxNearbyEntities, (s, v) -> s.getSpawner().maxNearbyEntities = v));

    public static final SpawnerStat<Integer> REQ_PLAYER_RANGE = register("req_player_range", new VanillaStat(s -> s.getSpawner().requiredPlayerRange, (s, v) -> s.getSpawner().requiredPlayerRange = v));

    public static final SpawnerStat<Integer> SPAWN_RANGE = register("spawn_range", new VanillaStat(s -> s.getSpawner().spawnRange, (s, v) -> s.getSpawner().spawnRange = v));

    public static final SpawnerStat<Float> INITIAL_HEALTH = register("initial_health", new PercentageStat(1F));

    public static final SpawnerStat<Boolean> IGNORE_PLAYERS = register("ignore_players", new BooleanStat(false));

    public static final SpawnerStat<Boolean> IGNORE_CONDITIONS = register("ignore_conditions", new BooleanStat(false));

    public static final SpawnerStat<Boolean> REDSTONE_CONTROL = register("redstone_control", new BooleanStat(false));

    public static final SpawnerStat<Boolean> IGNORE_LIGHT = register("ignore_light", new BooleanStat(false));

    public static final SpawnerStat<Boolean> NO_AI = register("no_ai", new BooleanStat(false));

    public static final SpawnerStat<Boolean> SILENT = register("silent", new BooleanStat(false));

    public static final SpawnerStat<Boolean> YOUTHFUL = register("youthful", new BooleanStat(false));

    public static final SpawnerStat<Boolean> BURNING = register("burning", new BooleanStat(false));

    public static final SpawnerStat<Integer> ECHOING = register("echoing", new LevelStat(0));

    public static void bootstrap() {}

    public static void generateTooltip(ApothSpawnerTile tile, Consumer<Component> list) {
        for (SpawnerStat<?> stat : REGISTRY) {
            Component comp = stat.getTooltip(tile);
            if (!comp.getString().isEmpty()) {
                list.accept(comp);
            }
        }
    }

    private static <T extends SpawnerStat<?>> T register(String id, T t) {
        Registry.register(REGISTRY, ApothicSpawners.loc(id), t);
        return t;
    }
}
