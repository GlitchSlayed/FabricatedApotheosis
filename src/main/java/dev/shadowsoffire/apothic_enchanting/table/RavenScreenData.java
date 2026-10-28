package dev.shadowsoffire.apothic_enchanting.table;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RavenScreenData(BlockPos pos, int eterna, int quanta, int arcana) {
    public static final StreamCodec<RegistryFriendlyByteBuf, RavenScreenData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RavenScreenData::pos,
        ByteBufCodecs.VAR_INT, RavenScreenData::eterna,
        ByteBufCodecs.VAR_INT, RavenScreenData::quanta,
        ByteBufCodecs.VAR_INT, RavenScreenData::arcana,
        RavenScreenData::new
    );
}
