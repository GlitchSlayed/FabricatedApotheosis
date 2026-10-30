package dev.shadowsoffire.apotheosis.util;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record SizedIngredient(Ingredient ingredient, int count) implements Predicate<ItemStack> {

    public static final Codec<SizedIngredient> NESTED_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Ingredient.CODEC.fieldOf("ingredient").forGetter(SizedIngredient::ingredient),
        Codec.INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count)
    ).apply(inst, SizedIngredient::new));

    public static final Codec<SizedIngredient> CODEC = NESTED_CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, SizedIngredient::ingredient,
        ByteBufCodecs.VAR_INT, SizedIngredient::count,
        SizedIngredient::new
    );

    public static SizedIngredient of(Ingredient ingredient, int count) {
        return new SizedIngredient(ingredient, count);
    }

    public static SizedIngredient of(Ingredient ingredient) {
        return new SizedIngredient(ingredient, 1);
    }

    @Override
    public boolean test(ItemStack stack) {
        return !stack.isEmpty() && this.ingredient.test(stack) && stack.getCount() >= this.count;
    }
}
