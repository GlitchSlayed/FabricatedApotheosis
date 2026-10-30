package dev.shadowsoffire.apotheosis.adventure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.commands.AffixCommand;
import dev.shadowsoffire.apotheosis.commands.CategoryCheckCommand;
import dev.shadowsoffire.apotheosis.commands.GemCommand;
import dev.shadowsoffire.apotheosis.commands.RarityCommand;
import dev.shadowsoffire.apotheosis.commands.ReforgeCommand;
import dev.shadowsoffire.apotheosis.commands.SocketCommand;
import dev.shadowsoffire.apotheosis.commands.WorldTierCommand;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.RarityOverrideRegistry;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
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
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.commands.Commands;

/**
 * Apothic Adventure module initialization.
 */
public class AdventureModule {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Adventure");

    public static void init() {
        LOGGER.info("Adventure module initialized");

        Apoth.bootstrap();

        // Dynamic registries
        RarityRegistry.INSTANCE.registerToBus();
        RarityOverrideRegistry.INSTANCE.registerToBus();
        AffixRegistry.INSTANCE.registerToBus();
        ExtraGemBonusRegistry.INSTANCE.registerToBus();
        GemRegistry.INSTANCE.registerToBus();
        AffixLootRegistry.INSTANCE.registerToBus();
        PurityWeightsRegistry.INSTANCE.registerToBus();
        TierAugmentRegistry.INSTANCE.registerToBus();

        // Network payloads
        PayloadHelper.registerPayload(new BossSpawnPayload.Provider());
        PayloadHelper.registerPayload(new RerollResultPayload.Provider());
        PayloadHelper.registerPayload(new RadialStatePayload.Provider());
        PayloadHelper.registerPayload(new AttributeTogglesPayload.Provider());
        PayloadHelper.registerPayload(new WorldTierPayload.Provider());
        PayloadHelper.registerPayload(new AdventureConfig.ConfigPayload.Provider());
        PayloadHelper.registerPayload(new LinkItemToChatPayload.Provider());
        PayloadHelper.registerPayload(new GemCaseSelectPayload.Provider());

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
            dispatcher.register(root);
        });
    }
}
