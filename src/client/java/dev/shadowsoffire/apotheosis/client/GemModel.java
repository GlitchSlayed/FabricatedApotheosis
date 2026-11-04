package dev.shadowsoffire.apotheosis.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Dispatches rendering of gem items to a per-gem baked item model based on the ID of the {@link Gem}
 * stored on the stack via {@link Components#GEM}.
 */
public class GemModel implements ItemModel {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GemModel.class);

    public static final List<String> BUILTIN_GEMS = List.of(
        "core/ballast",
        "core/brawlers",
        "core/breach",
        "core/combatant",
        "core/guardian",
        "core/lightning",
        "core/lunar",
        "core/samurai",
        "core/slipstream",
        "core/solar",
        "core/splendor",
        "core/tyrannical",
        "core/warlord",
        "overworld/earth",
        "overworld/royalty",
        "overworld/verdant_ruin",
        "the_end/endersurge",
        "the_end/mageslayer",
        "the_nether/blood_lord",
        "the_nether/inferno",
        "the_nether/molten_breach",
        "twilight/forest",
        "twilight/queen"
    );

    private final ItemModel fallback;
    private final Map<Identifier, ItemModel> gemModels;

    public GemModel(ItemModel fallback, Map<Identifier, ItemModel> gemModels) {
        this.fallback = fallback;
        this.gemModels = gemModels;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
        ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        DynamicHolder<Gem> gem = GemItem.getGem(stack);
        if (gem.isBound()) {
            ItemModel gemModel = this.gemModels.get(gem.getId());
            if (gemModel != null) {
                gemModel.update(output, stack, resolver, displayContext, level, owner, seed);
                return;
            }
        }
        this.fallback.update(output, stack, resolver, displayContext, level, owner, seed);
    }

    public static void init() {
        try {
            ItemModels.ID_MAPPER.put(Apotheosis.loc("gem_dispatch"), GemModel.Unbaked.MAP_CODEC);
        }
        catch (Throwable ignored) {
        }
    }

    public record Unbaked(ItemModel.Unbaked fallback, Map<Identifier, ItemModel.Unbaked> gemModels) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ItemModels.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback),
            com.mojang.serialization.Codec.unboundedMap(Identifier.CODEC, ItemModels.CODEC).optionalFieldOf("models", Map.of()).forGetter(Unbaked::gemModels)
        ).apply(i, Unbaked::create));

        public static Unbaked create(ItemModel.Unbaked fallback, Map<Identifier, ItemModel.Unbaked> models) {
            Map<Identifier, ItemModel.Unbaked> merged = new HashMap<>();
            for (String gemPath : BUILTIN_GEMS) {
                Identifier gemId = Apotheosis.loc(gemPath);
                Identifier modelId = Apotheosis.loc("item/gems/" + gemPath);
                merged.put(gemId, new CuboidItemModelWrapper.Unbaked(modelId, Optional.empty(), List.of()));
            }
            merged.putAll(models);
            return new Unbaked(fallback, merged);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ItemModel bakedFallback = this.fallback.bake(context, transformation);
            Map<Identifier, ItemModel> baked = new HashMap<>();
            for (Map.Entry<Identifier, ItemModel.Unbaked> entry : this.gemModels.entrySet()) {
                try {
                    baked.put(entry.getKey(), entry.getValue().bake(context, transformation));
                }
                catch (Exception e) {
                    LOGGER.warn("Failed to bake gem model for '{}': {}", entry.getKey(), e.getMessage());
                }
            }
            return new GemModel(bakedFallback, baked);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.fallback.resolveDependencies(resolver);
            for (ItemModel.Unbaked gemModel : this.gemModels.values()) {
                gemModel.resolveDependencies(resolver);
            }
        }
    }
}
