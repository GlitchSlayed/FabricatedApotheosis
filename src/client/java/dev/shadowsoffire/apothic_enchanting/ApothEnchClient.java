package dev.shadowsoffire.apothic_enchanting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.shadowsoffire.apothic_enchanting.Ench.Particles;
import dev.shadowsoffire.apothic_enchanting.library.EnchLibraryScreen;
import dev.shadowsoffire.apothic_enchanting.payloads.CluePayload;
import dev.shadowsoffire.apothic_enchanting.payloads.EnchantmentInfoPayload;
import dev.shadowsoffire.apothic_enchanting.payloads.StatsPayload;
import dev.shadowsoffire.apothic_enchanting.table.ApothEnchantmentScreen;
import dev.shadowsoffire.apothic_enchanting.table.EnchantmentInfoLoader;
import dev.shadowsoffire.apothic_enchanting.table.RavenEnchantmentScreen;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipeCache;
import dev.shadowsoffire.apothic_enchanting.util.TooltipUtil;
import dev.shadowsoffire.placebo.util.DrawsOnLeft;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.particle.FlyTowardsPositionParticle;
import net.minecraft.locale.Language;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ApothEnchClient {

    private static final BlockHitResult MISS = BlockHitResult.miss(Vec3.ZERO, Direction.NORTH, BlockPos.ZERO);

    public static void init() {
        dev.shadowsoffire.apothic_enchanting.util.MiscUtil.setClientHolderLookup(dev.shadowsoffire.apothic_enchanting.util.ClientMiscUtil::findClientHolder);
        MenuScreens.register(Ench.Menus.ENCHANTING_TABLE, ApothEnchantmentScreen::new);
        MenuScreens.register(Ench.Menus.RAVEN_ENCHANTING_TABLE, RavenEnchantmentScreen::new);
        MenuScreens.register(Ench.Menus.LIBRARY, EnchLibraryScreen::new);

        ParticleProviderRegistry.getInstance().register(Particles.ENCHANT_FIRE, FlyTowardsPositionParticle.EnchantProvider::new);
        ParticleProviderRegistry.getInstance().register(Particles.ENCHANT_WATER, FlyTowardsPositionParticle.EnchantProvider::new);
        ParticleProviderRegistry.getInstance().register(Particles.ENCHANT_SCULK, FlyTowardsPositionParticle.EnchantProvider::new);
        ParticleProviderRegistry.getInstance().register(Particles.ENCHANT_END, FlyTowardsPositionParticle.EnchantProvider::new);

        ClientPlayNetworking.registerGlobalReceiver(CluePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> handleCluePayload(payload));
        });
        ClientPlayNetworking.registerGlobalReceiver(StatsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> handleStatsPayload(payload));
        });
        ClientPlayNetworking.registerGlobalReceiver(EnchantmentInfoPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> EnchantmentInfoLoader.applyClientSync(payload.infoMap()));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            InfusionRecipeCache.clear();
            EnchantmentInfoLoader.resetClient();
        });

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, tooltip) -> {
            Item i = stack.getItem();
            if (i == Ench.Items.PRISMATIC_WEB.value()) {
                tooltip.add(TooltipUtil.lang("info", "prismatic_cobweb").withStyle(ChatFormatting.GRAY));
            }
            else if (i instanceof BlockItem bi) {
                Block block = bi.getBlock();
                BlockState state = block.defaultBlockState();

                Level level = Minecraft.getInstance().level;
                if (level != null && Minecraft.getInstance().player != null) {
                    BlockPlaceContext ctx = new BlockPlaceContext(level, Minecraft.getInstance().player, InteractionHand.MAIN_HAND, stack, MISS);
                    try {
                        state = block.getStateForPlacement(ctx);
                    }
                    catch (Exception ignored) {}
                }

                if (state == null) {
                    state = block.defaultBlockState();
                }

                BlockGetter getter = level != null ? level : EmptyBlockGetter.INSTANCE;
                TooltipUtil.appendBlockStats(getter, state, BlockPos.ZERO, tooltip::add);
            }
            else if (i == Items.ENCHANTED_BOOK) {
                ItemEnchantments enchMap = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                if (enchMap.size() == 1) {
                    Object2IntMap.Entry<Holder<Enchantment>> entry = enchMap.entrySet().iterator().next();
                    Holder<Enchantment> ench = entry.getKey();
                    int level = entry.getIntValue();

                    if (!FabricLoader.getInstance().isModLoaded("enchdesc")) {
                        String key = ench.unwrapKey().map(k -> k.identifier().toLanguageKey("enchantment") + ".desc").orElse("");
                        if (!key.isEmpty() && Language.getInstance().has(key)) {
                            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }

                    if (ApothEnchConfig.showEnchantedBookMetadata) {
                        EnchantmentInfo info = ApothicEnchanting.getEnchInfo(ench);
                        Object[] args = new Object[4];
                        args[0] = boolComp("info.apothic_enchanting.discoverable", ench.is(EnchantmentTags.IN_ENCHANTING_TABLE));
                        args[1] = boolComp("info.apothic_enchanting.lootable", ench.is(EnchantmentTags.ON_RANDOM_LOOT));
                        args[2] = boolComp("info.apothic_enchanting.tradeable", ench.is(EnchantmentTags.TRADEABLE));
                        args[3] = boolComp("info.apothic_enchanting.treasure", ench.is(EnchantmentTags.TREASURE));
                        if (tooltipFlag.isAdvanced()) {
                            tooltip.add(Component.translatable("%s \u2507 %s \u2507 %s \u2507 %s", args[0], args[1], args[2], args[3]).withStyle(ChatFormatting.DARK_GRAY));
                            tooltip.add(Component.translatable("info.apothic_enchanting.book_range", info.getMinPower(level, ench), info.getMaxPower(level, ench)).withStyle(ChatFormatting.GREEN));
                        }
                        else {
                            tooltip.add(Component.translatable("%s \u2507 %s", args[2], args[3]).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }

                    EnchantmentInfo info = ApothicEnchanting.getEnchInfo(ench);
                    if (info.levelCap() != -1) {
                        Iterator<Component> it = tooltip.iterator();
                        Component toReplace = null;
                        while (it.hasNext()) {
                            Component comp = it.next();
                            if (comp.contains(ench.value().description())) {
                                toReplace = comp;
                                break;
                            }
                        }
                        if (toReplace != null) {
                            MutableComponent newComp = toReplace.copy();
                            Component limit = Component.translatable("info.apothic_enchanting.limit", Component.translatable("enchantment.level." + info.levelCap())).withStyle(ChatFormatting.DARK_GRAY);
                            newComp.append(CommonComponents.SPACE).append(limit);
                            tooltip.set(tooltip.indexOf(toReplace), newComp);
                        }
                    }
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AnvilScreen anv) {
                ScreenEvents.afterExtract(screen).register((scrn, gfx, mouseX, mouseY, tickDelta) -> {
                    int level = anv.getMenu().getCost();
                    if (level <= 0 || !anv.getMenu().getSlot(anv.getMenu().getResultSlot()).hasItem() || level == Integer.MAX_VALUE) return;
                    List<Component> list = new ArrayList<>();
                    list.add(TooltipUtil.lang("info", "anvil_at", level).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GREEN));
                    int expCost = EnchantmentUtils.getTotalExperienceForLevel(level);
                    list.add(TooltipUtil.lang("info", "anvil_xp_cost", Component.literal("" + expCost).withStyle(ChatFormatting.GREEN),
                        Component.literal("" + level).withStyle(ChatFormatting.GREEN)));
                    DrawsOnLeft.draw(anv, gfx, list, anv.topPos + 28);
                });
            }
        });
    }

    public static void handleCluePayload(CluePayload msg) {
        if (Minecraft.getInstance().gui.screen() instanceof ApothEnchantmentScreen es) {
            es.acceptClues(msg.slot(), msg.clues(), msg.all());
        }
    }

    public static void handleStatsPayload(StatsPayload msg) {
        if (Minecraft.getInstance().gui.screen() instanceof ApothEnchantmentScreen es) {
            es.getMenu().setStats(msg.stats());
        }
    }

    private static Component boolComp(String key, boolean flag) {
        return Component.translatable(key + (flag ? "" : ".not")).withStyle(Style.EMPTY.withColor(flag ? 0x108810 : 0xAA1616));
    }
}
