package dev.shadowsoffire.placebo.dynreg;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

import dev.shadowsoffire.placebo.dynreg.WeightedDynamicRegistry.ILuckyWeighted;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicHolderSet;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicTagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.Level;

/**
 * An extension of {@link DynamicRegistry} with support for weighted random selection.
 *
 * @param <V> Item type implementing {@link ILuckyWeighted}.
 */
public abstract class WeightedDynamicRegistry<V extends ILuckyWeighted> extends DynamicRegistry<V> {

    protected volatile WeightedList<V> zeroLuckList = WeightedList.of();

    public WeightedDynamicRegistry(Logger logger, Identifier id, RegistrySerializer<V> serializer) {
        super(logger, id, serializer);
    }

    public WeightedDynamicRegistry(Logger logger, Identifier id, String path, RegistrySerializer<V> serializer) {
        super(logger, id, path, serializer);
    }

    @Override
    protected void validateItem(Identifier key, V item) {
        super.validateItem(key, item);
        Preconditions.checkArgument(item.getQuality() >= 0, "Quality may not be negative for " + key);
        Preconditions.checkArgument(item.getWeight() >= 0, "Weight may not be negative for " + key);
    }

    @Override
    protected void onReload() {
        super.onReload();
        WeightedList.Builder<V> builder = WeightedList.builder();
        for (V item : this.registry.values()) {
            if (item.getWeight() > 0) {
                builder.add(item, item.getWeight());
            }
        }
        this.zeroLuckList = builder.build();
    }

    public WeightedList<V> getZeroLuckList() {
        return this.zeroLuckList;
    }

    @Nullable
    public V getRandomItem(RandomSource rand) {
        return this.getRandomItem(rand, 0);
    }

    @Nullable
    public V getRandomItem(RandomSource rand, float luck) {
        if (luck == 0) {
            return this.zeroLuckList.getRandom(rand).orElse(null);
        }
        return this.getRandomItem(rand, luck, Predicates.alwaysTrue());
    }

    @Nullable
    @SafeVarargs
    public final V getRandomItem(RandomSource rand, float luck, Predicate<V>... filters) {
        WeightedList.Builder<V> builder = WeightedList.builder();
        Stream<V> stream = this.registry.values().stream();
        for (Predicate<V> filter : filters) {
            stream = stream.filter(filter);
        }
        stream.forEach(item -> {
            int weight = Math.max(0, item.getWeight() + (int) (luck * item.getQuality()));
            if (weight > 0) {
                builder.add(item, weight);
            }
        });
        return builder.build().getRandom(rand).orElse(null);
    }

    @Nullable
    public V getRandomFromTag(DynamicTagKey<V> tag, RandomSource rand, float luck) {
        DynamicHolderSet.Named<V> set = this.getTag(tag);
        return set != null ? this.getRandomFromSet(set, rand, luck) : null;
    }

    @Nullable
    public V getRandomFromSet(DynamicHolderSet<V> set, RandomSource rand, float luck) {
        return this.getRandomItem(rand, luck, set::contains);
    }

    public static interface ILuckyWeighted {
        public float getQuality();
        public int getWeight();

        default <T extends ILuckyWeighted> Weighted<T> wrap(float luck) {
            return wrap((T) this, luck);
        }

        static <T extends ILuckyWeighted> Weighted<T> wrap(T item, float luck) {
            return new Weighted<>(item, Math.max(0, item.getWeight() + (int) (luck * item.getQuality())));
        }
    }

    public static interface IDimensional {
        @Nullable
        Set<Identifier> getDimensions();

        public static <T extends IDimensional> Predicate<T> createPredicate(Identifier dimId) {
            return obj -> {
                Set<Identifier> dims = obj.getDimensions();
                return dims == null || dims.isEmpty() || dims.contains(dimId);
            };
        }

        public static <T extends IDimensional> Predicate<T> matches(Level level) {
            return createPredicate(level.dimension().identifier());
        }
    }
}
