package dev.shadowsoffire.placebo.util;

import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * A utility class for handling offsets and anchor points for GUI elements.
 * Calculates positions based on an anchor point and an (x, y) offset.
 *
 * @param anchor The anchor point of the element.
 * @param x      The X offset of the element.
 * @param y      The Y offset of the element.
 */
public record Offset(AnchorPoint anchor, int x, int y) {

    public static final Codec<Offset> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            AnchorPoint.CODEC.fieldOf("anchor").forGetter(Offset::anchor),
            Codec.INT.fieldOf("x").forGetter(Offset::x),
            Codec.INT.fieldOf("y").forGetter(Offset::y))
        .apply(inst, Offset::new));

    public int getX(Box window, Box element) {
        return this.anchor.getX(window.width) - this.anchor.getX(element.width) + this.x;
    }

    public int getY(Box window, Box element) {
        return this.anchor.getY(window.height) - this.anchor.getY(element.height) + this.y;
    }

    public static record Box(int width, int height) {}

    public enum AnchorPoint implements StringRepresentable {
        TOP_LEFT("top_left", width -> 0, height -> 0),
        TOP_CENTER("top_center", width -> width / 2, height -> 0),
        TOP_RIGHT("top_right", width -> width, height -> 0),
        MIDDLE_LEFT("middle_left", width -> 0, height -> height / 2),
        MIDDLE_CENTER("middle_center", width -> width / 2, height -> height / 2),
        MIDDLE_RIGHT("middle_right", width -> width, height -> height / 2),
        BOTTOM_LEFT("bottom_left", width -> 0, height -> height),
        BOTTOM_CENTER("bottom_center", width -> width / 2, height -> height),
        BOTTOM_RIGHT("bottom_right", width -> width, height -> height);

        public static final IntFunction<AnchorPoint> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<AnchorPoint> CODEC = StringRepresentable.fromValues(AnchorPoint::values);

        private final String name;
        private final Int2IntFunction xPos;
        private final Int2IntFunction yPos;

        AnchorPoint(String name, Int2IntFunction xPos, Int2IntFunction yPos) {
            this.name = name;
            this.xPos = xPos;
            this.yPos = yPos;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getX(int width) {
            return this.xPos.apply(width);
        }

        public int getY(int height) {
            return this.yPos.apply(height);
        }
    }
}
