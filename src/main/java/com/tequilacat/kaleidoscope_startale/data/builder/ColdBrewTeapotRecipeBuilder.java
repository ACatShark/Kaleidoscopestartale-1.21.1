package com.tequilacat.kaleidoscope_startale.data.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdBrewTeapotRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 冷萃茶壶配方序列化器。
 * <p>
 * 负责冷萃茶壶配方的编解码：茶液、原料、原料数量、处理时长、产物与容器的数据编解码，
 * 以及面向网络的流编解码。茶液、原料数量、处理时长与容器均有默认值。
 */
public class ColdBrewTeapotRecipeBuilder implements RecipeSerializer<ColdBrewTeapotRecipe> {

    /** 处理时长默认值（刻）。 */
    public static final int DEFAULT_TIME = 2400;

    /** 原料数量默认值。 */
    public static final int DEFAULT_INGREDIENT_COUNT = 1;

    /** 空茶液标识符，用作茶液字段的默认值。 */
    public static final ResourceLocation EMPTY_TEA_FLUID = ResourceLocation.withDefaultNamespace("empty");

    /**
     * 冷萃茶壶配方的数据编解码器。
     * <p>
     * 茶液、原料数量、处理时长与容器均为可选，缺省时使用默认值。
     */
    private static final MapCodec<ColdBrewTeapotRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("tea_fluid", EMPTY_TEA_FLUID).forGetter(ColdBrewTeapotRecipe::teaFluid),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ColdBrewTeapotRecipe::ingredient),
            Codec.INT.optionalFieldOf("ingredient_count", DEFAULT_INGREDIENT_COUNT).forGetter(ColdBrewTeapotRecipe::ingredientCount),
            Codec.INT.optionalFieldOf("time", DEFAULT_TIME).forGetter(ColdBrewTeapotRecipe::time),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ColdBrewTeapotRecipe::result),
            Ingredient.CODEC.optionalFieldOf("container",
                    Ingredient.of(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.EMPTY_CUP.get())).forGetter(ColdBrewTeapotRecipe::container)
    ).apply(instance, ColdBrewTeapotRecipe::new));

    /**
     * 冷萃茶壶配方的流编解码器，用于网络传输。
     * <p>
     * 依次写入茶液标识符、原料、原料数量、处理时长、产物与容器。
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, ColdBrewTeapotRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ColdBrewTeapotRecipe decode(RegistryFriendlyByteBuf buf) {
                    ResourceLocation teaFluid = buf.readResourceLocation();
                    Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    int ingredientCount = buf.readVarInt();
                    int time = buf.readVarInt();
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    Ingredient container = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    return new ColdBrewTeapotRecipe(teaFluid, ingredient, ingredientCount, time, result, container);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ColdBrewTeapotRecipe recipe) {
                    buf.writeResourceLocation(recipe.teaFluid());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient());
                    buf.writeVarInt(recipe.ingredientCount());
                    buf.writeVarInt(recipe.time());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.container());
                }
            };

    /**
     * 获取配方的数据编解码器。
     *
     * @return 数据编解码器
     */
    @Override
    public MapCodec<ColdBrewTeapotRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方的流编解码器。
     *
     * @return 流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ColdBrewTeapotRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
