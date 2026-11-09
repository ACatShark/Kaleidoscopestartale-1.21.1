package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.JuicerRecipe;
import com.tequilacat.kaleidoscope_startale.data.builder.JuicerRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface KSRecipes {
    DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, KaleidoscopeStartale.MODID);

    ResourceLocation JUICER_ID = KaleidoscopeStartale.id("juicer");

    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JuicerRecipe>> JUICER_SERIALIZER = RECIPE_SERIALIZERS.register("juicer", JuicerRecipeSerializer::new);

    RecipeType<JuicerRecipe> JUICER_TYPE = RecipeType.simple(JUICER_ID);

    static void register(IEventBus bus){
        RECIPE_SERIALIZERS.register(bus);
    }
}
