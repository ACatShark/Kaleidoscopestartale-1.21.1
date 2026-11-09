package tequila.cat.pigpig.crafting;

import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;

public class JuicerRecipeSerializer implements RecipeSerializer<JuicerRecipe> {
    public static final int MAX_INGREDIENTS = 4;
    public static final int DEFAULT_FLUID_AMOUNT = 250;
    /** 默认榨汁时间。原为 100 tick，按 1.7 倍速缩短为 59 tick。 */
    public static final int DEFAULT_CRAFT_TIME = 59;

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

    private static NonNullList<Ingredient> toNonNullList(List<Ingredient> list) {
        int size = Math.min(list.size(), MAX_INGREDIENTS);
        NonNullList<Ingredient> nonnull = NonNullList.withSize(size, Ingredient.EMPTY);
        for (int i = 0; i < size; ++i) {
            nonnull.set(i, list.get(i));
        }
        return nonnull;
    }

    @Override
    public MapCodec<JuicerRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, JuicerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
