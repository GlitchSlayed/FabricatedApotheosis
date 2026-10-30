package dev.shadowsoffire.placebo.cap;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * An internal inventory implementation that satisfies vanilla {@link Container}
 * and provides item handler-like operations and Fabric Transfer API storage via {@link #asStorage()}.
 */
public class InternalItemHandler extends SnapshotParticipant<NonNullList<ItemStack>> implements Container {

    protected final NonNullList<ItemStack> items;
    protected int slotLimit = 64;

    public InternalItemHandler(int size) {
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public InternalItemHandler(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    public int size() {
        return this.items.size();
    }

    public int getSlots() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        validateSlotIndex(slot);
        return this.items.get(slot);
    }

    public ItemStack getStackInSlot(int slot) {
        return getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        validateSlotIndex(slot);
        ItemStack current = this.items.get(slot);
        if (current.isEmpty() || amount <= 0) return ItemStack.EMPTY;

        ItemStack result = current.split(amount);
        if (current.isEmpty()) {
            this.items.set(slot, ItemStack.EMPTY);
        }
        onContentsChanged(slot, current);
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        validateSlotIndex(slot);
        ItemStack current = this.items.get(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        this.items.set(slot, ItemStack.EMPTY);
        onContentsChanged(slot, current);
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        ItemStack previous = this.items.get(slot);
        this.items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize(slot, stack)) {
            stack.setCount(getMaxStackSize(slot, stack));
        }
        onContentsChanged(slot, previous);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.items.size(); i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }

    public boolean isValid(int slot, ItemStack stack) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    public int getSlotLimit(int slot) {
        return this.slotLimit;
    }

    public int getMaxStackSize(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    public void setSlotLimit(int limit) {
        this.slotLimit = limit;
    }

    protected void onContentsChanged(int slot, ItemStack previousContents) {
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.items.size()) {
            throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + this.items.size() + ")");
        }
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        validateSlotIndex(slot);
        if (!canPlaceItem(slot, stack)) return stack;

        ItemStack existing = this.items.get(slot);
        int limit = getMaxStackSize(slot, stack);

        if (existing.isEmpty()) {
            int toInsert = Math.min(stack.getCount(), limit);
            if (!simulate) {
                ItemStack copy = stack.copyWithCount(toInsert);
                this.items.set(slot, copy);
                onContentsChanged(slot, ItemStack.EMPTY);
            }
            return stack.getCount() > toInsert ? stack.copyWithCount(stack.getCount() - toInsert) : ItemStack.EMPTY;
        }

        if (ItemStack.isSameItemSameComponents(existing, stack)) {
            int space = limit - existing.getCount();
            if (space <= 0) return stack;

            int toInsert = Math.min(stack.getCount(), space);
            if (!simulate) {
                existing.grow(toInsert);
                onContentsChanged(slot, existing);
            }
            return stack.getCount() > toInsert ? stack.copyWithCount(stack.getCount() - toInsert) : ItemStack.EMPTY;
        }

        return stack;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        validateSlotIndex(slot);

        ItemStack existing = this.items.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(toExtract);

        if (!simulate) {
            existing.shrink(toExtract);
            if (existing.isEmpty()) {
                this.items.set(slot, ItemStack.EMPTY);
            }
            onContentsChanged(slot, existing);
        }

        return result;
    }


    public void save(ValueOutput out) {
        ContainerHelper.saveAllItems(out, this.items);
    }

    public void load(ValueInput in) {
        ContainerHelper.loadAllItems(in, this.items);
    }

    // Fabric Transfer API implementation
    @Override
    protected NonNullList<ItemStack> createSnapshot() {
        NonNullList<ItemStack> snapshot = NonNullList.withSize(this.items.size(), ItemStack.EMPTY);
        for (int i = 0; i < this.items.size(); i++) {
            snapshot.set(i, this.items.get(i).copy());
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(NonNullList<ItemStack> snapshot) {
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, snapshot.get(i).copy());
        }
    }

    public Storage<ItemVariant> asStorage() {
        return ContainerStorage.of(this, null);
    }

    @Override
    public java.util.Iterator<ItemStack> iterator() {
        return this.items.iterator();
    }
}
