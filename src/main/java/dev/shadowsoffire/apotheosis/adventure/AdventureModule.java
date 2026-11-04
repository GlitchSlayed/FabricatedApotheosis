package dev.shadowsoffire.apotheosis.adventure;

import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.effect.AttributeToggleAffix;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingMenu;
import dev.shadowsoffire.apotheosis.commands.AffixCommand;
import dev.shadowsoffire.apotheosis.commands.BossCommand;
import dev.shadowsoffire.apotheosis.commands.CategoryCheckCommand;
import dev.shadowsoffire.apotheosis.commands.GemCommand;
import dev.shadowsoffire.apotheosis.commands.RarityCommand;
import dev.shadowsoffire.apotheosis.commands.ReforgeCommand;
import dev.shadowsoffire.apotheosis.commands.SocketCommand;
import dev.shadowsoffire.apotheosis.commands.WorldTierCommand;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.RarityOverrideRegistry;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.ApothMobEvents;
import dev.shadowsoffire.apotheosis.mobs.registries.AugmentRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.util.EntityModifier;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition;
import dev.shadowsoffire.apotheosis.net.AttributeTogglesPayload;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.net.GemCaseSelectPayload;
import dev.shadowsoffire.apotheosis.net.LinkItemToChatPayload;
import dev.shadowsoffire.apotheosis.net.RadialStatePayload;
import dev.shadowsoffire.apotheosis.net.RerollResultPayload;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
import dev.shadowsoffire.apotheosis.socket.gem.ExtraGemBonusRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.PurityWeightsRegistry;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawnerRegistry;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apotheosis.util.ModAttachments;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialState;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Apothic Adventure module initialization.
 */
public class AdventureModule {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Adventure");

