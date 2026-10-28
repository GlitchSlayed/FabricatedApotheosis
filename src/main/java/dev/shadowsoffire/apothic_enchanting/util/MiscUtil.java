package dev.shadowsoffire.apothic_enchanting.util;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;

public class MiscUtil {

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
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        Player player = source != null ? level.getPlayerByUUID(source) : null;
        if (player != null) {
            return level.destroyBlock(pos, true, player);
        }
        return level.destroyBlock(pos, true);
    }

    public static String getEnchDescKey(Holder<Enchantment> ench) {
        return ench.unwrapKey().map(k -> k.identifier().toLanguageKey("enchantment") + ".desc").orElse("");
    }

    @Nullable
    public static <T> Holder<T> findHolder(ResourceKey<? extends Registry<T>> registryKey, T obj) {
        return null;
    }
}
