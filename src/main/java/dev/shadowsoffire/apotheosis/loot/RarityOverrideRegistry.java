package dev.shadowsoffire.apotheosis.loot;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;
import net.minecraft.resources.Identifier;

public class RarityOverrideRegistry extends DynamicRegistry<RarityOverride> {

    public static final RarityOverrideRegistry INSTANCE = new RarityOverrideRegistry();

    protected Map<LootCategory, RarityOverride> byCategory = new HashMap<>();

    public RarityOverrideRegistry() {
        super(Apotheosis.LOGGER, Apotheosis.loc("rarity_override"), RegistrySerializer.synced(RarityOverride.CODEC));
    }

    @Nullable
    public RarityOverride getOverride(LootCategory category) {
        return this.byCategory.get(category);
    }

    @Override
    protected void validateItem(Identifier key, RarityOverride value) {
        String path = key.getPath().replace('/', ':');
        Identifier cat = path.contains(":") ? Identifier.tryParse(path) : Identifier.fromNamespaceAndPath(key.getNamespace(), path);
        Preconditions.checkNotNull(cat, "Invalid category path: " + path);
        LootCategory category = BuiltInRegs.LOOT_CATEGORY.getValue(cat);
        if (category == null || category.isNone()) {
            category = value.category();
        }
        Preconditions.checkArgument(category != null && !category.isNone(), "Category not found: " + cat);
        Preconditions.checkArgument(value.category() == category, "Category mismatch: " + value.category() + " != " + category);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.byCategory = new HashMap<>();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        this.byCategory = new HashMap<>();
        this.registry.forEach((key, value) -> {
            LootCategory category = value.category();
            if (category == null || category.isNone()) {
                String path = key.getPath().replace('/', ':');
                Identifier cat = path.contains(":") ? Identifier.tryParse(path) : Identifier.fromNamespaceAndPath(key.getNamespace(), path);
                category = BuiltInRegs.LOOT_CATEGORY.getValue(cat);
            }
            if (category != null && !category.isNone()) {
                RarityOverride old = this.byCategory.put(category, value);
                if (old != null) {
                    this.logger.warn("Duplicate rarity override for category {}: Old: {}, New: {}", category, this.getKey(old), key);
                }
            }
        });
    }

}
