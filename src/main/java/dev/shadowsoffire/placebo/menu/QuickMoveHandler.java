package dev.shadowsoffire.placebo.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class QuickMoveHandler {

    protected List<QuickMoveRule> rules = new ArrayList<>();

    public ItemStack quickMoveStack(QuickMoveMenu container, Player player, int index) {
        if (this.rules.isEmpty()) {
            throw new RuntimeException("Quick Move requires at least one rule to be registered");
        }
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot slot = container.getSlot(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            slotStackCopy = slotStack.copy();
            boolean matched = false;
            for (QuickMoveRule rule : this.rules) {
                if (rule.req.test(slotStack, index) && slot.mayPickup(player)) {
                    if (!container.moveItemStackTo(slotStack, rule.startIdx, rule.endIdx, rule.reversed)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onTake(player, slotStack);
                    container.onQuickMove(slotStackCopy, slotStack, slot);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return slotStackCopy;
    }

    public void registerRule(BiPredicate<ItemStack, Integer> req, int startIdx, int endIdx, boolean reversed) {
        this.rules.add(new QuickMoveRule(req, startIdx, endIdx, reversed));
    }

    public void registerRule(BiPredicate<ItemStack, Integer> req, int startIdx, int endIdx) {
        this.registerRule(req, startIdx, endIdx, false);
    }

    protected record QuickMoveRule(BiPredicate<ItemStack, Integer> req, int startIdx, int endIdx, boolean reversed) {
    }

    public interface QuickMoveMenu {
        boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection);
        Slot getSlot(int index);
        default void onQuickMove(ItemStack original, ItemStack remaining, Slot slot) {
            slot.setChanged();
        }
    }
}
