package com.tequilacat.kaleidoscope_startale.data.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ScentingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 窨制配方序列化器。
 * <p>
 * 负责窨制配方的编解码：原料列表、产物与时长的数据编解码，
 * 以及面向网络的流编解码。原料数量限制为最多 3 个，时长有默认值。
 */
public class ScentingRecipeBuilder implements RecipeSerializer<ScentingRecipe> {

    /** 窨制配方最多支持的原料数量。 */
    public static final int MAX_INGREDIENTS = 3;

    /** 窨制时长默认值（刻），即 60 秒。 */
    public static final int DEFAULT_DURATION = 20 * 60;

    /**
     * 窨制配方的数据编解码器。
     * <p>
     * 原料列表在编解码时校验数量不超过最大原料数；时长为可选，缺省时使用默认值。
     */
    private static final MapCodec<ScentingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().validate(list -> list.size() > MAX_INGREDIENTS
                            ? DataResult.error(() -> "ingredients size must be <= " + MAX_INGREDIENTS)
                            : DataResult.success(list))
                    .fieldOf("ingredients").forGetter(ScentingRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(ScentingRecipe::result),
            Codec.INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(ScentingRecipe::duration)
    ).apply(instance, ScentingRecipe::new));

    /**
     * 窨制配方的流编解码器，用于网络传输。
     * <p>
     * 编码时写入原料数量并逐个写入原料；解码时按数量读取并限制不超过最大原料数，
     * 随后依次读出产物与时长。
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, ScentingRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ScentingRecipe decode(RegistryFriendlyByteBuf buf) {
                    int size = Math.min(MAX_INGREDIENTS, buf.readVarInt());
                    List<Ingredient> ingredients = new ArrayList<>(size);
                    for (int i = 0; i < size; ++i) {
                        ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }

                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    int duration = buf.readVarInt();
                    return new ScentingRecipe(ingredients, result, duration);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ScentingRecipe recipe) {
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
    public MapCodec<ScentingRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方的流编解码器。
     *
     * @return 流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ScentingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
