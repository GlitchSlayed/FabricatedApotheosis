package dev.shadowsoffire.placebo.dynreg;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;

/**
 * An extension of {@link DynamicRegistry} with support for weighted random selection.
 *
 * @param <V> Item type implementing {@link ILuckyWeighted}.
 */
public abstract class WeightedDynamicRegistry<V extends WeightedDynamicRegistry.ILuckyWeighted> extends DynamicRegistry<V> {

    public interface ILuckyWeighted {
        int getWeight();
        float getQuality();
    }

    protected volatile WeightedList<V> zeroLuckList = WeightedList.of();

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
}
