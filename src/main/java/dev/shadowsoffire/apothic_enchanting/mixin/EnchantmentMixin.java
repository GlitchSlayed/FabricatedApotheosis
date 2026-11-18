package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.util.TooltipUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(value = Enchantment.class, priority = 1500)
public class EnchantmentMixin {

    /**
     * Allows Tridents to accept Sharpness, Looting, and Piercing (restoring parity with upstream TridentItemMixin).
     */
    @Inject(method = { "isSupportedItem", "canEnchant" }, at = @At("RETURN"), cancellable = true)
    private void apoth_allowTridentEnchantments(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()
            && ApotheosisConfig.getServer().enableEnchanting
            && ApotheosisConfig.getServer().allowTridentCombatEnchantments
            && stack.getItem() instanceof TridentItem) {
            Enchantment self = (Enchantment) (Object) this;
            if (self.description().getContents() instanceof TranslatableContents tc) {
                String key = tc.getKey();
                if ("enchantment.minecraft.sharpness".equals(key)
                    || "enchantment.minecraft.looting".equals(key)
                    || "enchantment.minecraft.piercing".equals(key)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    /**
     * Adjusts the color of the enchantment text if above the vanilla max.
     */
    @Inject(method = "getFullname", at = @At("RETURN"), cancellable = true, require = 1)
    private static void apoth_modifyEnchColorForAboveMaxLevel(Holder<Enchantment> ench, int level, CallbackInfoReturnable<Component> cir) {
        TooltipUtil.applyOverMaxLevelColor(ench, level, cir.getReturnValue());
        // A star prefix is shown to indicate that an Enchantment is above the Apoth configured max value.
        if (level > ApothicEnchanting.getEnchInfo(ench).getMaxLevel(ench)) {
            MutableComponent star = ApothicEnchanting.lang("text", "star_prefix").withStyle(cir.getReturnValue().getStyle());
            star.append(cir.getReturnValue());
            // We need to flatten the original components' siblings into the star prefix so that later logic can remove the level component if needed.
            for (Component c : cir.getReturnValue().getSiblings()) {
                star.append(c);
            }
            cir.getReturnValue().getSiblings().clear();
            cir.setReturnValue(star);
        }
    }

}
