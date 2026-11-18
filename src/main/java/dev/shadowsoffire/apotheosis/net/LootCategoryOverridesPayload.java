package dev.shadowsoffire.apotheosis.net;

import java.util.HashMap;
import java.util.Map;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public record LootCategoryOverridesPayload(Map<Identifier, LootCategory> overrides) implements CustomPacketPayload {

    public static final Type<LootCategoryOverridesPayload> TYPE = new Type<>(Apotheosis.loc("loot_category_overrides"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LootCategoryOverridesPayload> CODEC = ByteBufCodecs.map(
        HashMap::new,
        Identifier.STREAM_CODEC,
        LootCategory.STREAM_CODEC
    ).map(LootCategoryOverridesPayload::new, p -> new HashMap<>(p.overrides()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static LootCategoryOverridesPayload fromActiveOverrides() {
        Map<Identifier, LootCategory> map = new HashMap<>();
        for (Map.Entry<Item, LootCategory> entry : LootCategory.getOverrides().entrySet()) {
            Identifier key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(entry.getKey());
            if (key != null) {
                map.put(key, entry.getValue());
            }
        }
        return new LootCategoryOverridesPayload(map);
    }

    public static class Provider implements PayloadProvider<LootCategoryOverridesPayload> {

        @Override
        public Type<LootCategoryOverridesPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, LootCategoryOverridesPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(LootCategoryOverridesPayload msg, Player player) {
            Map<Item, LootCategory> resolved = new HashMap<>();
            var itemReg = net.minecraft.core.registries.BuiltInRegistries.ITEM;
            for (Map.Entry<Identifier, LootCategory> entry : msg.overrides().entrySet()) {
                itemReg.getOptional(entry.getKey()).ifPresent(item -> resolved.put(item, entry.getValue()));
            }
            LootCategory.replaceOverrides(resolved);
        }
    }
}
