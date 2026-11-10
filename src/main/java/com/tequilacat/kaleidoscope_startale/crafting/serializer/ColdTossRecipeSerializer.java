package com.tequilacat.kaleidoscope_startale.crafting.serializer;

import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdTossRecipe;
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
 * 冷炒配方序列化器。
 * <p>
 * 定义冷炒配方的数据编解码器与网络流编解码器。
 */
public class ColdTossRecipeSerializer implements RecipeSerializer<ColdTossRecipe> {

    /** 翻炒次数默认值 */
    public static final int DEFAULT_STIR_FRY_COUNT = 5;

    /** 配方数据编解码器，用于从配方 JSON 读取与写出 */
    private static final MapCodec<ColdTossRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(ColdTossRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(ColdTossRecipe::result),
            Codec.INT.optionalFieldOf("stir_fry_count", DEFAULT_STIR_FRY_COUNT).forGetter(ColdTossRecipe::stirFryCount),
            Ingredient.CODEC.optionalFieldOf("carrier", Ingredient.EMPTY).forGetter(ColdTossRecipe::carrier)
    ).apply(inst, ColdTossRecipe::new));

    /** 配方网络流编解码器，用于在客户端与服务端之间同步配方 */
    private static final StreamCodec<RegistryFriendlyByteBuf, ColdTossRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ColdTossRecipe::ingredients,
            ItemStack.STREAM_CODEC, ColdTossRecipe::result,
            ByteBufCodecs.VAR_INT, ColdTossRecipe::stirFryCount,
            Ingredient.CONTENTS_STREAM_CODEC, ColdTossRecipe::carrier,
            ColdTossRecipe::new
    );

    /**
     * 获取配方数据编解码器。
     *
     * @return 配方数据编解码器
     */
    @Override
    public MapCodec<ColdTossRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方网络流编解码器。
     *
     * @return 配方网络流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ColdTossRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}