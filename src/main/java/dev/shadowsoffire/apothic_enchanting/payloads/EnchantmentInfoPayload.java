package dev.shadowsoffire.apothic_enchanting.payloads;

import java.util.HashMap;
import java.util.Map;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.EnchantmentInfo;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EnchantmentInfoPayload(Map<Identifier, EnchantmentInfo> infoMap) implements CustomPacketPayload {

    public static final Type<EnchantmentInfoPayload> TYPE = new Type<>(ApothicEnchanting.loc("enchantment_info"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentInfoPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.fromCodec(EnchantmentInfo.CODEC.codec())),
        EnchantmentInfoPayload::infoMap,
        EnchantmentInfoPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<EnchantmentInfoPayload> {

        @Override
        public Type<EnchantmentInfoPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, EnchantmentInfoPayload> getCodec() {
            return CODEC;
        }
    }
}
