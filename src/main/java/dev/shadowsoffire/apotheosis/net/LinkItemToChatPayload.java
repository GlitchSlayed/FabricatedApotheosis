package dev.shadowsoffire.apotheosis.net;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.ItemLinking;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record LinkItemToChatPayload(int containerId, int slot, Item intendedItem) implements CustomPacketPayload {

    public static final Type<LinkItemToChatPayload> TYPE = new Type<>(Apotheosis.loc("link_item_to_chat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LinkItemToChatPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, LinkItemToChatPayload::containerId,
        ByteBufCodecs.VAR_INT, LinkItemToChatPayload::slot,
        ByteBufCodecs.registry(Registries.ITEM), LinkItemToChatPayload::intendedItem,
        LinkItemToChatPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<LinkItemToChatPayload> {

        @Override
        public Type<LinkItemToChatPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, LinkItemToChatPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleServer(LinkItemToChatPayload msg, ServerPlayer player) {
            if (ItemLinking.isOnCooldown(player.getUUID(), player.level().getGameTime())) {
                return;
            }

            if (!AdventureConfig.enableItemLinking) {
                player.sendSystemMessage(Apotheosis.lang("message", "item_linking_disabled"));
                return;
            }

            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId == msg.containerId && menu.slots.size() > msg.slot) {
                Slot slot = menu.getSlot(msg.slot);
                ItemStack stack = slot.getItem();

                int count = stack.getCount();
                if (count > 99) {
                    stack = stack.copy();
                    stack.setCount(99);
                }

                if (stack.is(msg.intendedItem)) {
                    Component name = player.getDisplayName();
                    Component item = stack.getDisplayName();
                    Component msgComp = Apotheosis.lang("chat", "item_link", name, item);
                    player.level().getServer().getPlayerList().broadcastSystemMessage(msgComp, false);
                    ItemLinking.setCooldown(player.getUUID(), player.level().getGameTime());
                }
            }
        }
    }
}
