package dev.shadowsoffire.apotheosis.attributes.format;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;

/**
 * Handles formatting for attributes and attribute modifiers, replacing NeoForge's
 * PercentageAttribute and BooleanAttribute classes with pure Fabric formatting utilities.
 */
public class AttributeFormatting {

    public enum FormatType {
        FLAT,
        PERCENTAGE,
        BOOLEAN
    }

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.##");
    public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#0.##%");
    private static final Map<Identifier, FormatType> FORMAT_MAP = new HashMap<>();

    static {
        // Percentage Attributes
        register(ALObjects.Attributes.ARMOR_SHRED, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.ARROW_DAMAGE, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.ARROW_VELOCITY, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.CRIT_CHANCE, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.CRIT_DAMAGE, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.CURRENT_HP_DAMAGE, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.DODGE_CHANCE, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.DRAW_SPEED, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.EXPERIENCE_GAINED, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.HEALING_RECEIVED, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.LIFE_STEAL, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.OVERHEAL, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.PROJECTILE_DAMAGE, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.PROT_SHRED, FormatType.PERCENTAGE);
        register(ALObjects.Attributes.COOLDOWN_REDUCTION, FormatType.PERCENTAGE);

        // Boolean Attributes
        register(ALObjects.Attributes.ELYTRA_FLIGHT, FormatType.BOOLEAN);
    }

    private static void register(Holder<Attribute> attr, FormatType type) {
        FORMAT_MAP.put(attr.unwrapKey().get().identifier(), type);
    }

    public static FormatType getFormatType(Holder<Attribute> attr) {
        return attr.unwrapKey()
            .map(key -> FORMAT_MAP.getOrDefault(key.identifier(), FormatType.FLAT))
            .orElse(FormatType.FLAT);
    }

    public static FormatType getFormatType(Attribute attr) {
        // Look up by attribute description ID or scan registered attributes
        for (var entry : ALObjects.Attributes.ALL) {
            if (entry.value() == attr) {
                return getFormatType(entry);
            }
        }
        return FormatType.FLAT;
    }

    /**
     * Formats an attribute's effective final value as a plain string.
     */
    public static String formatValue(Holder<Attribute> attr, double value) {
        FormatType type = getFormatType(attr);
        return switch (type) {
            case PERCENTAGE -> PERCENT_FORMAT.format(value);
            case BOOLEAN -> value >= 1.0 ? "Enabled" : "Disabled";
            case FLAT -> DECIMAL_FORMAT.format(value);
        };
    }

    public static MutableComponent toValueComponent(Attribute attr, AttributeModifier.Operation op, double value, TooltipFlag flag) {
        FormatType type = getFormatType(attr);
        if (op == null) {
            return switch (type) {
                case PERCENTAGE -> Component.literal(PERCENT_FORMAT.format(value));
                case BOOLEAN -> Component.literal(value >= 1.0 ? "Enabled" : "Disabled");
                case FLAT -> Component.literal(DECIMAL_FORMAT.format(value));
            };
        }
        if (op == AttributeModifier.Operation.ADD_VALUE) {
            return switch (type) {
                case PERCENTAGE -> Component.literal((value >= 0 ? "+" : "") + PERCENT_FORMAT.format(value));
                case BOOLEAN -> Component.literal(value >= 1.0 ? "+Enabled" : "-Disabled");
                case FLAT -> Component.literal((value >= 0 ? "+" : "") + DECIMAL_FORMAT.format(value));
            };
        } else {
            return Component.literal((value >= 0 ? "+" : "") + PERCENT_FORMAT.format(value));
        }
    }

    public static MutableComponent toComponent(Attribute attr, AttributeModifier modifier, TooltipFlag flag) {
        String formatted = formatBonus(attr, modifier);
        double amount = modifier.amount();
        ChatFormatting color = attr.getStyle(amount > 0);
        String key = amount > 0 ? "neoforge.modifier.plus" : "neoforge.modifier.take";
        return Component.translatable(key, Component.literal(formatted), Component.translatable(attr.getDescriptionId())).withStyle(color);
    }

