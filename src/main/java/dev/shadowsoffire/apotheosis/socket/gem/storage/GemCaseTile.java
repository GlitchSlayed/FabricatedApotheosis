package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth.Tiles;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.network.VanillaPacketDispatcher;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class GemCaseTile extends BlockEntity implements TickingBlockEntity {

    public static final int UPGRADE_MAT_SLOTS = 6;

    protected final Object2ObjectMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> gems = new Object2ObjectLinkedOpenHashMap<>();
    protected final Set<GemCaseMenu> activeContainers = new HashSet<>();
    protected final Storage<ItemVariant> itemHandler = new GemCaseStorage();
    protected final int maxCount;

    protected final InternalItemHandler upgradeMats = new InternalItemHandler(UPGRADE_MAT_SLOTS) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            GemCaseTile.this.setChanged();
        }
    };

    protected final Int2ObjectMap<UnsocketedGem> slotIndicies = new Int2ObjectOpenHashMap<>();

    protected int openCount = 0;
    protected float animationState = 0;
    protected float prevAnimationState = 0;

    public GemCaseTile(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState, int maxCount) {
        super(type, pPos, pBlockState);
        this.maxCount = maxCount;
    }

    public Object2ObjectMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> getGems() {
        return this.gems;
    }

    public EnumMap<Purity, Integer> getGems(DynamicHolder<Gem> gem) {
        return this.gems.computeIfAbsent(gem, g -> new EnumMap<>(Purity.class));
    }

    public int getCount(DynamicHolder<Gem> gem, Purity purity) {
        return this.getGems(gem).getOrDefault(purity, 0);
    }

    public int getCount(Gem gem, Purity purity) {
        return this.getCount(dev.shadowsoffire.apotheosis.socket.gem.GemRegistry.INSTANCE.holder(gem), purity);
    }

    public void depositGem(ItemStack stack) {
        dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem gem = dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem.of(stack);
        if (!gem.isValid()) return;
        EnumMap<Purity, Integer> map = this.getGems(gem.gem());
        int stored = map.getOrDefault(gem.purity(), 0);
        int inserted = (int) Math.min(stack.getCount(), this.maxCount - stored);
        if (inserted > 0) {
            map.put(gem.purity(), stored + inserted);
            stack.shrink(inserted);
            this.setChanged();
            this.activeContainers.forEach(GemCaseMenu::onChanged);
            if (this.level != null && !this.level.isClientSide()) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            }
        }
    }

    public void setCount(DynamicHolder<Gem> gem, Purity purity, int count) {
        count = Math.clamp(count, 0, this.maxCount);
        this.getGems(gem).put(purity, count);
        this.setChanged();
        this.activeContainers.forEach(GemCaseMenu::onChanged);
        if (this.level != null && !this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
    }

    public void addListener(GemCaseMenu menu) {
        this.activeContainers.add(menu);
    }

    public void removeListener(GemCaseMenu menu) {
        this.activeContainers.remove(menu);
    }

    public InternalItemHandler getUpgradeMats() {
        return this.upgradeMats;
    }

    @Nullable
    public GemUpgradeMatch getUpgradeMatch(DynamicHolder<Gem> holder, Purity purity) {
        EnumMap<Purity, Integer> map = this.gems.get(holder);
        if (map == null) {
            return null;
        }
        return GemUpgradeMatch.findMatch(this.level, purity, map, this.upgradeMats);
    }

    public ItemStack extractGem(DynamicHolder<Gem> holder, Purity purity, int count) {
        EnumMap<Purity, Integer> map = this.gems.get(holder);
        if (map == null) return ItemStack.EMPTY;
        int current = map.getOrDefault(purity, 0);
        int toExtract = Math.min(count, current);
        if (toExtract <= 0) return ItemStack.EMPTY;
        map.put(purity, current - toExtract);
        this.setChanged();
        this.activeContainers.forEach(GemCaseMenu::onChanged);
        if (this.level != null && !this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
        ItemStack stack = holder.get().toStack(purity);
        stack.setCount(toExtract);
        return stack;
    }

    public boolean upgradeGem(DynamicHolder<Gem> holder, Purity purity) {
        EnumMap<Purity, Integer> map = this.gems.get(holder);
        if (map == null) {
            return false;
        }
        GemUpgradeMatch match = GemUpgradeMatch.findMatch(this.level, purity, map, this.upgradeMats);
        if (match != null) {
            match.execute(this.upgradeMats, map);
            this.setChanged();
            return true;
        }
        return false;
    }

    public void startOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openCount++;
        }
    }

    public void stopOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openCount--;
        }
    }

    @Override
    public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {
        this.prevAnimationState = this.animationState;
        if (this.openCount > 0 && this.animationState < 1.0F) {
            this.animationState = Math.min(1.0F, this.animationState + 0.1F);
        }
        else if (this.openCount <= 0 && this.animationState > 0.0F) {
            this.animationState = Math.max(0.0F, this.animationState - 0.1F);
        }
    }

    public float getOpenAngle(float pPartialTicks) {
        return this.prevAnimationState + (this.animationState - this.prevAnimationState) * pPartialTicks;
    }

    public void saveGemData(CompoundTag tag) {
        CompoundTag gems = new CompoundTag();
        for (DynamicHolder<Gem> gem : this.gems.keySet()) {
            EnumMap<Purity, Integer> map = this.gems.get(gem);
            CompoundTag purityTag = new CompoundTag();
            for (Purity p : Purity.values()) {
                int count = map.getOrDefault(p, 0);
                if (count > 0) {
                    purityTag.putInt(p.getSerializedName(), count);
                }
            }
            gems.put(gem.getId().toString(), purityTag);
        }
        tag.put("gems", gems);
    }

    public void loadGemData(CompoundTag tag) {
        CompoundTag gems = tag.getCompoundOrEmpty("gems");
        for (String key : gems.keySet()) {
            Identifier res = Identifier.tryParse(key);
            DynamicHolder<Gem> gem = GemRegistry.INSTANCE.holder(res);
            if (!gem.isBound()) {
                continue;
            }
            CompoundTag purityTag = gems.getCompoundOrEmpty(key);
            if (purityTag.isEmpty()) {
                this.gems.remove(gem);
            }
            else {
                EnumMap<Purity, Integer> map = new EnumMap<>(Purity.class);
                for (Purity p : Purity.values()) {
                    map.put(p, purityTag.getIntOr(p.getSerializedName(), 0));
                }
                this.gems.put(gem, map);
            }
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag gemTag = new CompoundTag();
        this.saveGemData(gemTag);
        output.store("gems", CompoundTag.CODEC, gemTag.getCompoundOrEmpty("gems"));
        this.upgradeMats.save(output.child("upgrade_materials"));
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("gems", CompoundTag.CODEC).ifPresent(gems -> {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put("gems", gems);
            this.loadGemData(wrapper);
        });
        input.child("upgrade_materials").ifPresent(this.upgradeMats::load);
        this.activeContainers.forEach(GemCaseMenu::onChanged);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveGemData(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public Storage<ItemVariant> getItemHandler(@Nullable Direction dir) {
        return this.itemHandler;
    }

    private record Snapshot(Object2ObjectMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> gems) {}

    private class GemCaseStorage extends SnapshotParticipant<Snapshot> implements Storage<ItemVariant> {

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0 || resource.isBlank()) return 0;
            ItemStack stack = resource.toStack((int) Math.min(maxAmount, Integer.MAX_VALUE));
            UnsocketedGem gem = UnsocketedGem.of(stack);
            if (!gem.isValid()) return 0;

            EnumMap<Purity, Integer> map = GemCaseTile.this.getGems(gem.gem());
            int stored = map.getOrDefault(gem.purity(), 0);
            int inserted = (int) Math.min(maxAmount, GemCaseTile.this.maxCount - stored);
            if (inserted <= 0) return 0;

            this.updateSnapshots(transaction);
            map.put(gem.purity(), stored + inserted);
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0 || resource.isBlank()) return 0;
            ItemStack stack = resource.toStack(1);
            UnsocketedGem reqGem = UnsocketedGem.of(stack);
            if (!reqGem.isValid()) return 0;

            EnumMap<Purity, Integer> map = GemCaseTile.this.getGems(reqGem.gem());
            int stored = map.getOrDefault(reqGem.purity(), 0);
            int extracted = (int) Math.min(maxAmount, stored);
            if (extracted <= 0) return 0;

            this.updateSnapshots(transaction);
            map.put(reqGem.purity(), stored - extracted);
            return extracted;
        }

        @Override
        protected Snapshot createSnapshot() {
            Object2ObjectLinkedOpenHashMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> copy = new Object2ObjectLinkedOpenHashMap<>();
            for (Object2ObjectMap.Entry<DynamicHolder<Gem>, EnumMap<Purity, Integer>> e : GemCaseTile.this.gems.object2ObjectEntrySet()) {
                copy.put(e.getKey(), new EnumMap<>(e.getValue()));
            }
            return new Snapshot(copy);
        }

        @Override
        protected void readSnapshot(Snapshot snapshot) {
            GemCaseTile.this.gems.clear();
            GemCaseTile.this.gems.putAll(snapshot.gems());
        }

        @Override
        protected void onFinalCommit() {
            if (GemCaseTile.this.level != null && !GemCaseTile.this.level.isClientSide()) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(GemCaseTile.this);
            }
            GemCaseTile.this.setChanged();
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            List<StorageView<ItemVariant>> views = new ArrayList<>();
            for (Object2ObjectMap.Entry<DynamicHolder<Gem>, EnumMap<Purity, Integer>> entry : GemCaseTile.this.gems.object2ObjectEntrySet()) {
                DynamicHolder<Gem> gem = entry.getKey();
                if (!gem.isBound()) continue;
                for (Map.Entry<Purity, Integer> pe : entry.getValue().entrySet()) {
                    if (pe.getValue() > 0) {
                        ItemStack st = GemItem.createStack(gem.get(), pe.getKey(), 1);
                        ItemVariant variant = ItemVariant.of(st);
                        final int count = pe.getValue();
                        views.add(new StorageView<ItemVariant>() {
                            @Override
                            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                                return 0;
                            }

                            @Override
                            public boolean isResourceBlank() {
                                return variant.isBlank();
                            }

                            @Override
                            public ItemVariant getResource() {
                                return variant;
                            }

                            @Override
                            public long getAmount() {
                                return count;
                            }

                            @Override
                            public long getCapacity() {
                                return GemCaseTile.this.maxCount;
                            }
                        });
                    }
                }
            }
            return views.iterator();
        }
    }

    public static class BasicGemCaseTile extends GemCaseTile {
        public BasicGemCaseTile(BlockPos pos, BlockState state) {
            super(Tiles.GEM_CASE, pos, state, Short.MAX_VALUE);
        }
    }

    public static class EnderGemCaseTile extends GemCaseTile {
        public EnderGemCaseTile(BlockPos pos, BlockState state) {
            super(Tiles.ENDER_GEM_CASE, pos, state, Integer.MAX_VALUE);
        }
    }
}
