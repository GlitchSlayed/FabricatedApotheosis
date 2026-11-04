package dev.shadowsoffire.apotheosis.mixin;

import java.util.Optional;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.loot.modifiers.LootModifierManager;
import dev.shadowsoffire.apotheosis.util.LootContextAccess;
import dev.shadowsoffire.apotheosis.util.LootTableAccess;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootTable.class)
public abstract class LootTableMixin implements LootTableAccess {

    @Shadow
    private Optional<Identifier> randomSequence;

    @Shadow
    public abstract void getRandomItemsRaw(LootContext context, Consumer<ItemStack> output);

    @Unique
    private ResourceKey<LootTable> apoth$tableKey;

    @Override
    public ResourceKey<LootTable> apoth_getTableKey() {
        return this.apoth$tableKey;
    }

    @Override
    public void apoth_setTableKey(ResourceKey<LootTable> key) {
        this.apoth$tableKey = key;
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void apoth$applyGlobalLootModifiers(LootContext context, Consumer<ItemStack> output, CallbackInfo ci) {
        if (context instanceof LootContextAccess access) {
            Identifier id = access.apoth_getQueriedLootTableId();
            if (id == null || "empty".equals(id.getPath())) {
                if (this.apoth$tableKey != null) {
                    access.apoth_setQueriedLootTableId(this.apoth$tableKey.identifier());
                } else if (this.randomSequence.isPresent()) {
                    access.apoth_setQueriedLootTableId(this.randomSequence.get());
                }
            }
        }

        if (LootModifierManager.hasModifiers()) {
            ci.cancel();
            ObjectArrayList<ItemStack> items = new ObjectArrayList<>();
            this.getRandomItemsRaw(context, items::add);
            items = LootModifierManager.applyModifiers(items, context);
            Consumer<ItemStack> splitter = LootTable.createStackSplitter(context.getLevel(), output);
            items.forEach(splitter);
        }
    }
}
