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
        public static final int MAX_JSON_BYTES = 2 * 1024 * 1024;
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
                if (len < 0 || len > MAX_JSON_BYTES || len > buf.readableBytes()) {
                    throw new io.netty.handler.codec.DecoderException("Invalid json payload byte length: " + len + ", readable: " + buf.readableBytes());
                }
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

    public record Tags(Identifier registryId, java.util.Map<Identifier, java.util.List<Identifier>> tags) implements CustomPacketPayload {
        public static final Type<Tags> TYPE = new Type<>(Placebo.loc("dynreg_sync_tags"));
        public static final int MAX_TAG_COUNT = 10_000;
        public static final int MAX_ENTRIES_PER_TAG = 10_000;
        public static final int MAX_TOTAL_TAG_ENTRIES = 50_000;

        public static final StreamCodec<RegistryFriendlyByteBuf, Tags> CODEC = StreamCodec.of(
            (buf, msg) -> {
                if (msg.tags().size() > MAX_TAG_COUNT) {
                    throw new IllegalArgumentException("Cannot encode Tags payload with " + msg.tags().size() + " tags (max " + MAX_TAG_COUNT + ")");
                }
                buf.writeIdentifier(msg.registryId());
                buf.writeVarInt(msg.tags().size());
                long total = 0;
                for (java.util.Map.Entry<Identifier, java.util.List<Identifier>> entry : msg.tags().entrySet()) {
                    if (entry.getValue().size() > MAX_ENTRIES_PER_TAG) {
                        throw new IllegalArgumentException("Tag " + entry.getKey() + " has " + entry.getValue().size() + " entries (max " + MAX_ENTRIES_PER_TAG + ")");
                    }
                    total += entry.getValue().size();
                    if (total > MAX_TOTAL_TAG_ENTRIES) {
                        throw new IllegalArgumentException("Tags payload total entries exceed limit: " + total + " > " + MAX_TOTAL_TAG_ENTRIES);
                    }
                    buf.writeIdentifier(entry.getKey());
                    buf.writeVarInt(entry.getValue().size());
                    for (Identifier id : entry.getValue()) {
                        buf.writeIdentifier(id);
                    }
                }
            },
            buf -> {
                Identifier regId = buf.readIdentifier();
                int tagCount = buf.readVarInt();
                if (tagCount < 0 || tagCount > MAX_TAG_COUNT) {
                    throw new IllegalArgumentException("Invalid tagCount: " + tagCount + " (max " + MAX_TAG_COUNT + ")");
                }
                int estimatedMinBytes = tagCount * 2;
                if (buf.readableBytes() < estimatedMinBytes) {
                    throw new IllegalArgumentException("Malformed tag packet: requires at least " + estimatedMinBytes + " bytes, but only " + buf.readableBytes() + " available");
                }
                int capacity = Math.min(tagCount, 1024);
                java.util.Map<Identifier, java.util.List<Identifier>> tags = new java.util.HashMap<>(capacity);
                long totalEntries = 0;
                for (int i = 0; i < tagCount; i++) {
                    Identifier tagId = buf.readIdentifier();
                    int entryCount = buf.readVarInt();
                    if (entryCount < 0 || entryCount > MAX_ENTRIES_PER_TAG) {
                        throw new IllegalArgumentException("Invalid entryCount " + entryCount + " for tag " + tagId);
                    }
                    totalEntries += entryCount;
                    if (totalEntries > MAX_TOTAL_TAG_ENTRIES) {
                        throw new IllegalArgumentException("Total tag entries exceed maximum limit: " + totalEntries + " > " + MAX_TOTAL_TAG_ENTRIES);
                    }
                    if (buf.readableBytes() < entryCount * 2) {
                        throw new IllegalArgumentException("Malformed tag packet: not enough readable bytes for " + entryCount + " entries in tag " + tagId);
                    }
                    int entryCapacity = Math.min(entryCount, 1024);
                    java.util.List<Identifier> entries = new java.util.ArrayList<>(entryCapacity);
                    for (int j = 0; j < entryCount; j++) {
                        entries.add(buf.readIdentifier());
                    }
                    tags.put(tagId, entries);
                }
                return new Tags(regId, tags);
            }
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<Tags> {
            @Override
            public Type<Tags> getType() { return TYPE; }
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Tags> getCodec() { return CODEC; }
            @Override
            public void handleClient(Tags msg, Player player) {
                DynamicRegistry<?> reg = DynamicRegistry.getRegistry(msg.registryId());
                if (reg != null) {
                    reg.acceptClientTags(msg.tags());
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
