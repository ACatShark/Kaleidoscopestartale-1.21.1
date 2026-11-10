package com.tequilacat.kaleidoscope_startale.crafting.serializer;

import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdStewRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 冷炖配方序列化器。
 * <p>
 * 定义冷炖配方的数据编解码器与网络流编解码器。
 */
public class ColdStewRecipeSerializer implements RecipeSerializer<ColdStewRecipe> {

    /** 处理时长默认值（刻） */
    public static final int DEFAULT_DURATION = 1200;

    /** 默认汤底 */
    public static final Ingredient DEFAULT_SOUP_BASE = Ingredient.of(Items.POWDER_SNOW_BUCKET, Items.BUCKET);

    /** 配方数据编解码器，用于从配方 JSON 读取与写出 */
    private static final MapCodec<ColdStewRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.optionalFieldOf("soup_base", DEFAULT_SOUP_BASE).forGetter(ColdStewRecipe::soupBase),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(ColdStewRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(ColdStewRecipe::result),
            Codec.INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(ColdStewRecipe::duration)
    ).apply(inst, ColdStewRecipe::new));

    /** 配方网络流编解码器，用于在客户端与服务端之间同步配方 */
    private static final StreamCodec<RegistryFriendlyByteBuf, ColdStewRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ColdStewRecipe::soupBase,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ColdStewRecipe::ingredients,
            ItemStack.STREAM_CODEC, ColdStewRecipe::result,
            ByteBufCodecs.VAR_INT, ColdStewRecipe::duration,
            ColdStewRecipe::new
    );

    /**
     * 获取配方数据编解码器。
     *
     * @return 配方数据编解码器
     */
    @Override
    public MapCodec<ColdStewRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方网络流编解码器。
     *
     * @return 配方网络流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ColdStewRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}