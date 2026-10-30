package dev.shadowsoffire.apotheosis.affix.salvaging;

import java.util.Iterator;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SalvagingTableTile extends BlockEntity implements Clearable {

    public SalvagingTableTile(BlockPos pPos, BlockState pBlockState) {
        super(Apoth.Tiles.SALVAGING_TABLE, pPos, pBlockState);
    }

    /**
     * "Real" output inventory, as reflected in the container menu.
     */
    protected final InternalItemHandler output = new InternalItemHandler(6);
    protected final Storage<ItemVariant> itemHandler = new SalvagingItemHandler();

    public Storage<ItemVariant> getItemHandler() {
        return this.itemHandler;
    }

    public InternalItemHandler getOutput() {
        return this.output;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        this.output.save(out.child("output"));
    }

    @Override
    public void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("output").ifPresent(this.output::load);
    }

    @Override
    public void clearContent() {
        this.output.clearContent();
    }

    protected class SalvagingItemHandler implements Storage<ItemVariant> {

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0 || resource.isBlank() || SalvagingTableTile.this.level == null) {
                return 0;
            }

            ItemStack inStack = resource.toStack(1);
            List<ItemStack> outputs = SalvagingMenu.getSalvageResults(SalvagingTableTile.this.level, inStack);
            if (outputs.isEmpty()) {
                return 0;
            }

            try (Transaction nested = Transaction.openNested(transaction)) {
                for (ItemStack out : outputs) {
                    ItemVariant outVariant = ItemVariant.of(out);
                    long inserted = SalvagingTableTile.this.output.asStorage().insert(outVariant, out.getCount(), nested);
                    if (inserted < out.getCount()) {
                        return 0; // Could not fit all outputs
                    }
                }
                nested.commit();
            }
            return 1;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return SalvagingTableTile.this.output.asStorage().extract(resource, maxAmount, transaction);
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return SalvagingTableTile.this.output.asStorage().iterator();
        }
    }
}