    public static String formatBonus(Attribute attr, AttributeModifier modifier) {
        double amount = modifier.amount();
        FormatType type = getFormatType(attr);

        if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
            if (type == FormatType.PERCENTAGE) {
                String prefix = amount >= 0 ? "+" : "";
                return prefix + PERCENT_FORMAT.format(amount);
            } else if (type == FormatType.BOOLEAN) {
                return amount >= 1.0 ? "+Enabled" : "-Disabled";
            } else {
                String prefix = amount >= 0 ? "+" : "";
                return prefix + DECIMAL_FORMAT.format(amount);
            }
        } else {
            String prefix = amount >= 0 ? "+" : "";
            return prefix + PERCENT_FORMAT.format(amount);
        }
    }

    /**
     * Formats an attribute value or modifier total into a styled Component.
     */
    public static MutableComponent toValueComponent(Holder<Attribute> attr, AttributeModifier.Operation op, double value, TooltipFlag flag) {
        return toValueComponent(attr.value(), op, value, flag);
    }

    /**
     * Formats an attribute modifier's bonus value (e.g. "+15%", "+2.5").
     */
    public static String formatBonus(Holder<Attribute> attr, AttributeModifier modifier) {
        double amount = modifier.amount();
        FormatType type = getFormatType(attr);

        if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
            if (type == FormatType.PERCENTAGE) {
                String prefix = amount >= 0 ? "+" : "";
                return prefix + PERCENT_FORMAT.format(amount);
            } else if (type == FormatType.BOOLEAN) {
                return amount >= 1.0 ? "+Enabled" : "-Disabled";
            } else {
                String prefix = amount >= 0 ? "+" : "";
                return prefix + DECIMAL_FORMAT.format(amount);
            }
        } else {
            String prefix = amount >= 0 ? "+" : "";
            return prefix + PERCENT_FORMAT.format(amount);
        }
    }

    /**
     * Creates a formatted text component describing the modifier for tooltips.
     */
    public static MutableComponent toComponent(Holder<Attribute> attr, AttributeModifier modifier, TooltipFlag flag) {
        return toComponent(attr.value(), modifier, flag);
    }

    public static int getValueColor(AttributeInstance inst, int fallbackColor) {
        return getValueColor(inst.getAttribute().value(), inst.getValue(), inst.getBaseValue(), fallbackColor);
    }

    public static int getValueColor(Attribute attr, double value, double base, int fallbackColor) {
        if (value == base) {
            return fallbackColor;
        }
        boolean isPositive = value > base;
        return translateColor(attr.getStyle(isPositive));
    }

    public static int translateColor(ChatFormatting color) {
        return switch (color) {
            case BLUE -> 0x55DD55;
            case RED -> 0xFF6060;
            case GRAY -> 0xFFFFFF;
            default -> {
                TextColor tc = TextColor.fromLegacyFormat(color);
                yield tc != null ? tc.getValue() : 0xFFFFFF;
            }
        };
    }

    public static ChatFormatting getColor(Attribute attr, boolean isNegative) {
        ChatFormatting color = attr.getStyle(!isNegative);
        return color == ChatFormatting.BLUE ? ChatFormatting.YELLOW : color;
    }

    public static String colored(String str, ChatFormatting color) {
        return color.toString() + str + ChatFormatting.RESET.toString();
    }

    public static Component buildFormula(Component base, Component value, double[] numericValues, Attribute attr) {
        double add = numericValues[0];
        double mulBase = numericValues[1];
        double mulTotal = numericValues[2];

        boolean isAddNeg = add < 0;
        boolean isMulNeg = mulBase < 0;

        String addSym = isAddNeg ? "-" : "+";
        add = Math.abs(add);

        String mulBaseSym = isMulNeg ? "-" : "+";
        mulBase = Math.abs(mulBase);

        String addStr = DECIMAL_FORMAT.format(add);
        String mulBaseStr = DECIMAL_FORMAT.format(mulBase);
        String mulTotalStr = DECIMAL_FORMAT.format(mulTotal);

        String formula = "%2$s";

        if (add != 0) {
            ChatFormatting color = getColor(attr, isAddNeg);
            formula = formula + " " + colored(addSym + " " + addStr, color);
        }

        if (mulBase != 0) {
            String withParens = add == 0 ? formula : "(%s)".formatted(formula);
            ChatFormatting color = getColor(attr, isMulNeg);
            formula = withParens + " " + colored(mulBaseSym + " " + mulBaseStr + " * ", color) + withParens;
        }

        if (mulTotal != 1) {
            String withParens = add == 0 && mulBase == 0 ? formula : "(%s)".formatted(formula);
            ChatFormatting color = getColor(attr, mulTotal < 1);
            formula = colored(mulTotalStr + " * ", color) + withParens;
        }

        return Component.translatable("%1$s = " + formula, value, base).withStyle(ChatFormatting.GRAY);
    }
}
