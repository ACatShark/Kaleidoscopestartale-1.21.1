package com.tequilacat.kaleidoscope_startale.crafting.serializer;

import com.tequilacat.kaleidoscope_startale.crafting.recipe.ScentingRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 窨制配方序列化器。
 * <p>
 * 定义窨制配方的数据编解码器与网络流编解码器，
 */
public class ScentingRecipeSerializer implements RecipeSerializer<ScentingRecipe> {

    /** 窨制配方最多支持的原料数量 */
    public static final int MAX_INGREDIENTS = 3;

    /** 窨制时长默认值（刻），即 60 秒 */
    public static final int DEFAULT_DURATION = 20 * 60;

    /** 配方数据编解码器，用于从配方 JSON 读取与写出，限制 ingredients 最多 3 个 */
    private static final MapCodec<ScentingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().validate(list -> list.size() > MAX_INGREDIENTS
                    ? DataResult.error(() -> "ingredients size must be <= " + MAX_INGREDIENTS)
                    : DataResult.success(list))
                    .fieldOf("ingredients").forGetter(ScentingRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(ScentingRecipe::result),
            Codec.INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(ScentingRecipe::duration)
    ).apply(instance, ScentingRecipe::new));

    /** 配方网络流编解码器，用于在客户端与服务端之间同步配方 */
    private static final StreamCodec<RegistryFriendlyByteBuf, ScentingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ScentingRecipe::ingredients,
            ItemStack.STREAM_CODEC, ScentingRecipe::result,
            ByteBufCodecs.VAR_INT, ScentingRecipe::duration,
            ScentingRecipe::new);

    /**
     * 获取配方数据编解码器。
     *
     * @return 配方数据编解码器
     */
    @Override
    public MapCodec<ScentingRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方网络流编解码器。
     *
     * @return 配方网络流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ScentingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}