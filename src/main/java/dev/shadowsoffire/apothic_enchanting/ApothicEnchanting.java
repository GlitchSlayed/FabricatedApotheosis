package dev.shadowsoffire.apothic_enchanting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

import dev.shadowsoffire.apothic_enchanting.PowerFunction.DefaultMinPowerFunction;
import dev.shadowsoffire.apothic_enchanting.objects.TomeItem;
import dev.shadowsoffire.apothic_enchanting.payloads.CluePayload;
import dev.shadowsoffire.apothic_enchanting.payloads.SetRavenStatsPayload;
import dev.shadowsoffire.apothic_enchanting.payloads.StatsPayload;
import dev.shadowsoffire.apothic_enchanting.table.EnchantmentInfoLoader;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry;
import dev.shadowsoffire.apothic_enchanting.util.MiscUtil;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.payloads.ButtonClickPayload;
import dev.shadowsoffire.placebo.util.PlaceboTaskQueue;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class ApothicEnchanting {

    public static final String MODID = "apothic_enchanting";
    public static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis : Enchantment");

    public static final List<TomeItem> TYPED_BOOKS = new ArrayList<>();

    private static final String MIGRATED_CONFIG_NOTICE = """
        # Apotheosis Enchantment Information
        #
        # Per-enchantment configuration (max level, max loot level, level cap, and the min/max power functions)
        # no longer lives in this file. It has migrated to a data map:
        #
        #     data/apothic_enchanting/data_maps/enchantment/enchantment_info.json
        #
        # To capture the current effective values for every enchantment as a ready-to-edit data map file,
        # run the following command in-game (requires permission level 4):
        #
        #     /apoth dump_enchantment_info
        #
        # This file is intentionally left empty and is no longer read by the mod.
        """;

    public static void init() {
        Ench.bootstrap();
        writeMigratedConfigNotice();

        EnchantmentInfoLoader.init();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(EnchantmentInfoLoader.INSTANCE);
        EnchantingStatRegistry.INSTANCE.registerToBus();

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(MiscUtil::setCurrentServer);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> MiscUtil.setCurrentServer(null));

        net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity((tile, direction) -> tile.getItemStorage(direction), Ench.Tiles.LIBRARY);
        net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity((tile, direction) -> tile.getItemStorage(direction), Ench.Tiles.ENDER_LIBRARY);

        FabricDefaultAttributeRegistry.MODIFY.register(context -> {
            context.modify(EntityTypes.PLAYER, (type, builder) -> builder.add(Ench.Attributes.MAX_ETERNA));
        });

        PayloadHelper.registerPayload(new CluePayload.Provider());
        PayloadHelper.registerPayload(new StatsPayload.Provider());
        PayloadHelper.registerPayload(new SetRavenStatsPayload.Provider());
        PayloadHelper.registerPayload(new ButtonClickPayload.Provider());

        PlaceboTaskQueue.init();
        ApothEnchEvents.init();

        CreativeModeTabEvents.modifyOutputEvent(Ench.Tabs.ENCH.unwrapKey().get()).register(entries -> {
            entries.accept(Ench.Items.APOTHIC_ENCHANTING_TABLE.value());
            entries.accept(Ench.Items.RAVEN_ENCHANTING_TABLE.value());
            entries.accept(Ench.Items.BASIC_BOOKSHELF.value());
            entries.accept(Ench.Items.HELLSHELF.value());
            entries.accept(Ench.Items.INFUSED_HELLSHELF.value());
            entries.accept(Ench.Items.BLAZING_HELLSHELF.value());
            entries.accept(Ench.Items.GLOWING_HELLSHELF.value());
            entries.accept(Ench.Items.SEASHELF.value());
            entries.accept(Ench.Items.INFUSED_SEASHELF.value());
            entries.accept(Ench.Items.CRYSTAL_SEASHELF.value());
            entries.accept(Ench.Items.HEART_SEASHELF.value());
            entries.accept(Ench.Items.DORMANT_DEEPSHELF.value());
            entries.accept(Ench.Items.DEEPSHELF.value());
            entries.accept(Ench.Items.ECHOING_DEEPSHELF.value());
            entries.accept(Ench.Items.SOUL_TOUCHED_DEEPSHELF.value());
            entries.accept(Ench.Items.ECHOING_SCULKSHELF.value());
            entries.accept(Ench.Items.SOUL_TOUCHED_SCULKSHELF.value());
            entries.accept(Ench.Items.ENDSHELF.value());
            entries.accept(Ench.Items.PEARL_ENDSHELF.value());
            entries.accept(Ench.Items.DRACONIC_ENDSHELF.value());
            entries.accept(Ench.Items.BEESHELF.value());
            entries.accept(Ench.Items.MELONSHELF.value());
            entries.accept(Ench.Items.STONESHELF.value());
            entries.accept(Ench.Items.SIGHTSHELF.value());
            entries.accept(Ench.Items.SIGHTSHELF_T2.value());
            entries.accept(Ench.Items.FILTERING_SHELF.value());
            entries.accept(Ench.Items.TREASURE_SHELF.value());
            entries.accept(Ench.Items.GEODE_SHELF.value());
            entries.accept(Ench.Items.LIBRARY.value());
            entries.accept(Ench.Items.ENDER_LIBRARY.value());

            entries.accept(Ench.Items.HELMET_TOME.value());
            entries.accept(Ench.Items.CHESTPLATE_TOME.value());
            entries.accept(Ench.Items.LEGGINGS_TOME.value());
            entries.accept(Ench.Items.BOOTS_TOME.value());
            entries.accept(Ench.Items.WEAPON_TOME.value());
            entries.accept(Ench.Items.BOW_TOME.value());
            entries.accept(Ench.Items.PICKAXE_TOME.value());
            entries.accept(Ench.Items.FISHING_TOME.value());
            entries.accept(Ench.Items.OTHER_TOME.value());
            entries.accept(Ench.Items.SCRAP_TOME.value());
            entries.accept(Ench.Items.IMPROVED_SCRAP_TOME.value());
            entries.accept(Ench.Items.EXTRACTION_TOME.value());

            entries.accept(Ench.Items.PRISMATIC_WEB.value());
            entries.accept(Ench.Items.INERT_TRIDENT.value());
            entries.accept(Ench.Items.WARDEN_TENDRIL.value());
            entries.accept(Ench.Items.INFUSED_BREATH.value());
            entries.accept(Ench.Items.FLIMSY_ENDER_LEAD.value());
            entries.accept(Ench.Items.ENDER_LEAD.value());
            entries.accept(Ench.Items.OCCULT_ENDER_LEAD.value());
            entries.accept(Ench.Items.MUSIC_DISC_ETERNA.value());
            entries.accept(Ench.Items.MUSIC_DISC_QUANTA.value());
            entries.accept(Ench.Items.MUSIC_DISC_ARCANA.value());
        });
    }

    private static void writeMigratedConfigNotice() {
        Path file = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("apotheosis/enchantments.cfg");
        try {
            if (Files.exists(file) && MIGRATED_CONFIG_NOTICE.equals(Files.readString(file))) {
                return;
            }
            Files.createDirectories(file.getParent());
            Files.writeString(file, MIGRATED_CONFIG_NOTICE);
        } catch (IOException ex) {
            LOGGER.error("Failed to write the migrated enchantments.cfg notice", ex);
        }
    }

    public static EnchantmentInfo getEnchInfo(Holder<Enchantment> ench) {
        EnchantmentInfo info = EnchantmentInfoLoader.get(ench);
        if (info != null) return info;
        return EnchantmentInfo.fallback(ench);
    }

    private static final ConcurrentMap<Holder<Enchantment>, Integer> DEFAULT_MAX_LEVEL_CACHE = new MapMaker().weakKeys().makeMap();

    public static void clearEnchantmentCaches() {
        DEFAULT_MAX_LEVEL_CACHE.clear();
    }

    public static int getDefaultMaxLevel(Holder<Enchantment> ench) {
        return DEFAULT_MAX_LEVEL_CACHE.computeIfAbsent(ench, ApothicEnchanting::computeDefaultMaxLevel);
    }

    private static int computeDefaultMaxLevel(Holder<Enchantment> ench) {
        int level = ench.value().getMaxLevel();
        if (level == 1) return 1;
        DefaultMinPowerFunction minFunc = DefaultMinPowerFunction.INSTANCE;
        int max = 200;
        int minPower = minFunc.getPower(level, ench);
        if (minPower >= max) return level;
        int lastPower = minPower;
        while (minPower < max) {
            minPower = minFunc.getPower(++level, ench);
            if (lastPower == minPower) return level;
            if (minPower > max) return level - 1;
            lastPower = minPower;
        }
        return level;
    }

    private static ItemStack createEnchantedBook(EnchantmentInstance inst) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        stack.enchant(inst.enchantment(), inst.level());
        return stack;
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(langKey(type, path), args);
    }

    public static String langKey(String type, String path) {
        return type + "." + MODID + "." + path;
    }
}
