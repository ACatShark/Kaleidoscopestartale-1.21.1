package com.tequilacat.kaleidoscope_startale.crafting.serializer;

import com.tequilacat.kaleidoscope_startale.crafting.recipe.FermentingRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 发酵配方序列化器。
 * <p>
 * 定义发酵配方的数据编解码器与网络流编解码器，
 */
public class FermentingRecipeSerializer implements RecipeSerializer<FermentingRecipe> {

    /** 发酵时长默认值（刻），即 60 秒 */
    public static final int DEFAULT_DURATION = 60 * 20;
    /** 默认最小光照等级 */
    public static final int DEFAULT_MIN_LIGHT = 0;
    /** 默认最大光照等级 */
    public static final int DEFAULT_MAX_LIGHT = 3;

    /** 配方数据编解码器，用于从配方 JSON 读取与写出 */
    private static final MapCodec<FermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(FermentingRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(FermentingRecipe::result),
            Codec.INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(FermentingRecipe::duration),
            Codec.INT.optionalFieldOf("min_light", DEFAULT_MIN_LIGHT).forGetter(FermentingRecipe::minLight),
            Codec.INT.optionalFieldOf("max_light", DEFAULT_MAX_LIGHT).forGetter(FermentingRecipe::maxLight)
    ).apply(instance, FermentingRecipe::new));

    /** 配方网络流编解码器，用于在客户端与服务端之间同步配方 */
    private static final StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, FermentingRecipe::ingredient,
            ItemStack.STREAM_CODEC, FermentingRecipe::result,
            ByteBufCodecs.VAR_INT, FermentingRecipe::duration,
            ByteBufCodecs.VAR_INT, FermentingRecipe::minLight,
            ByteBufCodecs.VAR_INT, FermentingRecipe::maxLight,
            FermentingRecipe::new);

    /**
     * 获取配方数据编解码器。
     *
     * @return 配方数据编解码器
     */
    @Override
    public MapCodec<FermentingRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方网络流编解码器。
     *
     * @return 配方网络流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}