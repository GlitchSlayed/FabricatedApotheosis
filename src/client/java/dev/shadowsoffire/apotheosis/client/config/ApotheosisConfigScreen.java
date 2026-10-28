package dev.shadowsoffire.apotheosis.client.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.apotheosis.config.ClientConfig;
import dev.shadowsoffire.apotheosis.config.ServerConfig;
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

    public static Screen create(Screen parent) {
        boolean isRemote = ApotheosisConfig.isRemoteServer();
        ClientConfig client = ApotheosisConfig.getClient();
        ServerConfig server = ApotheosisConfig.getServer();

        Component serverStatusNotice = isRemote
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
            // ==================== 1. CLIENT PREFERENCES ====================
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Client Preferences"))
                .tooltip(Component.literal("Personal visual preferences, tooltip detail levels, and screen positioning."))
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Boss Health Bar Glow"))
                    .description(OptionDescription.of(
                        Component.literal("Renders a visual glow effect behind Apotheosis boss health bars."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> client.bossHealthBarGlow, val -> client.bossHealthBarGlow = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Sculk Shelf Particles"))
                    .description(OptionDescription.of(
                        Component.literal("Enables ambient soul particles emanating from active sculkshelves."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> client.sculkShelfParticles, val -> client.sculkShelfParticles = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Sculk Shelf Noise Chance"))
                    .description(OptionDescription.of(
                        Component.literal("The 1/n chance per tick that a sculkshelf plays an ambient sound (0 to disable)."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(200, () -> client.sculkShelfNoiseChance, val -> client.sculkShelfNoiseChance = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 2000).step(20))
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Display Attribute Deltas"))
                    .description(OptionDescription.of(
                        Component.literal("Displays comparative stat differences (e.g. +2.5 Armor) in item tooltips."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> client.displayAttributeDeltas, val -> client.displayAttributeDeltas = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Potion Tooltips"))
                    .description(OptionDescription.of(
                        Component.literal("Displays detailed effect descriptions in potion item tooltips."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> client.enablePotionTooltips, val -> client.enablePotionTooltips = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Show Enchanted Book Metadata"))
                    .description(OptionDescription.of(
                        Component.literal("Shows treasure, tradeable, and discoverable metadata on enchanted books."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> client.showEnchantedBookMetadata, val -> client.showEnchantedBookMetadata = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Detailed Spawner Tooltips"))
                    .description(OptionDescription.of(
                        Component.literal("Shows modified spawner stat values and limits when hovering spawner items."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> client.detailedSpawnerTooltips, val -> client.detailedSpawnerTooltips = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Attributes GUI"))
                    .description(OptionDescription.of(
                        Component.literal("Adds the Attributes GUI toggle button to the player inventory screen."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> client.enableAttributesGui, val -> client.enableAttributesGui = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Attributes GUI Button Offset X"))
                    .description(OptionDescription.of(
                        Component.literal("Horizontal pixel offset of the Attributes button on the inventory screen."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(-3, () -> client.attributesGuiOffsetX, val -> client.attributesGuiOffsetX = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(-100, 100).step(1))
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Attributes GUI Button Offset Y"))
                    .description(OptionDescription.of(
                        Component.literal("Vertical pixel offset of the Attributes button on the inventory screen."),
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(3, () -> client.attributesGuiOffsetY, val -> client.attributesGuiOffsetY = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(-100, 100).step(1))
                    .build())
                .build())

            // ==================== 2. GAMEPLAY: SPAWNERS ====================
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Gameplay: Spawners"))
                .tooltip(Component.literal("Server-authoritative spawner mechanics and modification limits."))
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Silk Touch Harvest Level"))
                    .description(OptionDescription.of(
                        Component.literal("Level of Silk Touch needed to harvest a spawner (-1 to disable, 0 for always)."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(1, () -> server.spawnerSilkLevel, val -> server.spawnerSilkLevel = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(-1, 10).step(1))
                    .available(!isRemote)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Silk Touch Tool Damage"))
                    .description(OptionDescription.of(
                        Component.literal("Durability damage dealt to a tool when harvesting a monster spawner."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(100, () -> server.spawnerSilkDamage, val -> server.spawnerSilkDamage = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 2000).step(25))
                    .available(!isRemote)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Spawners Drop Empty"))
                    .description(OptionDescription.of(
                        Component.literal("If true, harvested spawners clear their stored entity type upon drop."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(false, () -> server.spawnersDropEmpty, val -> server.spawnersDropEmpty = val)
                    .controller(BooleanControllerBuilder::create)
                    .available(!isRemote)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Entity Despawn Delay (Ticks)"))
                    .description(OptionDescription.of(
                        Component.literal("Time in ticks spawner mobs are immune to despawning after spawning."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(600, () -> server.entityDespawnDelay, val -> server.entityDespawnDelay = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 6000).step(100))
                    .available(!isRemote)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Redstone Activation Control"))
                    .description(OptionDescription.of(
                        Component.literal("Allows players to invert or toggle spawner activation with redstone signals."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(true, () -> server.spawnerRedstoneControl, val -> server.spawnerRedstoneControl = val)
                    .controller(BooleanControllerBuilder::create)
                    .available(!isRemote)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Minimum Spawn Delay Floor"))
                    .description(OptionDescription.of(
                        Component.literal("The absolute lowest spawn delay (in ticks) a spawner can be modified to reach."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(20, () -> server.minSpawnDelayLimit, val -> server.minSpawnDelayLimit = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 200).step(5))
                    .available(!isRemote)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Maximum Spawn Delay Ceiling"))
                    .description(OptionDescription.of(
                        Component.literal("The highest max spawn delay (in ticks) permitted after modifier applications."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(800, () -> server.maxSpawnDelayLimit, val -> server.maxSpawnDelayLimit = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(200, 5000).step(50))
                    .available(!isRemote)
                    .build())
                .build())

            // ==================== 3. GAMEPLAY: ENCHANTING ====================
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Gameplay: Enchanting"))
                .tooltip(Component.literal("Server-authoritative enchanting rules and bookshelf mechanics."))
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Maximum Enchantment Level Cap"))
                    .description(OptionDescription.of(
                        Component.literal("The highest allowed level cap for any enchantment in the ecosystem."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(100, () -> server.maxEnchantmentLevelLimit, val -> server.maxEnchantmentLevelLimit = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(10, 255).step(5))
                    .available(!isRemote)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Bookshelf Detection Range"))
                    .description(OptionDescription.of(
                        Component.literal("Horizontal distance around the enchanting table searched for valid bookshelves."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(2, () -> server.maxBookshelfRange, val -> server.maxBookshelfRange = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 5).step(1))
                    .available(!isRemote)
                    .build())
                .build())

            // ==================== 4. GAMEPLAY: ATTRIBUTES ====================
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Gameplay: Attributes"))
                .tooltip(Component.literal("Server-authoritative combat math formulas and multipliers."))
                .option(Option.<Float>createBuilder()
                    .name(Component.literal("Ancient Knowledge XP Multiplier"))
                    .description(OptionDescription.of(
                        Component.literal("Experience Gained modifier per level of the Ancient Knowledge effect."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(4.0F, () -> server.knowledgeMultiplier, val -> server.knowledgeMultiplier = val)
                    .controller(opt -> FloatSliderControllerBuilder.create(opt).range(1.0F, 50.0F).step(0.5F))
                    .available(!isRemote)
                    .build())
                .option(Option.<Float>createBuilder()
                    .name(Component.literal("Negative Armor Factor"))
                    .description(OptionDescription.of(
                        Component.literal("Damage amplification fraction per negative armor point."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(0.015F, () -> server.negativeArmorFactor, val -> server.negativeArmorFactor = val)
                    .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0F, 0.1F).step(0.005F))
                    .available(!isRemote)
                    .build())
                .option(Option.<Float>createBuilder()
                    .name(Component.literal("Critical Damage Modifier Cap"))
                    .description(OptionDescription.of(
                        Component.literal("Upper threshold multiplier cap on critical strike bonus damage."),
                        serverStatusNotice,
                        Component.literal("[Impact: Live]").withStyle(ChatFormatting.DARK_AQUA)
                    ))
                    .binding(10.0F, () -> server.critDamageModifierCap, val -> server.critDamageModifierCap = val)
                    .controller(opt -> FloatSliderControllerBuilder.create(opt).range(1.0F, 50.0F).step(1.0F))
                    .available(!isRemote)
                    .build())
                .build())

            // ==================== 5. MODULE TOGGLES ====================
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Module Toggles"))
                .tooltip(Component.literal("Enable or disable major ecosystem modules. Requires restart."))
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Apothic Spawners Module"))
                    .description(OptionDescription.of(
                        Component.literal("Enables spawner stat modification, silk touch capture, and modifier items."),
                        serverStatusNotice,
                        Component.literal("[Impact: Requires Restart]").withStyle(ChatFormatting.RED)
                    ))
                    .binding(true, () -> server.enableSpawners, val -> server.enableSpawners = val)
                    .controller(BooleanControllerBuilder::create)
                    .flag(OptionFlag.GAME_RESTART)
                    .available(!isRemote)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Apothic Attributes Module"))
                    .description(OptionDescription.of(
                        Component.literal("Enables 21 custom combat attributes, armor calculations, and attributes GUI."),
                        serverStatusNotice,
                        Component.literal("[Impact: Requires Restart]").withStyle(ChatFormatting.RED)
                    ))
                    .binding(true, () -> server.enableAttributes, val -> server.enableAttributes = val)
                    .controller(BooleanControllerBuilder::create)
                    .flag(OptionFlag.GAME_RESTART)
                    .available(!isRemote)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Apothic Enchanting Module"))
                    .description(OptionDescription.of(
                        Component.literal("Enables bookshelf table stats (Eterna, Quanta, Arcana) and high-tier level caps."),
                        serverStatusNotice,
                        Component.literal("[Impact: Requires Restart]").withStyle(ChatFormatting.RED)
                    ))
                    .binding(true, () -> server.enableEnchanting, val -> server.enableEnchanting = val)
                    .controller(BooleanControllerBuilder::create)
                    .flag(OptionFlag.GAME_RESTART)
                    .available(!isRemote)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Apothic Adventure Module"))
                    .description(OptionDescription.of(
                        Component.literal("Enables affix loot, sockets, gem drops, and boss combat mechanics."),
                        serverStatusNotice,
                        Component.literal("[Impact: Requires Restart]").withStyle(ChatFormatting.RED)
                    ))
                    .binding(true, () -> server.enableAdventure, val -> server.enableAdventure = val)
                    .controller(BooleanControllerBuilder::create)
                    .flag(OptionFlag.GAME_RESTART)
                    .available(!isRemote)
                    .build())
                .build())
            .build()
            .generateScreen(parent);
    }
}
