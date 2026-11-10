package com.tequilacat.kaleidoscope_startale.data.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.FermentingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 发酵配方序列化器。
 * <p>
 * 负责发酵配方的编解码：原料、产物、时长与光照范围的数据编解码，
 * 以及面向网络的流编解码。时长与光照范围均有默认值。
 */
public class FermentingRecipeBuilder implements RecipeSerializer<FermentingRecipe> {

    /** 发酵时长默认值（刻），即 60 秒。 */
    public static final int DEFAULT_DURATION = 60 * 20;

    /** 默认最小光照等级。 */
    public static final int DEFAULT_MIN_LIGHT = 0;

    /** 默认最大光照等级。 */
    public static final int DEFAULT_MAX_LIGHT = 3;

    /**
     * 发酵配方的数据编解码器。
     * <p>
     * 时长与光照范围均为可选，缺省时使用默认值。
     */
    private static final MapCodec<FermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(FermentingRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(FermentingRecipe::result),
            Codec.INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(FermentingRecipe::duration),
            Codec.INT.optionalFieldOf("min_light", DEFAULT_MIN_LIGHT).forGetter(FermentingRecipe::minLight),
            Codec.INT.optionalFieldOf("max_light", DEFAULT_MAX_LIGHT).forGetter(FermentingRecipe::maxLight)
    ).apply(instance, FermentingRecipe::new));

    /**
     * 发酵配方的流编解码器，用于网络传输。
     * <p>
     * 依次写入原料、产物、时长、最小与最大光照等级。
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public FermentingRecipe decode(RegistryFriendlyByteBuf buf) {
                    Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    int duration = buf.readVarInt();
                    int minLight = buf.readVarInt();
                    int maxLight = buf.readVarInt();
                    return new FermentingRecipe(ingredient, result, duration, minLight, maxLight);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, FermentingRecipe recipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                    buf.writeVarInt(recipe.duration());
                    buf.writeVarInt(recipe.minLight());
                    buf.writeVarInt(recipe.maxLight());
                }
            };

    /**
     * 获取配方的数据编解码器。
     *
     * @return 数据编解码器
     */
    @Override
    public MapCodec<FermentingRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方的流编解码器。
     *
     * @return 流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
