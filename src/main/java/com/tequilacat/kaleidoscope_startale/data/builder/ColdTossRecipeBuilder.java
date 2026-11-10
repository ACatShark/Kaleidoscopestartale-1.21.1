package com.tequilacat.kaleidoscope_startale.data.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdTossRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 冷炒配方序列化器。
 * <p>
 * 负责冷炒配方的编解码：原料列表、产物、翻炒次数与载体的数据编解码，
 * 以及面向网络的流编解码。翻炒次数与载体均有默认值。
 */
public class ColdTossRecipeBuilder implements RecipeSerializer<ColdTossRecipe> {

    /** 翻炒次数默认值。 */
    public static final int DEFAULT_STIR_FRY_COUNT = 5;

    /**
     * 冷炒配方的数据编解码器。
     * <p>
     * 翻炒次数与载体均为可选，缺省时使用默认值。
     */
    private static final MapCodec<ColdTossRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(ColdTossRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(ColdTossRecipe::result),
            Codec.INT.optionalFieldOf("stir_fry_count", DEFAULT_STIR_FRY_COUNT).forGetter(ColdTossRecipe::stirFryCount),
            Ingredient.CODEC.optionalFieldOf("carrier", Ingredient.EMPTY).forGetter(ColdTossRecipe::carrier)
    ).apply(instance, ColdTossRecipe::new));

    /**
     * 冷炒配方的流编解码器，用于网络传输。
     * <p>
     * 依次写入原料数量与各原料、产物、翻炒次数与载体。
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, ColdTossRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ColdTossRecipe decode(RegistryFriendlyByteBuf buf) {
                    int size = buf.readVarInt();
                    List<Ingredient> ingredients = new ArrayList<>(size);
                    for (int i = 0; i < size; ++i) {
                        ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }

                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    int stirFryCount = buf.readVarInt();
                    Ingredient carrier = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    return new ColdTossRecipe(ingredients, result, stirFryCount, carrier);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ColdTossRecipe recipe) {
                    buf.writeVarInt(recipe.ingredients().size());
                    for (Ingredient ingredient : recipe.ingredients()) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }

                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                    buf.writeVarInt(recipe.stirFryCount());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.carrier());
                }
            };

    /**
     * 获取配方的数据编解码器。
     *
     * @return 数据编解码器
     */
    @Override
    public MapCodec<ColdTossRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方的流编解码器。
     *
     * @return 流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ColdTossRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
