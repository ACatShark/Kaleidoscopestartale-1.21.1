package com.tequilacat.kaleidoscope_startale.crafting.serializer;

import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdBrewTeapotRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 冷萃茶壶配方序列化器。
 * <p>
 * 定义冷萃茶壶配方的数据编解码器与网络流编解码器。
 */
public class ColdBrewTeapotRecipeSerializer implements RecipeSerializer<ColdBrewTeapotRecipe> {

    /** 处理时长默认值（刻） */
    public static final int DEFAULT_TIME = 2400;
    /** 原料数量默认值 */
    public static final int DEFAULT_INGREDIENT_COUNT = 1;

    /** 空茶液标识符，用作茶液字段的默认值 */
    public static final ResourceLocation EMPTY_TEA_FLUID = ResourceLocation.withDefaultNamespace("empty");

    /** 配方数据编解码器，用于从配方 JSON 读取与写出 */
    private static final MapCodec<ColdBrewTeapotRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.optionalFieldOf("tea_fluid", EMPTY_TEA_FLUID).forGetter(ColdBrewTeapotRecipe::teaFluid),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ColdBrewTeapotRecipe::ingredient),
            Codec.INT.optionalFieldOf("ingredient_count", DEFAULT_INGREDIENT_COUNT).forGetter(ColdBrewTeapotRecipe::ingredientCount),
            Codec.INT.optionalFieldOf("time", DEFAULT_TIME).forGetter(ColdBrewTeapotRecipe::time),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ColdBrewTeapotRecipe::result),
            Ingredient.CODEC.optionalFieldOf("container", Ingredient.of(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.EMPTY_CUP.get())).forGetter(ColdBrewTeapotRecipe::container)
    ).apply(inst, ColdBrewTeapotRecipe::new));

    /** 配方网络流编解码器，用于在客户端与服务端之间同步配方 */
    private static final StreamCodec<RegistryFriendlyByteBuf, ColdBrewTeapotRecipe> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ColdBrewTeapotRecipe::teaFluid,
            Ingredient.CONTENTS_STREAM_CODEC, ColdBrewTeapotRecipe::ingredient,
            ByteBufCodecs.VAR_INT, ColdBrewTeapotRecipe::ingredientCount,
            ByteBufCodecs.VAR_INT, ColdBrewTeapotRecipe::time,
            ItemStack.STREAM_CODEC, ColdBrewTeapotRecipe::result,
            Ingredient.CONTENTS_STREAM_CODEC, ColdBrewTeapotRecipe::container,
            ColdBrewTeapotRecipe::new
    );

    /**
     * 获取配方数据编解码器。
     *
     * @return 配方数据编解码器
     */
    @Override
    public MapCodec<ColdBrewTeapotRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方网络流编解码器。
     *
     * @return 配方网络流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ColdBrewTeapotRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}