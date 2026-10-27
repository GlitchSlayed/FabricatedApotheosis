package dev.shadowsoffire.placebo.dynreg;

import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Network payloads used to synchronize dynamic registry data to connecting clients.
 */
public class DynRegPayloads {

    public record Start(Identifier registryId) implements CustomPacketPayload {
        public static final Type<Start> TYPE = new Type<>(Placebo.loc("dynreg_sync_start"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Start> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, Start::registryId,
            Start::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<Start> {
            @Override
            public Type<Start> getType() { return TYPE; }
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Start> getCodec() { return CODEC; }
            @Override
            public void handleClient(Start msg, Player player) {
                DynamicRegistry<?> reg = DynamicRegistry.getRegistry(msg.registryId());
                if (reg != null) {
                    reg.initClientSync();
                }
            }
        }
    }

    public record Content(Identifier registryId, Identifier entryId, String jsonString) implements CustomPacketPayload {
        public static final Type<Content> TYPE = new Type<>(Placebo.loc("dynreg_sync_content"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Content> CODEC = StreamCodec.of(
            (buf, msg) -> {
                Identifier.STREAM_CODEC.encode(buf, msg.registryId());
                Identifier.STREAM_CODEC.encode(buf, msg.entryId());
                byte[] bytes = msg.jsonString().getBytes(StandardCharsets.UTF_8);
                buf.writeVarInt(bytes.length);
                buf.writeBytes(bytes);
            },
            buf -> {
                Identifier regId = Identifier.STREAM_CODEC.decode(buf);
                Identifier entryId = Identifier.STREAM_CODEC.decode(buf);
                int len = buf.readVarInt();
                byte[] bytes = new byte[len];
                buf.readBytes(bytes);
                return new Content(regId, entryId, new String(bytes, StandardCharsets.UTF_8));
            }
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<Content> {
            @Override
            public Type<Content> getType() { return TYPE; }
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Content> getCodec() { return CODEC; }
            @Override
            public void handleClient(Content msg, Player player) {
                DynamicRegistry<?> reg = DynamicRegistry.getRegistry(msg.registryId());
                if (reg != null) {
                    try {
                        JsonElement json = JsonParser.parseString(msg.jsonString());
                        reg.acceptClientContent(msg.entryId(), json);
                    } catch (Exception e) {
                        Placebo.LOGGER.error("Failed to parse synced registry element {} in registry {}", msg.entryId(), msg.registryId(), e);
                    }
                }
            }
        }
    }

    public record End(Identifier registryId) implements CustomPacketPayload {
        public static final Type<End> TYPE = new Type<>(Placebo.loc("dynreg_sync_end"));
        public static final StreamCodec<RegistryFriendlyByteBuf, End> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, End::registryId,
            End::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<End> {
            @Override
            public Type<End> getType() { return TYPE; }
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, End> getCodec() { return CODEC; }
            @Override
            public void handleClient(End msg, Player player) {
                DynamicRegistry<?> reg = DynamicRegistry.getRegistry(msg.registryId());
                if (reg != null) {
                    reg.completeClientSync();
                }
            }
        }
    }
}
