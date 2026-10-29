package dev.shadowsoffire.apothic_enchanting.util;

import java.util.UUID;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MiscUtil {

    private static volatile MinecraftServer CURRENT_SERVER = null;
    private static volatile BiFunction<ResourceKey<? extends Registry<?>>, Object, Holder<?>> CLIENT_HOLDER_LOOKUP = null;
    private static final ThreadLocal<Boolean> IS_BREAKING_EXTRA = ThreadLocal.withInitial(() -> false);

    public static void setCurrentServer(@Nullable MinecraftServer server) {
        CURRENT_SERVER = server;
    }

    public static void setClientHolderLookup(@Nullable BiFunction<ResourceKey<? extends Registry<?>>, Object, Holder<?>> lookup) {
        CLIENT_HOLDER_LOOKUP = lookup;
    }

    public static int getExpCostForSlot(int level, int slot) {
        int cost = 0;
        for (int i = 0; i <= slot; i++) {
            cost += EnchantmentUtils.getExperienceForLevel(level - i);
        }
        return cost - 1;
    }

    public static int[] doubleUpGradient(int[] data) {
        int[] out = new int[data.length * 2];
        System.arraycopy(data, 0, out, 0, data.length);
        for (int i = data.length - 1; i >= 0; i--) {
            out[data.length * 2 - 1 - i] = data[i];
        }
        return out;
    }

    public static boolean breakExtraBlock(ServerLevel level, BlockPos pos, ItemStack mainhand, @Nullable UUID source) {
        if (IS_BREAKING_EXTRA.get()) return false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false;

        ServerPlayer player = source != null && level.getServer() != null ? level.getServer().getPlayerList().getPlayer(source) : null;
        if (player != null && player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) {
            return false;
        }

        IS_BREAKING_EXTRA.set(true);
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            Block block = state.getBlock();

            if (player != null) {
                if (player.getAbilities().instabuild) {
                    block.playerWillDestroy(level, pos, state, player);
                    level.removeBlock(pos, false);
                    return true;
                } else {
                    ItemStack tool = player.getMainHandItem();
                    ItemStack toolCopy = tool.copy();
                    boolean canHarvest = player.hasCorrectToolForDrops(state);
                    block.playerWillDestroy(level, pos, state, player);
                    tool.mineBlock(level, state, pos, player);
                    boolean removed = level.destroyBlock(pos, false, player);
                    if (removed && canHarvest) {
                        block.playerDestroy(level, player, pos, state, blockEntity, toolCopy);
                        state.spawnAfterBreak(level, pos, toolCopy, true);
                    }
                    if (tool.isEmpty() && !toolCopy.isEmpty()) {
                        player.onEquippedItemBroken(toolCopy, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                    }
                    return removed;
                }
            } else {
                return level.destroyBlock(pos, true);
            }
        } finally {
            IS_BREAKING_EXTRA.set(false);
        }
    }

    public static String getEnchDescKey(Holder<Enchantment> ench) {
        return ench.unwrapKey().map(k -> k.identifier().toLanguageKey("enchantment") + ".desc").orElse("");
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Holder<T> findHolder(ResourceKey<? extends Registry<T>> registryKey, T obj) {
        if (obj == null) return null;

        // Try active server lookup
        MinecraftServer server = CURRENT_SERVER;
        if (server != null) {
            Registry<T> registry = server.registries().compositeAccess().lookup(registryKey).orElse(null);
            if (registry != null) {
                Holder<T> holder = registry.wrapAsHolder(obj);
                if (holder.kind() == Holder.Kind.REFERENCE) {
                    return holder;
                }
            }
        }

        // Try client lookup if wired
        BiFunction<ResourceKey<? extends Registry<?>>, Object, Holder<?>> clientLookup = CLIENT_HOLDER_LOOKUP;
        if (clientLookup != null) {
            try {
                Holder<?> clientHolder = clientLookup.apply(registryKey, obj);
                if (clientHolder != null && clientHolder.kind() == Holder.Kind.REFERENCE) {
                    return (Holder<T>) clientHolder;
                }
            } catch (Exception ignored) {}
        }

        return null;
    }
}
