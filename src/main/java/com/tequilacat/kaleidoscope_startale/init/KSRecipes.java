package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.*;
import com.tequilacat.kaleidoscope_startale.data.builder.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface KSRecipes {
    DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, KaleidoscopeStartale.MODID);

    /** 榨汁机 */
    ResourceLocation JUICER_ID = KaleidoscopeStartale.id("juicer");
    /** 冷萃 */
    ResourceLocation COLD_BREW_TEAPOT_ID = KaleidoscopeStartale.id("cold_brew_teapot");
    /** 发酵 */
    ResourceLocation FERMENTING_ID = KaleidoscopeStartale.id("fermenting");
    /** 窨制 */
    ResourceLocation SCENTING_ID = KaleidoscopeStartale.id("scenting");
    /** 冷炒 */
    ResourceLocation COLD_TOSS_ID = KaleidoscopeStartale.id("cold_toss");
    /** 冷炖 */
    ResourceLocation COLD_STEW_ID = KaleidoscopeStartale.id("cold_stew");


    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JuicerRecipe>> JUICER_SERIALIZER = RECIPE_SERIALIZERS.register("juicer", JuicerRecipeBuilder::new);
    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ColdBrewTeapotRecipe>> COLD_BREW_TEAPOT_SERIALIZER = RECIPE_SERIALIZERS.register("cold_brew_teapot", ColdBrewTeapotRecipeBuilder::new);
    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FermentingRecipe>> FERMENTING_SERIALIZER = RECIPE_SERIALIZERS.register("fermenting", FermentingRecipeBuilder::new);
    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ScentingRecipe>> SCENTING_SERIALIZER = RECIPE_SERIALIZERS.register("scenting", ScentingRecipeBuilder::new);
    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ColdTossRecipe>> COLD_TOSS_SERIALIZER = RECIPE_SERIALIZERS.register("cold_toss", ColdTossRecipeBuilder::new);
    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ColdStewRecipe>> COLD_STEW_SERIALIZER = RECIPE_SERIALIZERS.register("cold_stew", ColdStewRecipeBuilder::new);


    RecipeType<JuicerRecipe> JUICER_TYPE = RecipeType.simple(JUICER_ID);
    RecipeType<ColdBrewTeapotRecipe> COLD_BREW_TEAPOT_TYPE = RecipeType.simple(COLD_BREW_TEAPOT_ID);
    RecipeType<FermentingRecipe> FERMENTING_TYPE = RecipeType.simple(FERMENTING_ID);
    RecipeType<ScentingRecipe> SCENTING_TYPE = RecipeType.simple(SCENTING_ID);
    RecipeType<ColdTossRecipe> COLD_TOSS_TYPE = RecipeType.simple(COLD_TOSS_ID);
    RecipeType<ColdStewRecipe> COLD_STEW_TYPE = RecipeType.simple(COLD_STEW_ID);

    static void register(IEventBus bus){
        RECIPE_SERIALIZERS.register(bus);
    }
}