    public static void init() {
        LOGGER.info("Adventure module initialized");

        Apoth.bootstrap();

        // Codecs
        EntityModifier.initCodecs();
        SpawnCondition.initCodecs();
        dev.shadowsoffire.apotheosis.loot.LootRule.initCodecs();
        dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus.initCodecs();

        // Dynamic registries
        RarityRegistry.INSTANCE.registerToBus();
        RarityOverrideRegistry.INSTANCE.registerToBus();
        AffixRegistry.INSTANCE.registerToBus();
        ExtraGemBonusRegistry.INSTANCE.registerToBus();
        GemRegistry.INSTANCE.registerToBus();
        AffixLootRegistry.INSTANCE.registerToBus();
        PurityWeightsRegistry.INSTANCE.registerToBus();
        TierAugmentRegistry.INSTANCE.registerToBus();
        GearSetRegistry.INSTANCE.registerToBus();
        InvaderRegistry.INSTANCE.registerToBus();
        EliteRegistry.INSTANCE.registerToBus();
        AugmentRegistry.INSTANCE.registerToBus();
        RogueSpawnerRegistry.INSTANCE.registerToBus();

        AffixLootRegistry.INSTANCE.dependsOn(AffixRegistry.INSTANCE);
        ExtraGemBonusRegistry.INSTANCE.dependsOn(GemRegistry.INSTANCE);
        InvaderRegistry.INSTANCE.dependsOn(AffixRegistry.INSTANCE).dependsOn(GearSetRegistry.INSTANCE);
        EliteRegistry.INSTANCE.dependsOn(AffixRegistry.INSTANCE).dependsOn(GearSetRegistry.INSTANCE);

        // Network payloads
        PayloadHelper.registerPayload(new BossSpawnPayload.Provider());
        PayloadHelper.registerPayload(new RerollResultPayload.Provider());
        PayloadHelper.registerPayload(new RadialStatePayload.Provider());
        PayloadHelper.registerPayload(new AttributeTogglesPayload.Provider());
        PayloadHelper.registerPayload(new WorldTierPayload.Provider());
        PayloadHelper.registerPayload(new AdventureConfig.ConfigPayload.Provider());
        PayloadHelper.registerPayload(new LinkItemToChatPayload.Provider());
        PayloadHelper.registerPayload(new GemCaseSelectPayload.Provider());

        // Global Loot Modifiers
        dev.shadowsoffire.apotheosis.loot.modifiers.LootModifierManager.init();

        // Fabric Transfer API ItemStorage automation for workstations
        try {
            ItemStorage.SIDED.registerForBlockEntity((be, dir) -> be.getItemHandler(), Apoth.Tiles.SALVAGING_TABLE);
            ItemStorage.SIDED.registerForBlockEntity((be, dir) -> be.getInventory().asStorage(), Apoth.Tiles.REFORGING_TABLE);
            ItemStorage.SIDED.registerForBlockEntity((be, dir) -> be.getInventory().asStorage(), Apoth.Tiles.AUGMENTING_TABLE);
            ItemStorage.SIDED.registerForBlockEntity((be, dir) -> be.getItemHandler(dir), Apoth.Tiles.GEM_CASE);
            ItemStorage.SIDED.registerForBlockEntity((be, dir) -> be.getItemHandler(dir), Apoth.Tiles.ENDER_GEM_CASE);
        }
        catch (Throwable ignored) {
            // Ignored in raw test runtime without Fabric mixin transformation
        }

        // Biome modifications for worldgen features
        registerBiomeFeatures();

        // Creative mode tabs
        registerCreativeTabs();

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = Commands.literal("apotheosis");
            RarityCommand.register(root);
            CategoryCheckCommand.register(root);
            ReforgeCommand.register(root);
            GemCommand.register(root);
            SocketCommand.register(root);
            AffixCommand.register(root);
            WorldTierCommand.register(root);
            BossCommand.register(root);
            dispatcher.register(root);
        });

        // Lifecycle & Progression events
        registerLifecycleEvents();
    }

    private static void registerBiomeFeatures() {
        Set<Identifier> blacklistedBiomes = Set.of(
            Identifier.parse("minecraft:warm_ocean"),
            Identifier.parse("minecraft:lukewarm_ocean"),
            Identifier.parse("minecraft:cold_ocean"),
            Identifier.parse("minecraft:frozen_ocean"),
            Identifier.parse("minecraft:deep_frozen_ocean"),
            Identifier.parse("minecraft:deep_lukewarm_ocean"),
            Identifier.parse("minecraft:deep_cold_ocean"),
            Identifier.parse("minecraft:ocean"),
            Identifier.parse("minecraft:deep_ocean"),
            Identifier.parse("minecraft:deep_dark")
        );

        Predicate<BiomeSelectionContext> selector = ctx -> !blacklistedBiomes.contains(ctx.getBiomeKey().identifier());

        ResourceKey<PlacedFeature> bossDungeon = ResourceKey.create(Registries.PLACED_FEATURE, Apotheosis.loc("boss_dungeon"));
        ResourceKey<PlacedFeature> bossDungeonDeep = ResourceKey.create(Registries.PLACED_FEATURE, Apotheosis.loc("boss_dungeon_deep"));
        ResourceKey<PlacedFeature> bossDungeon2 = ResourceKey.create(Registries.PLACED_FEATURE, Apotheosis.loc("boss_dungeon_2"));
        ResourceKey<PlacedFeature> bossDungeon2Deep = ResourceKey.create(Registries.PLACED_FEATURE, Apotheosis.loc("boss_dungeon_2_deep"));
        ResourceKey<PlacedFeature> rogueSpawner = ResourceKey.create(Registries.PLACED_FEATURE, Apotheosis.loc("rogue_spawner"));
        ResourceKey<PlacedFeature> rogueSpawnerDeep = ResourceKey.create(Registries.PLACED_FEATURE, Apotheosis.loc("rogue_spawner_deep"));

        BiomeModifications.addFeature(selector, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, bossDungeon);
        BiomeModifications.addFeature(selector, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, bossDungeonDeep);
        BiomeModifications.addFeature(selector, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, bossDungeon2);
        BiomeModifications.addFeature(selector, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, bossDungeon2Deep);
        BiomeModifications.addFeature(selector, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, rogueSpawner);
        BiomeModifications.addFeature(selector, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, rogueSpawnerDeep);
    }

    private static void registerLifecycleEvents() {
        // Player Join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            WorldTier tier = WorldTier.getTier(player);
            WorldTier max = ModAttachments.getData(player, Attachments.MAX_UNLOCKED_TIER);
            PayloadHelper.sendToPlayer(player, new WorldTierPayload(tier, max != null ? max : tier));
            PayloadHelper.sendToPlayer(player, new RadialStatePayload(RadialState.getState(player)));
            AttributeToggleAffix.prune(player);
            AttributeToggleAffix.sync(player);

            Boolean applied = ModAttachments.getData(player, Attachments.TIER_AUGMENTS_APPLIED);
            if (applied == null || !applied) {
                for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.PLAYERS)) {
                    aug.apply(player.level(), player);
                }
                ModAttachments.setData(player, Attachments.TIER_AUGMENTS_APPLIED, true);
            }
        });

        // Player Dimension Change
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            WorldTier max = ModAttachments.getData(player, Attachments.MAX_UNLOCKED_TIER);
            WorldTier current = WorldTier.getTier(player);
            PayloadHelper.sendToPlayer(player, new WorldTierPayload(current, max != null ? max : current));
        });

        // Player Clone (Respawn)
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            int oldSeed = AttributesUtil.getPersistentData(oldPlayer).getIntOr(ReforgingMenu.REFORGE_SEED, 0);
            AttributesUtil.getPersistentData(newPlayer).putInt(ReforgingMenu.REFORGE_SEED, oldSeed);

            WorldTier tier = ModAttachments.getData(oldPlayer, Attachments.WORLD_TIER);
            if (tier != null) {
                ModAttachments.setData(newPlayer, Attachments.WORLD_TIER, tier);
            }
            WorldTier maxTier = ModAttachments.getData(oldPlayer, Attachments.MAX_UNLOCKED_TIER);
            if (maxTier != null) {
                ModAttachments.setData(newPlayer, Attachments.MAX_UNLOCKED_TIER, maxTier);
            }
            ModAttachments.setData(newPlayer, Attachments.TIER_AUGMENTS_APPLIED, true);
            WorldTier current = WorldTier.getTier(newPlayer);
            PayloadHelper.sendToPlayer(newPlayer, new WorldTierPayload(current, maxTier != null ? maxTier : current));
        });

        // Entity Load into Level
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Mob mob) {
                ApothMobEvents.delayedEliteMobs(mob, level);

                Boolean applied = ModAttachments.getData(mob, Attachments.TIER_AUGMENTS_APPLIED);
                if (applied == null || !applied) {
                    Player nearest = level.getNearestPlayer(mob, -1);
                    if (nearest != null) {
                        WorldTier tier = WorldTier.getTier(nearest);
                        float healthPct = mob.getHealth() / mob.getMaxHealth();
                        for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.MONSTERS)) {
                            aug.apply(level, mob);
                        }
                        ModAttachments.setData(mob, Attachments.TIER_AUGMENTS_APPLIED, true);
                        mob.setHealth(healthPct * mob.getMaxHealth());
                    }
                }
            }
        });

        // Equipment Change
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, from, to) -> {
            if (entity instanceof ServerPlayer player) {
                Apoth.Triggers.EQUIPPED_ITEM.trigger(player, slot, to);
                if (slot == EquipmentSlot.FEET) {
                    AttributeToggleAffix.prune(player);
                }
            }
        });
    }

    private static void registerCreativeTabs() {
        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(Apoth.Tabs.ADVENTURE.unwrapKey().get()).register(entries -> {
            entries.accept(Apoth.Items.MYSTERIOUS_SCRAP_METAL.value());
            entries.accept(Apoth.Items.TIMEWORN_FABRIC.value());
            entries.accept(Apoth.Items.LUMINOUS_CRYSTAL_SHARD.value());
            entries.accept(Apoth.Items.ARCANE_SANDS.value());
            entries.accept(Apoth.Items.GODFORGED_PEARL.value());
            entries.accept(Apoth.Items.GOD_FUSED_PEARL.value());
            entries.accept(Apoth.Items.GEM_DUST.value());
            entries.accept(Apoth.Items.GEM_FUSED_SLATE.value());

            entries.accept(Apoth.Items.SIGIL_OF_SOCKETING.value());
            entries.accept(Apoth.Items.SIGIL_OF_WITHDRAWAL.value());
            entries.accept(Apoth.Items.SIGIL_OF_REBIRTH.value());
            entries.accept(Apoth.Items.SIGIL_OF_ENHANCEMENT.value());
            entries.accept(Apoth.Items.SIGIL_OF_UNNAMING.value());
            entries.accept(Apoth.Items.SIGIL_OF_MALICE.value());
            entries.accept(Apoth.Items.SIGIL_OF_SUPREMACY.value());

            entries.accept(Apoth.Items.SALVAGING_TABLE.value());
            entries.accept(Apoth.Items.GEM_CUTTING_TABLE.value());
            entries.accept(Apoth.Items.SIMPLE_REFORGING_TABLE.value());
            entries.accept(Apoth.Items.REFORGING_TABLE.value());
            entries.accept(Apoth.Items.AUGMENTING_TABLE.value());
            entries.accept(Apoth.Items.GEM_CASE.value());
            entries.accept(Apoth.Items.ENDER_GEM_CASE.value());

            entries.accept(Apoth.Items.IRON_UPGRADE_SMITHING_TEMPLATE.value());
            entries.accept(Apoth.Items.GOLD_UPGRADE_SMITHING_TEMPLATE.value());
            entries.accept(Apoth.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE.value());

            entries.accept(Apoth.Items.MUSIC_DISC_FLASH.value());
            entries.accept(Apoth.Items.MUSIC_DISC_GLIMMER.value());
            entries.accept(Apoth.Items.MUSIC_DISC_SHIMMER.value());

            if (dev.shadowsoffire.apotheosis.config.ApotheosisConfig.getServer().enableSpawners) {
                entries.accept(Apoth.Items.SPAWNER_CHAIN.value());
                entries.accept(Apoth.Items.SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.INFUSED_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.FRONTIER_SPAWNER_UPGRADE_RUNE.value());
                entries.accept(Apoth.Items.ASCENT_SPAWNER_UPGRADE_RUNE.value());
                entries.accept(Apoth.Items.SUMMIT_SPAWNER_UPGRADE_RUNE.value());
                entries.accept(Apoth.Items.PINNACLE_SPAWNER_UPGRADE_RUNE.value());
                entries.accept(Apoth.Items.SPAWN_RANGE_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.REDSTONE_CONTROL_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.IGNORE_LIGHT_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.INITIAL_HEALTH_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.SILENT_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.YOUTHFUL_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.BURNING_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.NO_AI_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.IGNORE_CONDITIONS_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.IGNORE_PLAYERS_SPAWNER_RUNE.value());
                entries.accept(Apoth.Items.ECHOING_SPAWNER_RUNE.value());
            }

            net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event = new net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent(entries);
            if (Apoth.Items.GEM.value() instanceof dev.shadowsoffire.placebo.tabs.ITabFiller filler) {
                filler.fillItemCategory(Apoth.Tabs.ADVENTURE.value(), event);
            }
            if (GemRegistry.INSTANCE.getValues().isEmpty()) {
                entries.accept(Apoth.Items.GEM.value());
            }

            entries.accept(Apoth.Items.BOSS_SUMMONER.value());
        });

        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event = new net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent(entries);
            if (Apoth.Items.POTION_CHARM.value() instanceof dev.shadowsoffire.placebo.tabs.ITabFiller filler) {
                filler.fillItemCategory(null, event);
            }
        });
    }
}
