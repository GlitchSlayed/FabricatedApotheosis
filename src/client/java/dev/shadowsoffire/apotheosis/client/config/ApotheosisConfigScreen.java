package dev.shadowsoffire.apotheosis.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apotheosis.config.ClientConfig;
import dev.shadowsoffire.apotheosis.config.ServerConfig;
import dev.shadowsoffire.placebo.util.Offset;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Client-only configuration screen built with YetAnotherConfigLib (YACL).
 * <p>
 * Strictly segregated in the client source set. Automatically displays authoritative
 * server settings as read-only when connected to a remote multiplayer server, while
 * keeping client preferences fully editable.
 */
public class ApotheosisConfigScreen {

    private static final Component LIVE_IMPACT = Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA);
    private static final Component RESTART_IMPACT = Component.literal("[Impact: Requires Restart]").withStyle(ChatFormatting.RED);

    public static Screen create(Screen parent) {
        boolean isRemote = ApotheosisConfig.isRemoteServer();
        ClientConfig client = ApotheosisConfig.getClient();
        ServerConfig server = ApotheosisConfig.getServer();

        Component serverNotice = isRemote
            ? Component.literal("[Remote Server Authoritative] Settings are enforced by the server and cannot be edited locally.").withStyle(ChatFormatting.YELLOW)
            : Component.literal("[Local Singleplayer / Dedicated Server] Changes will be saved locally.").withStyle(ChatFormatting.GREEN);

        return YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Fabricated Apotheosis Configuration"))
            .save(() -> {
                ApotheosisConfig.saveClient();
                if (!ApotheosisConfig.isRemoteServer()) {
                    ApotheosisConfig.saveLocalServer();
                }
            })
            .category(buildClientCategory(client))
            .category(buildAdventureCategory(server, isRemote, serverNotice))
            .category(buildWorldgenCategory(server, isRemote, serverNotice))
            .category(buildSpawnersAndEnchantingCategory(server, isRemote, serverNotice))
            .category(buildAttributesCategory(server, isRemote, serverNotice))
            .category(buildModuleTogglesCategory(server, isRemote, serverNotice))
            .build()
            .generateScreen(parent);
    }

    private static ConfigCategory buildClientCategory(ClientConfig client) {
        ClientConfig defaults = new ClientConfig();

        OptionGroup visualsGroup = OptionGroup.createBuilder()
            .name(Component.literal("Visuals, Tooltips & QoL"))
            .description(OptionDescription.of(Component.literal("Personal rendering, tooltip verbosity, and HUD flair toggles.")))
            .option(clientBool("Boss Health Bar Glow",
                "Renders a visual glow effect behind Apotheosis boss health bars.",
                defaults.bossHealthBarGlow, () -> client.bossHealthBarGlow, v -> client.bossHealthBarGlow = v))
            .option(clientBool("Sculk Shelf Particles",
                "Enables ambient soul particles emanating from active sculkshelves.",
                defaults.sculkShelfParticles, () -> client.sculkShelfParticles, v -> client.sculkShelfParticles = v))
            .option(clientInt("Sculk Shelf Noise Chance",
                "The 1/n chance per tick that a sculkshelf plays an ambient sound (0 to disable).",
                defaults.sculkShelfNoiseChance, () -> client.sculkShelfNoiseChance, v -> client.sculkShelfNoiseChance = v, 0, 2000, 20))
            .option(clientBool("Enable Affix Item Effects",
                "Renders loot beams, colored particles, and custom shadows for dropped affix items.",
                defaults.enableAffixItemEffects, () -> client.enableAffixItemEffects, v -> client.enableAffixItemEffects = v))
            .option(clientBool("Display Attribute Deltas",
                "Displays comparative stat differences (e.g. +2.5 Armor) in item tooltips.",
                defaults.displayAttributeDeltas, () -> client.displayAttributeDeltas, v -> client.displayAttributeDeltas = v))
            .option(clientBool("Enable Potion Tooltips",
                "Displays detailed effect descriptions and attribute breakdowns in potion & status effect tooltips.",
                defaults.enablePotionTooltips, () -> client.enablePotionTooltips, v -> client.enablePotionTooltips = v))
            .option(clientBool("Show Enchanted Book Metadata",
                "Shows treasure, tradeable, and discoverable metadata on enchanted books.",
                defaults.showEnchantedBookMetadata, () -> client.showEnchantedBookMetadata, v -> client.showEnchantedBookMetadata = v))
            .option(clientBool("Enable Inline Enchantment Descriptions",
                "Shows inline descriptions for enchanted books when no standalone description mod is present.",
                defaults.enableInlineEnchDescs, () -> client.enableInlineEnchDescs, v -> client.enableInlineEnchDescs = v))
            .option(clientBool("Detailed Spawner Tooltips",
                "Shows modified spawner stat values and limits when hovering spawner items.",
                defaults.detailedSpawnerTooltips, () -> client.detailedSpawnerTooltips, v -> client.detailedSpawnerTooltips = v))
            .option(clientBool("Enable Equipment Comparisons",
                "Shows side-by-side comparison tooltips against equipped gear when holding the compare hotkey.",
                defaults.enableEquipmentCompare, () -> client.enableEquipmentCompare, v -> client.enableEquipmentCompare = v))
            .option(clientBool("Disable Third-Party Attribute Tooltips on Affix Items",
                "Prevents conflicting third-party attribute tooltip handlers on Apotheosis affix gear.",
                defaults.disableQuarkOnAffixItems, () -> client.disableQuarkOnAffixItems, v -> client.disableQuarkOnAffixItems = v))
            .build();

        Option<Offset.AnchorPoint> anchorOpt = Option.<Offset.AnchorPoint>createBuilder()
            .name(Component.literal("Attributes Button Anchor Point"))
            .description(OptionDescription.of(
                Component.literal("Corner or edge anchor used to position the Attributes button relative to the inventory screen."),
                LIVE_IMPACT))
            .binding(Offset.AnchorPoint.TOP_RIGHT, client::getAnchorPoint, client::setAnchorPoint)
            .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Offset.AnchorPoint.class))
            .build();

        OptionGroup attributesGuiGroup = OptionGroup.createBuilder()
            .name(Component.literal("Attributes Inventory Overlay"))
            .description(OptionDescription.of(Component.literal("Placement and behavior of the inventory attributes inspection panel.")))
            .option(clientBool("Enable Attributes GUI",
                "Adds the Attributes GUI toggle button to the player inventory screen.",
                defaults.enableAttributesGui, () -> client.enableAttributesGui, v -> client.enableAttributesGui = v))
            .option(anchorOpt)
            .option(clientInt("Attributes GUI Offset X",
                "Horizontal pixel offset of the Attributes button on the inventory screen.",
                defaults.attributesGuiOffsetX, () -> client.attributesGuiOffsetX, v -> client.attributesGuiOffsetX = v, -500, 500, 1))
            .option(clientInt("Attributes GUI Offset Y",
                "Vertical pixel offset of the Attributes button on the inventory screen.",
                defaults.attributesGuiOffsetY, () -> client.attributesGuiOffsetY, v -> client.attributesGuiOffsetY = v, -500, 500, 1))
            .build();

        ListOption<String> hiddenAttributesList = ListOption.<String>createBuilder()
            .name(Component.literal("Hidden Attributes List"))
            .description(OptionDescription.of(
                Component.literal("Attribute IDs (or 'namespace:*', prefix with '!' to force show) hidden in the inventory Attributes GUI."),
                LIVE_IMPACT))
            .binding(List.copyOf(defaults.hiddenAttributes),
                () -> new ArrayList<>(client.hiddenAttributes),
                val -> client.hiddenAttributes = new ArrayList<>(val))
            .controller(StringControllerBuilder::create)
            .initial("")
            .collapsed(true)
            .build();

        return ConfigCategory.createBuilder()
            .name(Component.literal("Client Preferences"))
            .tooltip(Component.literal("Personal visual preferences, tooltip detail levels, and screen positioning."))
            .group(visualsGroup)
            .group(attributesGuiGroup)
            .group(hiddenAttributesList)
            .build();
    }

    private static ConfigCategory buildAdventureCategory(ServerConfig server, boolean isRemote, Component notice) {
        ServerConfig defaults = new ServerConfig();
        boolean editable = !isRemote;

        OptionGroup bossesGroup = OptionGroup.createBuilder()
            .name(Component.literal("Bosses & Invaders"))
            .description(OptionDescription.of(Component.literal("Natural boss invader spawning, aggro, curses, and special boss tiers.")))
            .option(serverInt("Boss Spawn Cooldown (Ticks)",
                "Ticks before a player may trigger another natural Apothic Invader spawn.",
                defaults.bossSpawnCooldown, () -> server.bossSpawnCooldown, v -> server.bossSpawnCooldown = v, 0, 720000, 100, editable, notice))
            .option(serverFloat("Boss Announce Range (Blocks)",
                "Horizontal block radius within which nearby players receive boss spawn announcements and beams.",
                defaults.bossAnnounceRange, () -> server.bossAnnounceRange, v -> server.bossAnnounceRange = v, 0.0F, 1024.0F, 4.0F, editable, notice))
            .option(serverBool("Boss Glowing On Spawn",
                "Applies the Glowing outline effect to newly spawned Apothic Invaders.",
                defaults.bossGlowOnSpawn, () -> server.bossGlowOnSpawn, v -> server.bossGlowOnSpawn = v, editable, notice))
            .option(serverBool("Boss Auto-Aggro",
                "If enabled, natural Apothic Invaders immediately target the nearest non-creative player on spawn.",
                defaults.bossAutoAggro, () -> server.bossAutoAggro, v -> server.bossAutoAggro = v, editable, notice))
            .option(serverBool("Curse Boss Items",
                "Always applies a random curse enchantment to boss-dropped affix gear.",
                defaults.curseBossItems, () -> server.curseBossItems, v -> server.curseBossItems = v, editable, notice))
            .option(serverInt("Golem Boss Despawn Delay (Ticks)",
                "Ticks before far-away golem bosses (e.g. Iron Golem invaders) are eligible to despawn naturally.",
                defaults.golemBossDespawnTicks, () -> server.golemBossDespawnTicks, v -> server.golemBossDespawnTicks = v, 200, 720000, 200, editable, notice))
            .option(serverBool("Apply World Tier to Ender Dragon & Wither",
                "Applies Apotheosis world-tier scaling and elite/augment rules when the Ender Dragon or Wither spawns.",
                defaults.applyWorldTierToEndAndWither, () -> server.applyWorldTierToEndAndWither, v -> server.applyWorldTierToEndAndWither = v, editable, notice))
            .option(serverBool("Skeletons Shoot Crossbows",
                "Allows skeletons and skeleton bosses equipped with crossbows to fire them with proper AI instead of meleeing.",
                defaults.enableSkeletonCrossbows, () -> server.enableSkeletonCrossbows, v -> server.enableSkeletonCrossbows = v, editable, notice))
            .build();

        OptionGroup affixesGroup = OptionGroup.createBuilder()
            .name(Component.literal("Affixes & Gems"))
            .description(OptionDescription.of(Component.literal("Combat affix rules and in-world gem interactions.")))
            .option(serverBool("Cleave Hits Players",
                "Allows the Cleaving affix to damage non-user players within cleave radius.",
                defaults.cleaveHitsPlayers, () -> server.cleaveHitsPlayers, v -> server.cleaveHitsPlayers = v, editable, notice))
            .option(serverString("Enlightened Torch Placement Item",
                "Item ID placed by right-clicking with the Enlightened affix (falls back to minecraft:torch if invalid).",
                defaults.torchItem, () -> server.torchItem, v -> server.torchItem = v, editable, notice))
            .option(serverBool("Anvil Gem Smashing",
                "Allows falling anvils to crush dropped gems into Gem Dust.",
                defaults.enableGemSmashing, () -> server.enableGemSmashing, v -> server.enableGemSmashing = v, editable, notice))
            .build();

        OptionGroup augmentingGroup = OptionGroup.createBuilder()
            .name(Component.literal("Augmenting Table"))
            .description(OptionDescription.of(Component.literal("Sigil and experience costs for affix upgrades and rerolls.")))
            .option(serverInt("Upgrade Sigil Cost",
                "Number of Sigils of Enhancement consumed when upgrading an affix tier in the Augmenting Table.",
                defaults.upgradeSigilCost, () -> server.upgradeSigilCost, v -> server.upgradeSigilCost = v, 0, 64, 1, editable, notice))
            .option(serverInt("Upgrade XP Level Cost",
                "Equivalent experience levels charged when upgrading an affix in the Augmenting Table.",
                defaults.upgradeLevelCost, () -> server.upgradeLevelCost, v -> server.upgradeLevelCost = v, 0, 65536, 5, editable, notice))
            .option(serverInt("Reroll Sigil Cost",
                "Number of Sigils of Enhancement consumed when rerolling an affix in the Augmenting Table.",
                defaults.rerollSigilCost, () -> server.rerollSigilCost, v -> server.rerollSigilCost = v, 0, 64, 1, editable, notice))
            .option(serverInt("Reroll XP Level Cost",
                "Equivalent experience levels charged when rerolling an affix in the Augmenting Table.",
                defaults.rerollLevelCost, () -> server.rerollLevelCost, v -> server.rerollLevelCost = v, 0, 65536, 5, editable, notice))
            .build();

        OptionGroup qolGroup = OptionGroup.createBuilder()
            .name(Component.literal("World Tiers & Quality of Life"))
            .description(OptionDescription.of(Component.literal("Player World Tier switching permissions and item chat linking limits.")))
            .option(serverBool("Allow Manual World Tier Changes",
                "Allows players to freely switch between unlocked World Tiers from the World Tier screen.",
                defaults.enableManualWorldTierChanges, () -> server.enableManualWorldTierChanges, v -> server.enableManualWorldTierChanges = v, editable, notice))
            .option(serverBool("Enable Item Linking",
                "Allows players to share hovered items into multiplayer chat via the link keybind.",
                defaults.enableItemLinking, () -> server.enableItemLinking, v -> server.enableItemLinking = v, editable, notice))
            .option(serverInt("Item Linking Cooldown (Ticks)",
                "Minimum ticks a player must wait between linking items into chat.",
                defaults.itemLinkingCooldown, () -> server.itemLinkingCooldown, v -> server.itemLinkingCooldown = v, 0, 65536, 10, editable, notice))
            .build();

        return ConfigCategory.createBuilder()
            .name(Component.literal("Gameplay: Adventure & Bosses"))
            .tooltip(Component.literal("Server-authoritative invader bosses, affixes, Augmenting Table costs, and World Tiers."))
            .group(bossesGroup)
            .group(affixesGroup)
            .group(augmentingGroup)
            .group(qolGroup)
            .build();
    }

    private static ConfigCategory buildWorldgenCategory(ServerConfig server, boolean isRemote, Component notice) {
        ServerConfig defaults = new ServerConfig();
        boolean editable = !isRemote;

        OptionGroup worldgenGroup = OptionGroup.createBuilder()
            .name(Component.literal("World Generation"))
            .description(OptionDescription.of(Component.literal("Boss dungeon and rogue spawner generation settings.")))
            .option(serverFloat("Rogue Spawner Valuable Chest Chance",
                "Probability (0.0 to 1.0) that a rogue spawner generates with a valuable chest loot table.",
                defaults.spawnerValueChance, () -> server.spawnerValueChance, v -> server.spawnerValueChance = v, 0.0F, 1.0F, 0.01F, editable, notice))
            .option(serverBool("WorldGen Debug Logging",
                "Logs coordinates whenever an Apotheosis boss dungeon or rogue spawner generates.",
                defaults.worldGenDebug, () -> server.worldGenDebug, v -> server.worldGenDebug = v, editable, notice))
            .build();

        OptionGroup wandererGroup = OptionGroup.createBuilder()
            .name(Component.literal("Wandering Trader"))
            .description(OptionDescription.of(Component.literal("Overworld Wandering Trader spawn chances, underground spawning, and lifetime.")))
            .option(serverBool("Underground Wandering Trader",
                "Allows Wandering Traders to attempt spawning underground near cave-exploring players.",
                defaults.undergroundTrader, () -> server.undergroundTrader, v -> server.undergroundTrader = v, editable, notice))
            .option(serverInt("Wanderer Max Spawn Chance (%)",
                "Maximum accumulated spawn probability ceiling used by WanderingTraderSpawner.",
                defaults.wandererMaxChance, () -> server.wandererMaxChance, v -> server.wandererMaxChance = v, 1, 100, 1, editable, notice))
            .option(serverInt("Wanderer Spawn Roll Bound",
                "Upper 1-in-N random bound when a Wandering Trader attempts to spawn (lower = more frequent).",
                defaults.wandererRngRoll, () -> server.wandererRngRoll, v -> server.wandererRngRoll = v, 1, 100, 1, editable, notice))
            .option(serverInt("Wanderer Despawn Delay (Ticks)",
                "Initial despawn timer in ticks assigned to newly spawned Wandering Traders.",
                defaults.wandererDespawnDelay, () -> server.wandererDespawnDelay, v -> server.wandererDespawnDelay = v, 1200, 240000, 400, editable, notice))
            .build();

        ListOption<String> dimensionWhitelist = ListOption.<String>createBuilder()
            .name(Component.literal("Generation Dimension Whitelist"))
            .description(OptionDescription.of(
                Component.literal("Dimension IDs (e.g. 'minecraft:overworld') where boss dungeons and rogue spawners may generate."),
                notice,
                LIVE_IMPACT))
            .binding(List.copyOf(defaults.generationDimensionWhitelist),
                () -> new ArrayList<>(server.generationDimensionWhitelist),
                val -> server.generationDimensionWhitelist = new ArrayList<>(val))
            .controller(StringControllerBuilder::create)
            .initial("minecraft:overworld")
            .available(editable)
            .collapsed(false)
            .build();

        return ConfigCategory.createBuilder()
            .name(Component.literal("Gameplay: Worldgen & Wanderer"))
            .tooltip(Component.literal("Server-authoritative dimension whitelists, dungeon chest odds, and Wandering Trader behavior."))
            .group(worldgenGroup)
            .group(wandererGroup)
            .group(dimensionWhitelist)
            .build();
    }

    private static ConfigCategory buildSpawnersAndEnchantingCategory(ServerConfig server, boolean isRemote, Component notice) {
        ServerConfig defaults = new ServerConfig();
        boolean editable = !isRemote;

        OptionGroup spawnersGroup = OptionGroup.createBuilder()
            .name(Component.literal("Spawners"))
            .description(OptionDescription.of(Component.literal("Silk Touch spawner harvesting, speed bounds, instability, and Capturing odds.")))
            .option(serverInt("Silk Touch Harvest Level",
                "Level of Silk Touch needed to harvest a spawner (-1 to disable, 0 for always).",
                defaults.spawnerSilkLevel, () -> server.spawnerSilkLevel, v -> server.spawnerSilkLevel = v, -1, 10, 1, editable, notice))
            .option(serverInt("Silk Touch Tool Damage",
                "Durability damage dealt to a tool when harvesting a monster spawner.",
                defaults.spawnerSilkDamage, () -> server.spawnerSilkDamage, v -> server.spawnerSilkDamage = v, 0, 10000, 25, editable, notice))
            .option(serverBool("Spawners Drop Empty",
                "If true, harvested spawners clear their stored entity type upon drop.",
                defaults.spawnersDropEmpty, () -> server.spawnersDropEmpty, v -> server.spawnersDropEmpty = v, editable, notice))
            .option(serverInt("Entity Despawn Delay (Ticks)",
                "Time in ticks spawner mobs are immune to despawning after spawning.",
                defaults.entityDespawnDelay, () -> server.entityDespawnDelay, v -> server.entityDespawnDelay = v, 0, 24000, 100, editable, notice))
            .option(serverBool("Redstone Activation Control",
                "Allows players to toggle spawner activation with redstone signals.",
                defaults.spawnerRedstoneControl, () -> server.spawnerRedstoneControl, v -> server.spawnerRedstoneControl = v, editable, notice))
            .option(serverInt("Minimum Spawn Delay Floor",
                "The absolute lowest spawn delay (in ticks) a spawner can be modified to reach.",
                defaults.minSpawnDelayLimit, () -> server.minSpawnDelayLimit, v -> server.minSpawnDelayLimit = v, 0, 200, 5, editable, notice))
            .option(serverInt("Maximum Spawn Delay Ceiling",
                "The highest max spawn delay (in ticks) permitted after modifier applications.",
                defaults.maxSpawnDelayLimit, () -> server.maxSpawnDelayLimit, v -> server.maxSpawnDelayLimit = v, 200, 5000, 50, editable, notice))
            .option(serverInt("Spawner Instability Duration (Ticks)",
                "Fuse duration before an unstable spawner detonates.",
                defaults.spawnerInstabilityDurationTicks, () -> server.spawnerInstabilityDurationTicks, v -> server.spawnerInstabilityDurationTicks = v, 10, 1200, 10, editable, notice))
            .option(serverFloat("Unstable Spawner Explosion Radius",
                "Blast strength when an unstable spawner detonates.",
                defaults.spawnerInstabilityExplosionRadius, () -> server.spawnerInstabilityExplosionRadius, v -> server.spawnerInstabilityExplosionRadius = v, 0.0F, 32.0F, 0.5F, editable, notice))
            .option(serverInt("Unstable Spawner Mob Burst Count",
                "Number of mobs burst-spawned when an unstable spawner detonates.",
                defaults.spawnerUnstableMobCount, () -> server.spawnerUnstableMobCount, v -> server.spawnerUnstableMobCount = v, 0, 64, 1, editable, notice))
            .option(serverFloat("Capturing Spawn Egg Chance Per Level",
                "Baseline Capturing drop rate per enchantment level (0.005 = 0.5% per level).",
                defaults.capturingDropChancePerLevel, () -> server.capturingDropChancePerLevel, v -> server.capturingDropChancePerLevel = v, 0.0F, 1.0F, 0.001F, editable, notice))
            .build();

        OptionGroup enchantingGroup = OptionGroup.createBuilder()
            .name(Component.literal("Enchanting & Anvil"))
            .description(OptionDescription.of(Component.literal("Level caps, bookshelf detection range, anvil costs, and enchantment rules.")))
            .option(serverInt("Maximum Enchantment Level Cap",
                "The highest allowed level cap for any enchantment in the ecosystem.",
                defaults.maxEnchantmentLevelLimit, () -> server.maxEnchantmentLevelLimit, v -> server.maxEnchantmentLevelLimit = v, 10, 255, 5, editable, notice))
            .option(serverInt("Bookshelf Detection Range",
                "Horizontal distance around the enchanting table searched for valid bookshelves.",
                defaults.maxBookshelfRange, () -> server.maxBookshelfRange, v -> server.maxBookshelfRange = v, 1, 5, 1, editable, notice))
            .option(serverInt("Prismatic Web Curse Removal XP Cost",
                "Experience level cost to strip curses from an item with a Prismatic Web in an anvil.",
                defaults.prismaticWebCost, () -> server.prismaticWebCost, v -> server.prismaticWebCost = v, 0, 255, 1, editable, notice))
            .option(serverInt("Anvil Iron Block Repair XP Cost",
                "Experience level cost to repair a Chipped or Damaged Anvil with an Iron Block.",
                defaults.anvilRepairXpCost, () -> server.anvilRepairXpCost, v -> server.anvilRepairXpCost = v, 0, 255, 1, editable, notice))
            .option(serverBool("Allow Trident Combat Enchantments",
                "Allows Sharpness, Looting, and Piercing to be applied to Tridents.",
                defaults.allowTridentCombatEnchantments, () -> server.allowTridentCombatEnchantments, v -> server.allowTridentCombatEnchantments = v, editable, notice))
            .option(serverInt("Chainsaw Breaks Per Tick",
                "Maximum connected log blocks felled per chainsaw interval.",
                defaults.chainsawBreaksPerTick, () -> server.chainsawBreaksPerTick, v -> server.chainsawBreaksPerTick = v, 1, 64, 1, editable, notice))
            .build();

        return ConfigCategory.createBuilder()
            .name(Component.literal("Gameplay: Spawners & Enchanting"))
            .tooltip(Component.literal("Server-authoritative spawner mechanics, enchanting limits, and anvil behaviors."))
            .group(spawnersGroup)
            .group(enchantingGroup)
            .build();
    }

    private static ConfigCategory buildAttributesCategory(ServerConfig server, boolean isRemote, Component notice) {
        ServerConfig defaults = new ServerConfig();
        boolean editable = !isRemote;

        OptionGroup defenseGroup = OptionGroup.createBuilder()
            .name(Component.literal("Armor, Toughness & Protection"))
            .description(OptionDescription.of(Component.literal("Server-authoritative combat mitigation curves and bypass resistance caps.")))
            .option(serverFloat("Negative Armor Factor",
                "Damage amplification fraction per negative armor point.",
                defaults.negativeArmorFactor, () -> server.negativeArmorFactor, v -> server.negativeArmorFactor = v, 0.0F, 0.1F, 0.005F, editable, notice))
            .option(serverFloat("Protection Reduction Per Point",
                "Damage reduction fraction gained per Protection point (default 0.025 = 2.5%).",
                defaults.protectionFactorPerPoint, () -> server.protectionFactorPerPoint, v -> server.protectionFactorPerPoint = v, 0.001F, 0.20F, 0.001F, editable, notice))
            .option(serverFloat("Maximum Protection Cap",
                "Upper cap on total damage reduction achievable via Protection points (default 0.85 = 85%).",
                defaults.protectionMaxCap, () -> server.protectionMaxCap, v -> server.protectionMaxCap = v, 0.0F, 0.99F, 0.01F, editable, notice))
            .option(serverFloat("Toughness Bypass Resist Per Point",
                "Fraction of incoming armor pierce/shred resisted per point of Armor Toughness (default 0.02 = 2%).",
                defaults.toughnessBypassResistPerPoint, () -> server.toughnessBypassResistPerPoint, v -> server.toughnessBypassResistPerPoint = v, 0.0F, 0.20F, 0.005F, editable, notice))
            .option(serverFloat("Maximum Toughness Bypass Resist Cap",
                "Upper cap on how much armor bypass Armor Toughness can resist (default 0.60 = 60%).",
                defaults.toughnessMaxBypassResist, () -> server.toughnessMaxBypassResist, v -> server.toughnessMaxBypassResist = v, 0.0F, 1.0F, 0.01F, editable, notice))
            .build();

        OptionGroup offenseAndXpGroup = OptionGroup.createBuilder()
            .name(Component.literal("Crits, Overheal & Experience"))
            .description(OptionDescription.of(Component.literal("Critical strike scaling, overheal absorption caps, and Ancient Knowledge XP.")))
            .option(serverFloat("Critical Damage Modifier Cap",
                "Upper threshold multiplier cap on critical strike bonus damage.",
                defaults.critDamageModifierCap, () -> server.critDamageModifierCap, v -> server.critDamageModifierCap = v, 1.0F, 100.0F, 0.5F, editable, notice))
            .option(serverFloat("Multi-Crit Degradation Factor",
                "Multiplier applied to critical bonus damage on each chained crit tier above 100% Crit Chance.",
                defaults.multicritDegradationFactor, () -> server.multicritDegradationFactor, v -> server.multicritDegradationFactor = v, 0.10F, 1.0F, 0.01F, editable, notice))
            .option(serverFloat("Overheal Max Absorption Ratio",
                "Maximum Overheal absorption shield as a fraction of the entity's max health (default 0.50 = 50%).",
                defaults.overhealMaxAbsorptionRatio, () -> server.overhealMaxAbsorptionRatio, v -> server.overhealMaxAbsorptionRatio = v, 0.0F, 5.0F, 0.05F, editable, notice))
            .option(serverFloat("Ancient Knowledge XP Multiplier",
                "Experience Gained modifier per level of the Ancient Knowledge effect.",
                defaults.knowledgeMultiplier, () -> server.knowledgeMultiplier, v -> server.knowledgeMultiplier = v, 1.0F, 50.0F, 0.5F, editable, notice))
            .build();

        return ConfigCategory.createBuilder()
            .name(Component.literal("Gameplay: Attributes & Combat"))
            .tooltip(Component.literal("Server-authoritative combat math formulas, armor bypass curves, and multipliers."))
            .group(defenseGroup)
            .group(offenseAndXpGroup)
            .build();
    }

    private static ConfigCategory buildModuleTogglesCategory(ServerConfig server, boolean isRemote, Component notice) {
        ServerConfig defaults = new ServerConfig();
        boolean editable = !isRemote;

        return ConfigCategory.createBuilder()
            .name(Component.literal("Module Toggles"))
            .tooltip(Component.literal("Enable or disable major ecosystem modules. Requires restart."))
            .option(moduleToggle("Enable Apothic Spawners Module",
                "Enables spawner stat modification, silk touch capture, and modifier items.",
                defaults.enableSpawners, () -> server.enableSpawners, v -> server.enableSpawners = v, editable, notice))
            .option(moduleToggle("Enable Apothic Attributes Module",
                "Enables custom combat attributes, armor calculations, and attributes GUI mechanics.",
                defaults.enableAttributes, () -> server.enableAttributes, v -> server.enableAttributes = v, editable, notice))
            .option(moduleToggle("Enable Apothic Enchanting Module",
                "Enables bookshelf table stats (Eterna, Quanta, Arcana) and high-tier level caps.",
                defaults.enableEnchanting, () -> server.enableEnchanting, v -> server.enableEnchanting = v, editable, notice))
            .option(moduleToggle("Enable Apothic Adventure Module",
                "Enables affix loot, sockets, gem drops, and boss combat mechanics.",
                defaults.enableAdventure, () -> server.enableAdventure, v -> server.enableAdventure = v, editable, notice))
            .build();
    }

    private static Option<Boolean> clientBool(String name, String desc, boolean def, Supplier<Boolean> get, Consumer<Boolean> set) {
        return Option.<Boolean>createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc), LIVE_IMPACT))
            .binding(def, get, set)
            .controller(BooleanControllerBuilder::create)
            .build();
    }

    private static Option<Integer> clientInt(String name, String desc, int def, Supplier<Integer> get, Consumer<Integer> set, int min, int max, int step) {
        return Option.<Integer>createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc), LIVE_IMPACT))
            .binding(def, get, set)
            .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step))
            .build();
    }

    private static Option<Boolean> serverBool(String name, String desc, boolean def, Supplier<Boolean> get, Consumer<Boolean> set, boolean editable, Component notice) {
        return Option.<Boolean>createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc), notice, LIVE_IMPACT))
            .binding(def, get, set)
            .controller(BooleanControllerBuilder::create)
            .available(editable)
            .build();
    }

    private static Option<Integer> serverInt(String name, String desc, int def, Supplier<Integer> get, Consumer<Integer> set, int min, int max, int step, boolean editable, Component notice) {
        return Option.<Integer>createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc), notice, LIVE_IMPACT))
            .binding(def, get, set)
            .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step))
            .available(editable)
            .build();
    }

    private static Option<Float> serverFloat(String name, String desc, float def, Supplier<Float> get, Consumer<Float> set, float min, float max, float step, boolean editable, Component notice) {
        return Option.<Float>createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc), notice, LIVE_IMPACT))
            .binding(def, get, set)
            .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step))
            .available(editable)
            .build();
    }

    private static Option<String> serverString(String name, String desc, String def, Supplier<String> get, Consumer<String> set, boolean editable, Component notice) {
        return Option.<String>createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc), notice, LIVE_IMPACT))
            .binding(def, get, set)
            .controller(StringControllerBuilder::create)
            .available(editable)
            .build();
    }

    private static Option<Boolean> moduleToggle(String name, String desc, boolean def, Supplier<Boolean> get, Consumer<Boolean> set, boolean editable, Component notice) {
        return Option.<Boolean>createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc), notice, RESTART_IMPACT))
            .binding(def, get, set)
            .controller(BooleanControllerBuilder::create)
            .flag(OptionFlag.GAME_RESTART)
            .available(editable)
            .build();
    }
}
