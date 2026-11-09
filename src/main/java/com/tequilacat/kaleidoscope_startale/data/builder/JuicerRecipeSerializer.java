package com.tequilacat.kaleidoscope_startale.data.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.JuicerRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Objects;

/**
 * 榨汁机配方序列化器。
 * <p>
 * 负责榨汁机配方的编解码：物品配方与流体的数据编解码，
 * 以及面向网络的流编解码。原料数量限制为最多 4 个，
 * 流体量与制作时间均有默认值。
 */
public class JuicerRecipeSerializer implements RecipeSerializer<JuicerRecipe> {

    /** 最大原料数量。 */
    public static final int MAX_INGREDIENTS = 4;

    /** 默认流体量。 */
    public static final int DEFAULT_FLUID_AMOUNT = 250;

    /** 默认制作时间（刻）。 */
    public static final int DEFAULT_CRAFT_TIME = 59;

    /**
     * 榨汁机配方的数据编解码器。
     * <p>
     * 原料列表在编解码时过滤掉空原料并限制数量；
     * 流体量、制作时间与产物均为可选，缺省时使用默认值。
     */
    private static final MapCodec<JuicerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf()
                    .xmap(JuicerRecipeSerializer::toNonNullList,
                            nonnull -> nonnull.stream().filter(ingredient -> !ingredient.isEmpty()).toList())
                    .fieldOf("ingredients").forGetter(JuicerRecipe::ingredients),
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(JuicerRecipe::fluid),
            Codec.INT.optionalFieldOf("fluid_amount", DEFAULT_FLUID_AMOUNT).forGetter(JuicerRecipe::fluidAmount),
            Codec.INT.optionalFieldOf("time", DEFAULT_CRAFT_TIME).forGetter(JuicerRecipe::craftTime),
            ItemStack.CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(JuicerRecipe::result)
    ).apply(instance, JuicerRecipe::new));

    /**
     * 榨汁机配方的流编解码器，用于网络传输。
     * <p>
     * 编码时只写入非空原料并记录其数量，解码时按数量读取并限制不超过最大原料数；
     * 流体以注册名传输，流体量、制作时间与产物依次写入。
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, JuicerRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public JuicerRecipe decode(RegistryFriendlyByteBuf buf) {
                    int size = Math.min(MAX_INGREDIENTS, buf.readVarInt());
                    NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
                    for (int i = 0; i < size; ++i) {
                        ingredients.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }

                    ResourceLocation fluidId = buf.readResourceLocation();
                    Fluid fluid = Objects.requireNonNull(BuiltInRegistries.FLUID.get(fluidId));
                    int fluidAmount = buf.readVarInt();
                    int craftTime = buf.readVarInt();
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    return new JuicerRecipe(ingredients, fluid, fluidAmount, craftTime, result);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, JuicerRecipe recipe) {
                    List<Ingredient> nonEmpty = recipe.ingredients().stream()
                            .filter(ingredient -> !ingredient.isEmpty())
                            .toList();
                    buf.writeVarInt(nonEmpty.size());
                    for (Ingredient ingredient : nonEmpty) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }

                    ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(recipe.fluid());
                    buf.writeResourceLocation(Objects.requireNonNull(fluidId));
                    buf.writeVarInt(recipe.fluidAmount());
                    buf.writeVarInt(recipe.craftTime());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                }
            };

    /**
     * 将原料列表转换为定长非空列表。
     * <p>
     * 数量限制为不超过最大原料数，不足部分以空原料填充。
     *
     * @param list 原料列表
     * @return 定长非空原料列表
     */
    private static NonNullList<Ingredient> toNonNullList(List<Ingredient> list) {
        int size = Math.min(list.size(), MAX_INGREDIENTS);
        NonNullList<Ingredient> nonnull = NonNullList.withSize(size, Ingredient.EMPTY);
        for (int i = 0; i < size; ++i) {
            nonnull.set(i, list.get(i));
        }
        return nonnull;
    }

    /**
     * 获取配方的数据编解码器。
     *
     * @return 数据编解码器
     */
    @Override
    public MapCodec<JuicerRecipe> codec() {
        return CODEC;
    }

    /**
     * 获取配方的流编解码器。
     *
     * @return 流编解码器
     */
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, JuicerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}