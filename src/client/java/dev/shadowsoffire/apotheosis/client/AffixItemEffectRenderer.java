package dev.shadowsoffire.apotheosis.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRenderData;
import dev.shadowsoffire.apotheosis.loot.RarityRenderData.ShadowData;
import dev.shadowsoffire.apotheosis.particle.RarityParticleData;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Specialized visual effect renderer for dropped affix items (beams, particles, colored shadows).
 * Specialized visual refinement deferred to Milestone 10.
 */
public class AffixItemEffectRenderer {

    private static final float GROW_IN_TICKS = 15F;
    private static final int ALPHA_ZERO = 0;
    private static final int ALPHA_LOW = 0x1F;
    private static final int ALPHA_MAX = 0x9F;

    // Beams and shadows geometry submission deferred to Milestone 10 visual refinement

    private static int color(int alpha, int color) {
        return ARGB.color(alpha, color);
    }

    public static void spawnParticles(Minecraft mc) {
        if (!AdventureConfig.enableAffixItemEffects) {
            return;
        }

        if (mc.level == null || mc.isPaused()) {
            return;
        }

        for (Entity ent : mc.level.entitiesForRendering()) {
            if (!(ent instanceof ItemEntity item)) {
                continue;
            }

            ItemStack stack = item.getItem();
            DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
            if (!rarityHolder.isBound() || !item.onGround()) {
                continue;
            }

            LootRarity rarity = rarityHolder.get();
            if (!rarity.renderData().particle().enabled()) {
                continue;
            }

            int delay = item.getAttachedOrCreate(Apoth.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME, () -> 0);
            if (item.tickCount - delay <= 0) {
                continue;
            }

            int color = rarity.color().getValue();
            RarityParticleData opt = new RarityParticleData(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F);
            RandomSource rand = item.getRandom();
            double spread = 0.1;
            mc.level.addParticle(opt,
                item.getX() - spread + rand.nextDouble() * 2 * spread,
                item.getY(),
                item.getZ() - spread + rand.nextDouble() * 2 * spread,
                0, 0.03 + 0.005 * rand.nextGaussian(), 0);
            item.setAttached(Apoth.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME, item.tickCount + 10 + rand.nextInt(15));
        }
    }
}
