package dev.shadowsoffire.apotheosis.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

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
import dev.shadowsoffire.apotheosis.client.StoneformingTooltipRenderer.StoneformingComponent;
import dev.shadowsoffire.apotheosis.client.mixin.KeyMappingAccessor;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload.BossSpawnData;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketTooltipComponent;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipeCache;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingScreen;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseScreen;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTileRenderer;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apotheosis.util.EquipmentComparePositioner;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AdventureModuleClient {

    public static final int COMPARE_PADDING = 18;

    public static final ExtraModelKey<BlockStateModel> HAMMER_MODEL = ExtraModelKey.create(() -> "apotheosis:hammer");
    public static final ExtraModelKey<BlockStateModel> STAR_CUBE_MODEL = ExtraModelKey.create(() -> "apotheosis:star_cube");

    public static final int[] GHOST_ALPHA_TIERS = {
        0x40, 0x48, 0x50, 0x58, 0x60, 0x68, 0x70, 0x78, 0x80, 0x88,
        0x90, 0x98, 0xA0, 0xA8, 0xB0, 0xB8, 0xC0, 0xC8, 0xD0, 0xD8, 0xE0
    };

    private static final List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();

    public static void init() {
        // Register standalone block state models for block entity renderers (using item atlas textures)
        ModelLoadingPlugin.register(ctx -> {
            ctx.addModel(HAMMER_MODEL, createUnfilteredBlockStateModel(Apotheosis.loc("item/hammer")));
            ctx.addModel(STAR_CUBE_MODEL, createUnfilteredBlockStateModel(Apotheosis.loc("item/star_cube")));
        });

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
            if (comp instanceof SocketTooltipComponent sc) {
                return new SocketTooltipRenderer(sc);
            }
            if (comp instanceof StoneformingComponent sfc) {
                return new StoneformingTooltipRenderer(sfc);
            }
            return null;
        });

        // Register key mappings
        AdventureKeys.init();

        ApothMiscUtil.clientPlayerSupplier = () -> Minecraft.getInstance().player;
        ApothMiscUtil.clientAdvancementChecker = key -> {
            var conn = Minecraft.getInstance().getConnection();
            if (conn != null) {
                var advs = conn.getAdvancements();
                var holder = advs.get(key);
                if (holder != null) {
                    var progress = advs.progress().get(holder);
                    return progress != null && progress.isDone();
                }
            }
            return false;
        };
        WorldTier.clientTutorialChecker = player -> {
            if (WorldTier.clientTutorialDismissed) {
                return false;
            }
            if (player instanceof net.minecraft.client.player.LocalPlayer lp) {
                if (lp.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(dev.shadowsoffire.apotheosis.Apoth.Stats.WORLD_TIERS_ACTIVATED)) > 0) {
                    return false;
                }
            }
            return WorldTier.getTier(player) == WorldTier.HAVEN;
        };

        // Wire client dynamic registry lookup
        dev.shadowsoffire.placebo.dynreg.DynamicRegistry.setClientLookupSupplier(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.getConnection() != null) {
                return client.getConnection().registryAccess();
            }
            return null;
        });

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

        // Request player stats immediately upon joining so world tier tutorial state resolves
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            handler.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
        });

        // Clear recipe caches and tutorial state on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SalvagingRecipeCache.clear();
            GemCuttingRecipeCache.clear();
            ReforgingRecipeCache.clear();
            WorldTier.clientTutorialDismissed = false;
        });
    }

    /**
     * Bakes an item-atlas standalone model into a {@link BlockStateModel} without
     * Fabric API's block-atlas rejection filter, for block entity renderers paired with
     * {@link net.minecraft.client.renderer.Sheets#cutoutItemSheet()} / {@code translucentItemSheet()}.
     */
    private static SimpleUnbakedExtraModel<BlockStateModel> createUnfilteredBlockStateModel(Identifier id) {
        return new SimpleUnbakedExtraModel<>(id, (resolvedModel, baker) -> {
            TextureSlots slots = resolvedModel.getTopTextureSlots();
            boolean ao = resolvedModel.getTopAmbientOcclusion();
            Material.Baked particle = resolvedModel.resolveParticleMaterial(slots, baker);
            QuadCollection quads = resolvedModel.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);
            return new SingleVariant(new SimpleModelWrapper(quads, ao, particle));
        });
    }

    public static void renderBossBeams(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector collector) {
        if (BOSS_SPAWNS.isEmpty()) {
            return;
        }

        Vec3 camPos = levelRenderState.cameraRenderState.pos;
        float animTime = levelRenderState.gameTime;

        for (BossSpawnData data : BOSS_SPAWNS) {
            BlockPos pos = data.pos();
            int color = 0xFF000000 | data.rarity().color().getValue();

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            BeaconRenderer.submitBeaconBeam(
                poseStack, collector,
                BeaconRenderer.BEAM_LOCATION,
                1.0F, animTime, 0, 1024, color, 0.2F, 0.25F);
            poseStack.popPose();
        }
    }

    public static void renderCanSocketTooltip(AbstractContainerScreen<?> screen, Slot hoveredSlot, GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return;
        }

        ItemStack carried = screen.getMenu().getCarried();
        ItemStack hover = hoveredSlot.getItem();
        if (!carried.is(Apoth.Items.GEM) || !SocketHelper.canSocketGemInItem(hover, carried)) {
            return;
        }

        Component itemName = Component.translatable("%s", hover.getHoverName()).withStyle(ChatFormatting.WHITE);
        Component line = Apotheosis.lang("misc", "right_click_to_socket", carried.getHoverName(), itemName).withStyle(ChatFormatting.GRAY);

        Font font = Minecraft.getInstance().font;
        List<ClientTooltipComponent> comps = List.of(ClientTooltipComponent.create(line.getVisualOrderText()));
        gfx.tooltip(font, comps, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null, false);
    }

    private static boolean inComparisonRender = false;
    private static final Identifier COMPARE_TOOLTIP_SPRITES = Apotheosis.loc("compare");

    public static boolean tryCompareItems(AbstractContainerScreen<?> screen, Slot hoveredSlot, GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        if (inComparisonRender || !AdventureConfig.enableEquipmentCompare || hoveredSlot == null || !hoveredSlot.hasItem()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        InputConstants.Key compareKey = ((KeyMappingAccessor) AdventureKeys.COMPARE_EQUIPMENT).apoth_getKey();
        if (compareKey.getType() != InputConstants.Type.KEYBOARD || !InputConstants.isKeyDown(compareKey.getValue())) {
            return false;
        }

        ItemStack stack = hoveredSlot.getItem();
        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone() || mc.player == null) {
            return false;
        }

        Player player = mc.player;
        ItemStack equipped = ItemStack.EMPTY;

        Equippable equip = stack.get(DataComponents.EQUIPPABLE);
        if (equip != null) {
            ItemStack candidate = player.getItemBySlot(equip.slot());
            if (!candidate.isEmpty() && stack != candidate) {
                equipped = candidate;
            }
        } else {
            if (cat.getSlots().test(ALObjects.EquipmentSlots.MAINHAND)) {
                ItemStack candidate = player.getMainHandItem();
                if (LootCategory.forItem(candidate) == cat && stack != candidate) {
                    equipped = candidate;
                }
            }
            if (equipped.isEmpty() && cat.getSlots().test(ALObjects.EquipmentSlots.OFFHAND)) {
                ItemStack candidate = player.getOffhandItem();
                if (LootCategory.forItem(candidate) == cat && stack != candidate) {
                    equipped = candidate;
                }
            }
        }

        if (equipped.isEmpty()) {
            return false;
        }

        return renderComparison(mc, screen, gfx, stack, equipped, mouseX, mouseY);
    }

    private static List<ClientTooltipComponent> gatherComponents(Minecraft mc, Font font, ItemStack stack, int maxWidth) {
        List<Component> lines = Screen.getTooltipFromItem(mc, stack);
        List<ClientTooltipComponent> list = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            for (var seq : font.split(line, maxWidth)) {
                list.add(ClientTooltipComponent.create(seq));
            }
            if (i == 0) {
                stack.getTooltipImage().ifPresent(comp -> {
                    ClientTooltipComponent custom = ClientTooltipComponentCallback.EVENT.invoker().getClientComponent(comp);
                    list.add(custom != null ? custom : ClientTooltipComponent.create(comp));
                });
            }
        }
        return list;
    }

    private static boolean renderComparison(Minecraft mc, AbstractContainerScreen<?> screen, GuiGraphicsExtractor gfx, ItemStack stack, ItemStack equipped, int mouseX, int mouseY) {
        Font font = mc.font;
        int scnWidth = gfx.guiWidth();
        int scnHeight = gfx.guiHeight();
        int defaultMaxWidth = Math.max(120, scnWidth - mouseX - 16);

        List<ClientTooltipComponent> compList = gatherComponents(mc, font, stack, defaultMaxWidth);
        int compWidth = -1;
        int compHeight = 0;
        for (var comp : compList) {
            compWidth = Math.max(compWidth, comp.getWidth(font));
            compHeight += comp.getHeight(font);
        }

        Vector2ic compPos = DefaultTooltipPositioner.INSTANCE.positionTooltip(scnWidth, scnHeight, mouseX, mouseY, compWidth, compHeight);
        int availLeftWidth = Math.max(100, scnWidth - compWidth - COMPARE_PADDING * 2);
        List<ClientTooltipComponent> equipList = gatherComponents(mc, font, equipped, availLeftWidth);

        int equipWidth = -1;
        int equipHeight = 0;
        for (var comp : equipList) {
            equipWidth = Math.max(equipWidth, comp.getWidth(font));
            equipHeight += comp.getHeight(font);
        }

        Vector2ic equipPos = new Vector2i(compPos.x() - COMPARE_PADDING - equipWidth, compPos.y());
        EquipmentComparePositioner realPositioner = new EquipmentComparePositioner(scnWidth, scnHeight);
        if (!realPositioner.position(equipPos, equipWidth + 6, equipHeight + 6, compPos, compWidth + 6, compHeight + 6)) {
            return false;
        }

        inComparisonRender = true;
        try {
            final Vector2ic finalCompPos = realPositioner.getComparePos();
            final Vector2ic finalEquipPos = realPositioner.getEquippedPos();

            ClientTooltipPositioner compFixed = (sw, sh, mx, my, w, h) -> finalCompPos;
            ClientTooltipPositioner equipFixed = (sw, sh, mx, my, w, h) -> finalEquipPos;

            gfx.tooltip(font, compList, finalCompPos.x(), finalCompPos.y(), compFixed, stack.get(DataComponents.TOOLTIP_STYLE), false);
            gfx.tooltip(font, equipList, finalEquipPos.x(), finalEquipPos.y(), equipFixed, equipped.get(DataComponents.TOOLTIP_STYLE), false);

            Component equippedTxt = Apotheosis.lang("text", "equipped");
            int txtWidth = font.width(equippedTxt);
            int txtX = finalEquipPos.x() + equipWidth / 2 - txtWidth / 2;
            int txtY = finalEquipPos.y() - font.lineHeight - 10;

            TooltipRenderUtil.extractTooltipBackground(gfx, txtX, txtY, txtWidth, font.lineHeight, COMPARE_TOOLTIP_SPRITES);
            gfx.text(font, equippedTxt, txtX, txtY, 0xFFFFFFFF);
            return true;
        } finally {
            inComparisonRender = false;
        }
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
