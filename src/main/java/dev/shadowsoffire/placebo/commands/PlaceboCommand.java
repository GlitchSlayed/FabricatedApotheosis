package dev.shadowsoffire.placebo.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.Placebo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Registers utility diagnostic commands under {@code /placebo}:
 * {@code /placebo hand}, {@code /placebo serialize_loot_table}, and {@code /placebo get_dimension_type}.
 */
public class PlaceboCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(arg -> Component.translatable("placebo.cmd.not_found", arg));
    private static final SimpleCommandExceptionType REQUIRES_LIVING = new SimpleCommandExceptionType(Component.literal("Command must be executed by a living entity holding an item."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("placebo");
        registerHand(builder);
        registerSerializeLootTable(builder, ctx);
        registerGetDimensionType(builder);
        dispatcher.register(builder);
    }

    private static void registerHand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("hand").executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof LivingEntity living)) {
                throw REQUIRES_LIVING.create();
            }
            ItemStack stack = living.getWeaponItem();
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, ctx.getSource().getServer().registryAccess());
            try {
                String jsonStr = itemToJsonString(stack, ops);
                Placebo.LOGGER.info("Serialized held item:\n{}", jsonStr);
                ctx.getSource().sendSystemMessage(
                    Component.literal("Currently Held Item:\n" + jsonStr)
                        .withStyle(s -> s
                            .withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent.CopyToClipboard(jsonStr))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy JSON")))));
                return 1;
            }
            catch (Exception e) {
                Placebo.LOGGER.error("Failed to serialize held item", e);
                ctx.getSource().sendFailure(Component.translatable("placebo.cmd.serialize_failure"));
                return 0;
            }
        }));
    }

    public static String itemToJsonString(ItemStack stack, RegistryOps<JsonElement> ops) throws IOException {
        JsonElement json = (stack.isEmpty() ? ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack) : ItemStack.CODEC.encodeStart(ops, stack)).getOrThrow();
        StringWriter str = new StringWriter();
        try (JsonWriter writer = GSON.newJsonWriter(str)) {
            writer.setIndent("    ");
            GSON.toJson(json, writer);
        }
        return str.toString();
    }

    private static void registerSerializeLootTable(LiteralArgumentBuilder<CommandSourceStack> builder, CommandBuildContext context) {
        builder.then(Commands.literal("serialize_loot_table")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.argument("loot_table", ResourceOrIdArgument.lootTable(context)).executes(ctx -> {
                Holder<LootTable> table = ResourceOrIdArgument.getLootTable(ctx, "loot_table");
                Identifier id = table.unwrapKey().map(key -> key.identifier()).orElse(null);
                if (id == null || table.value() == LootTable.EMPTY) {
                    throw NOT_FOUND.create(id == null ? "<unknown>" : id.toString());
                }

                var access = ctx.getSource().registryAccess();
                JsonElement json = LootTable.DIRECT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, access), table.value()).getOrThrow();
                String relativePath = writeDatagenJson(json, "loot_table", id);
                Placebo.LOGGER.info("Serialized loot table {} to {}", id, relativePath);
                ctx.getSource().sendSuccess(() -> Component.translatable("placebo.cmd.serialize_success", id.toString(), relativePath), true);
                return 1;
            })));
    }

    private static void registerGetDimensionType(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("get_dimension_type").executes(ctx -> {
            ServerLevel level = ctx.getSource().getLevel();
            Registry<DimensionType> reg = level.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE);
            Identifier dimKey = level.dimension().identifier();
            Identifier typeKey = reg.getKey(level.dimensionType());
            ctx.getSource().sendSuccess(() -> Component.literal("Dimension: " + dimKey + " | Dimension Type: " + typeKey), true);
            return 1;
        }));
    }

    private static String writeDatagenJson(JsonElement json, String type, Identifier key) {
        File gameDir = FabricLoader.getInstance().getGameDir().toFile();
        String relPath = "datagen/" + key.getNamespace() + "/" + type + "/" + key.getPath() + ".json";
        File file = new File(gameDir, relPath);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file); JsonWriter jWriter = GSON.newJsonWriter(writer)) {
            jWriter.setIndent("    ");
            GSON.toJson(json, jWriter);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to write datagen file " + file.getAbsolutePath(), e);
        }
        return relPath;
    }
}
