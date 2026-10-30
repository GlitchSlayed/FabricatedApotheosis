package dev.shadowsoffire.apotheosis.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apoth.Menus;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AttributeProvidingAffix;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingScreen;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingTableTileRenderer;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipeCache;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingScreen;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingTableTileRenderer;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipeCache;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.client.SocketTooltipRenderer.SocketComponent;
import dev.shadowsoffire.apotheosis.client.StoneformingTooltipRenderer.StoneformingComponent;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload.BossSpawnData;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipeCache;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingScreen;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseScreen;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTileRenderer;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AdventureModuleClient {

    public static final int COMPARE_PADDING = 18;

    public static final int[] GHOST_ALPHA_TIERS = {
        0x40, 0x48, 0x50, 0x58, 0x60, 0x68, 0x70, 0x78, 0x80, 0x88,
        0x90, 0x98, 0xA0, 0xA8, 0xB0, 0xB8, 0xC0, 0xC8, 0xD0, 0xD8, 0xE0
    };

    private static final List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();

    public static void init() {
        // Register workstation screens
        MenuScreens.register(Menus.REFORGING, ReforgingScreen::new);
        MenuScreens.register(Menus.SALVAGE, SalvagingScreen::new);
        MenuScreens.register(Menus.GEM_CUTTING, GemCuttingScreen::new);
        MenuScreens.register(Menus.AUGMENTING, AugmentingScreen::new);
        MenuScreens.register(Menus.GEM_CASE, GemCaseScreen::new);

        // Register block entity renderers
        BlockEntityRendererRegistry.register(Apoth.Tiles.REFORGING_TABLE, ReforgingTableTileRenderer::new);
        BlockEntityRendererRegistry.register(Apoth.Tiles.AUGMENTING_TABLE, AugmentingTableTileRenderer::new);
        BlockEntityRendererRegistry.register(Apoth.Tiles.GEM_CASE, GemCaseTileRenderer::new);
        BlockEntityRendererRegistry.register(Apoth.Tiles.ENDER_GEM_CASE, GemCaseTileRenderer::new);

        // Register custom tooltip components
        ClientTooltipComponentCallback.EVENT.register(comp -> {
            if (comp instanceof SocketComponent sc) {
                return new SocketTooltipRenderer(sc);
            }
            if (comp instanceof StoneformingComponent sfc) {
                return new StoneformingTooltipRenderer(sfc);
            }
            return null;
        });

        // Register key mappings
        AdventureKeys.init();

        // Bootstrap modifier source
        TierAugmentModifierSource.bootstrap();

        // Register item tooltip callback
        ItemTooltipCallback.EVENT.register(AdventureModuleClient::affixTooltips);

        // Register rarity particle provider
        ParticleProviderRegistry.getInstance().register(Apoth.Particles.RARITY_GLOW, RarityParticle.Provider::new);

        // Tick handlers
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AffixItemEffectRenderer.spawnParticles(client);
            for (int i = 0; i < BOSS_SPAWNS.size(); i++) {
                BossSpawnData data = BOSS_SPAWNS.get(i);
                if (data.ticks().getAndIncrement() > 400) {
                    BOSS_SPAWNS.remove(i--);
                }
            }
        });

        // Clear recipe caches on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SalvagingRecipeCache.clear();
            GemCuttingRecipeCache.clear();
            ReforgingRecipeCache.clear();
        });
    }

    public static void onBossSpawn(BlockPos pos, DynamicHolder<LootRarity> rarityHolder) {
        if (rarityHolder.isBound()) {
            LootRarity rarity = rarityHolder.get();
            BOSS_SPAWNS.add(new BossSpawnData(pos, rarity, new MutableInt()));
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.getSoundManager().play(new SimpleSoundInstance(rarity.invaderSound(), SoundSource.HOSTILE, AdventureConfig.bossAnnounceRange / 16F, 1.0F, mc.player.getRandom(), pos));
            }
        }
    }

    public static AttributeTooltipContext tooltipCtx() {
        return AttributeTooltipContext.of(Minecraft.getInstance().player, TooltipContext.of(Minecraft.getInstance().level), net.minecraft.world.item.component.TooltipDisplay.DEFAULT, ApothicAttributes.getTooltipFlag());
    }

    public static void affixTooltips(ItemStack stack, TooltipContext context, TooltipFlag flags, List<Component> tooltips) {
        AttributeTooltipContext ctx = AttributeTooltipContext.of(Minecraft.getInstance().player, context,
            stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT), flags);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && WorldTier.isTutorialActive(mc.player)) {
            if (stack.has(Components.AFFIXES) || stack.has(Components.SOCKETS) || stack.has(Components.RARITY)) {
                tooltips.add(1, Apotheosis.lang("text", "world_tier_tutorial").withStyle(ChatFormatting.YELLOW));
                tooltips.add(2, Apotheosis.lang("text", "world_tier_tutorial.2", AdventureKeys.OPEN_WORLD_TIER_SELECT.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        List<Component> components = new ArrayList<>();

        if (stack.has(Components.AFFIXES)) {
            AffixHelper.streamAffixes(stack)
                .sorted(Comparator.comparingInt(a -> a.getAffix().definition().type().ordinal()))
                .forEach(inst -> {
                    Component desc = inst.getDescription(ctx);
                    if (desc.getContents() != PlainTextContents.EMPTY) {
                        if (inst.level() > Affix.STANDARD_MAX_LEVEL) {
                            components.add(ApothMiscUtil.starPrefix(desc).withStyle(ChatFormatting.YELLOW));
                        }
                        else {
                            components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.YELLOW));
                        }
                    }
                });
        }

        if (stack.has(Components.DURABILITY_BONUS) && !stack.has(DataComponents.UNBREAKABLE)) {
            Component desc = Component.translatable("affix.apotheosis:durable.desc", Math.round(100 * stack.get(Components.DURABILITY_BONUS)));
            components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.YELLOW));
        }

        if (stack.getOrDefault(Components.MALICE_MARKER, false)) {
            Component desc = Apotheosis.lang("text", "malice_marker").withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
            components.add(desc);
        }

        if (stack.getOrDefault(Components.TOUCHED_BY_MALICE, 0) > 0) {
            Component desc = Apotheosis.lang("text", "touched_by_malice", String.valueOf(stack.get(Components.TOUCHED_BY_MALICE)));
            MutableComponent prefixed = AffixHelper.isExtremelyMalicious(stack) ? ApothMiscUtil.starPrefix(desc) : ApothMiscUtil.dotPrefix(desc);
            components.add(prefixed.withStyle(ChatFormatting.RED));
        }

        if (!components.isEmpty()) {
            tooltips.addAll(1, components);
        }

        // Over-max affix indicator
        Set<Component> special = new HashSet<>();
        AffixHelper.streamAffixes(stack)
            .filter(inst -> inst.level() > Affix.STANDARD_MAX_LEVEL)
            .filter(inst -> inst.getAffix() instanceof AttributeProvidingAffix)
            .forEach(inst -> ((AttributeProvidingAffix) inst.getAffix()).gatherModifierTooltips(inst, ctx, special::add));

        Component listHeader = Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);

        if (!special.isEmpty()) {
            for (int i = 0; i < tooltips.size(); i++) {
                Component comp = tooltips.get(i);
                if (special.contains(comp)) {
                    tooltips.remove(i);
                    tooltips.add(i, ApothMiscUtil.starPrefix(comp).withStyle(comp.getStyle()));
                }
                else if (comp.getContents().equals(listHeader.getContents()) && comp.getSiblings().size() == 1) {
                    Component child = comp.getSiblings().get(0);
                    if (special.contains(child)) {
                        tooltips.remove(i);
                        MutableComponent replacement = listHeader.copy();
                        replacement.append(ApothMiscUtil.starPrefix(child).withStyle(child.getStyle()));
                        for (int j = 1; j < comp.getSiblings().size(); j++) {
                            replacement.append(comp.getSiblings().get(j));
                        }
                        tooltips.add(i, replacement);
                    }
                }
            }
        }

        // Show blacklisted potions for potion charms
        if (stack.getItem() == Items.POTION) {
            Holder<Potion> potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(Potions.WATER);
            if (!dev.shadowsoffire.apotheosis.item.PotionCharmItem.isValidPotion(potion)) {
                tooltips.add(Component.translatable("misc.apotheosis.blacklisted_potion").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }
    }
}
