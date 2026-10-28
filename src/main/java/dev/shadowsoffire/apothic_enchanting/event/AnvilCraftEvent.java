package dev.shadowsoffire.apothic_enchanting.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AnvilCraftEvent {

    public static class Post {
        private final ItemStack left;
        private final ItemStack right;
        private final ItemStack output;
        private final Player player;

        public Post(ItemStack left, ItemStack right, ItemStack output, Player player) {
            this.left = left;
            this.right = right;
            this.output = output;
            this.player = player;
        }

        public ItemStack getLeft() {
            return this.left;
        }

        public ItemStack getRight() {
            return this.right;
        }

        public ItemStack getOutput() {
            return this.output;
        }

        public Player getEntity() {
            return this.player;
        }

        public Player getPlayer() {
            return this.player;
        }
    }
}
