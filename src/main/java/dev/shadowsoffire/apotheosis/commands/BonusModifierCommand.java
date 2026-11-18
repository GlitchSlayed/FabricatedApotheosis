package dev.shadowsoffire.apotheosis.commands;

import java.util.Arrays;
import java.util.Locale;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.api.ALObjects.BuiltInRegs;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiers;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Gamemaster command for attaching {@link ALObjects.Components#BONUS_STACK_ATTRIBUTE_MODIFIERS}
 * entries to the player's currently held item.
 */
public class BonusModifierCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) ->
        SharedSuggestionProvider.suggest(Arrays.stream(Operation.values()).map(Operation::name), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_SLOT = (ctx, builder) ->
        SharedSuggestionProvider.suggest(BuiltInRegs.ENTITY_SLOT_GROUP.keySet().stream().map(Identifier::toString), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIB = (ctx, builder) ->
        SharedSuggestionProvider.suggest(BuiltInRegistries.ATTRIBUTE.keySet().stream().map(Identifier::toString), builder);

    private static final DynamicCommandExceptionType UNKNOWN_ATTRIB = new DynamicCommandExceptionType(id -> Component.literal("Unknown attribute: " + id));
    private static final DynamicCommandExceptionType UNKNOWN_OP = new DynamicCommandExceptionType(op -> Component.literal("Unknown operation: " + op));
    private static final DynamicCommandExceptionType UNKNOWN_SLOT = new DynamicCommandExceptionType(slot -> Component.literal("Unknown slot group: " + slot));
    private static final SimpleCommandExceptionType EMPTY_HAND = new SimpleCommandExceptionType(Component.literal("Must hold an item in main hand."));

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root, CommandBuildContext ctx) {
        root.then(Commands.literal("add_bonus_modifier").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.argument("attribute", IdentifierArgument.id()).suggests(SUGGEST_ATTRIB)
                .then(Commands.argument("op", StringArgumentType.word()).suggests(SUGGEST_OP)
                    .then(Commands.argument("value", FloatArgumentType.floatArg())
                        .then(Commands.argument("slot", IdentifierArgument.id()).suggests(SUGGEST_SLOT).executes(c -> {
                            Player p = c.getSource().getPlayerOrException();
                            ItemStack stack = p.getMainHandItem();
                            if (stack.isEmpty()) {
                                throw EMPTY_HAND.create();
                            }

                            Identifier attrId = c.getArgument("attribute", Identifier.class);
                            Holder<Attribute> attrib = BuiltInRegistries.ATTRIBUTE.get(attrId)
                                .map(h -> (Holder<Attribute>) h)
                                .orElseThrow(() -> UNKNOWN_ATTRIB.create(attrId));

                            String opRaw = c.getArgument("op", String.class);
                            Operation op;
                            try {
                                op = Operation.valueOf(opRaw.toUpperCase(Locale.ROOT));
                            }
                            catch (IllegalArgumentException ex) {
                                throw UNKNOWN_OP.create(opRaw);
                            }

                            Identifier slotId = c.getArgument("slot", Identifier.class);
                            EntitySlotGroup slot = BuiltInRegs.ENTITY_SLOT_GROUP.get(slotId)
                                .map(Holder.Reference::value)
                                .orElseThrow(() -> UNKNOWN_SLOT.create(slotId));

                            float value = c.getArgument("value", Float.class);
                            StackAttributeModifiers bonus = stack.getOrDefault(ALObjects.Components.BONUS_STACK_ATTRIBUTE_MODIFIERS, StackAttributeModifiers.EMPTY);
                            AttributeModifier modif = new AttributeModifier(Apotheosis.loc("command_generated_" + p.level().getRandom().nextInt()), value, op);

                            stack.set(ALObjects.Components.BONUS_STACK_ATTRIBUTE_MODIFIERS, bonus.withModifierAdded(attrib, modif, slot));
                            c.getSource().sendSuccess(() -> Component.literal("Added bonus modifier for " + attrId + " to held item."), true);
                            return 1;
                        }))))));
    }
}
