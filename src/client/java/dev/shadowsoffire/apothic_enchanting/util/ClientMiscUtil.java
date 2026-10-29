package dev.shadowsoffire.apothic_enchanting.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Client-only registry accessor to safely resolve holders without dedicated-server leakage.
 */
public class ClientMiscUtil {

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Holder<T> findClientHolder(ResourceKey<? extends Registry<?>> registryKey, Object obj) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            Registry<T> registry = (Registry<T>) mc.level.registryAccess().lookup((ResourceKey<? extends Registry<T>>) registryKey).orElse(null);
            if (registry != null) {
                Holder<T> holder = registry.wrapAsHolder((T) obj);
                if (holder.kind() == Holder.Kind.REFERENCE) {
                    return holder;
                }
            }
        }
        return null;
    }
}
