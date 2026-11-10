package com.tequilacat.kaleidoscope_startale.data.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdStewRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 冷炖配方序列化器。
 * <p>
 * 负责冷炖配方的编解码：汤底、原料列表、产物与时长的数据编解码，
 * 以及面向网络的流编解码。汤底与时长均有默认值。
 */
public class ColdStewRecipeBuilder implements RecipeSerializer<ColdStewRecipe> {

    /** 处理时长默认值（刻）。 */
    public static final int DEFAULT_DURATION = 1200;

    /** 默认汤底。 */
    public static final Ingredient DEFAULT_SOUP_BASE = Ingredient.of(Items.POWDER_SNOW_BUCKET, Items.BUCKET);

    /**
     * 冷炖配方的数据编解码器。
     * <p>
     * 汤底与时长为可选，缺省时使用默认值。
     */
    private static final MapCodec<ColdStewRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.optionalFieldOf("soup_base", DEFAULT_SOUP_BASE).forGetter(ColdStewRecipe::soupBase),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(ColdStewRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(ColdStewRecipe::result),
            Codec.INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(ColdStewRecipe::duration)
    ).apply(instance, ColdStewRecipe::new));

    /**
     * 冷炖配方的流编解码器，用于网络传输。
     * <p>
     * 依次写入汤底、原料数量与各原料、产物与时长。
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, ColdStewRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ColdStewRecipe decode(RegistryFriendlyByteBuf buf) {
                    Ingredient soupBase = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    int size = buf.readVarInt();
                    List<Ingredient> ingredients = new ArrayList<>(size);
                    for (int i = 0; i < size; ++i) {
                        ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }

                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    int duration = buf.readVarInt();
                    return new ColdStewRecipe(soupBase, ingredients, result, duration);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ColdStewRecipe recipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.soupBase());
                    buf.writeVarInt(recipe.ingredients().size());
                    for (Ingredient ingredient : recipe.ingredients()) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }

                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                    buf.writeVarInt(recipe.duration());
                }
            };

    /**
     * 获取配方的数据编解码器。
     *
     * @return 数据编解码器
     */
    @Override
    public MapCodec<ColdStewRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方的流编解码器。
     *
     * @return 流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ColdStewRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
