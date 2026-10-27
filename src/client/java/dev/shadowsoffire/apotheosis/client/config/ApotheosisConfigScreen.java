package dev.shadowsoffire.apotheosis.client.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Client-only configuration screen built with YetAnotherConfigLib (YACL).
 * <p>
 * This class is strictly segregated in the client source set and will never
 * be loaded by dedicated server classloaders.
 */
public class ApotheosisConfigScreen {

    public static Screen create(Screen parent) {
        ApotheosisConfig config = ApotheosisConfig.get();

        return YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Fabricated Apotheosis Configuration"))
            .save(ApotheosisConfig::save)
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Modules"))
                .tooltip(Component.literal("Enable or disable individual Apotheosis ecosystem modules."))
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Spawners Module"))
                    .description(OptionDescription.of(Component.literal("Enables Apothic Spawners modifiers, silk touch capture, and mechanics.")))
                    .binding(true, () -> config.enableSpawners, val -> config.enableSpawners = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Attributes Module"))
                    .description(OptionDescription.of(Component.literal("Enables Apothic Attributes modifiers, custom attributes, and GUI formatting.")))
                    .binding(true, () -> config.enableAttributes, val -> config.enableAttributes = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Enchanting Module"))
                    .description(OptionDescription.of(Component.literal("Enables Apothic Enchanting stats (Eterna, Quanta, Arcana) and high-tier capping.")))
                    .binding(true, () -> config.enableEnchanting, val -> config.enableEnchanting = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Enable Adventure Module"))
                    .description(OptionDescription.of(Component.literal("Enables affix loot, socketing, gems, and boss mechanics.")))
                    .binding(true, () -> config.enableAdventure, val -> config.enableAdventure = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Spawners"))
                .tooltip(Component.literal("Apothic Spawners module configuration options."))
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Silk Touch Spawner Harvest"))
                    .description(OptionDescription.of(Component.literal("Allows players to harvest monster spawners with a Silk Touch pickaxe.")))
                    .binding(true, () -> config.spawnerSilkTouchHarvest, val -> config.spawnerSilkTouchHarvest = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Redstone Control"))
                    .description(OptionDescription.of(Component.literal("Enables toggling spawner activation with redstone signals.")))
                    .binding(true, () -> config.spawnerRedstoneControl, val -> config.spawnerRedstoneControl = val)
                    .controller(BooleanControllerBuilder::create)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Minimum Spawn Delay Limit"))
                    .description(OptionDescription.of(Component.literal("The absolute lowest minimum spawn delay (in ticks) a spawner can be modified to.")))
                    .binding(20, () -> config.minSpawnDelayLimit, val -> config.minSpawnDelayLimit = val)
                    .controller(IntegerFieldControllerBuilder::create)
                    .build())
                .build())
            .build()
            .generateScreen(parent);
    }
}
