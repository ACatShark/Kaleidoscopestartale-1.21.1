package tequila.cat.pigpig.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.crafting.JuicerRecipe;
import tequila.cat.pigpig.crafting.JuicerRecipeSerializer;

public final class ModRecipes {
    public static final ResourceLocation JUICER_ID =
            ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "juicer");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, KaleidoscopeStartale.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JuicerRecipe>> JUICER_SERIALIZER =
            RECIPE_SERIALIZERS.register("juicer", JuicerRecipeSerializer::new);

    public static final RecipeType<JuicerRecipe> JUICER_TYPE = RecipeType.simple(JUICER_ID);

    private ModRecipes() {
    }
}
