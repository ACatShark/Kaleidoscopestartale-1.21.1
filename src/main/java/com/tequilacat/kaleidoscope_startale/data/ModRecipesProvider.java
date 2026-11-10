package com.tequilacat.kaleidoscope_startale.data;

import com.github.ysbbbbbb.kaleidoscopecookery.datagen.builder.PotRecipeBuilder;
import com.github.ysbbbbbb.kaleidoscopecookery.datagen.builder.StockpotRecipeBuilder;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.TeacupRegistry;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagCommon;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder.BarrelBuilder;
import com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder.PressingTubBuilder;
import com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder.ShakerBuilder;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModFluids;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.data.builder.*;
import com.tequilacat.kaleidoscope_startale.init.KSFluids;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import com.tequilacat.kaleidoscope_startale.init.KSTeacups;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        modRecipes(output);
        ColdBrewTeapotRecipes(output);
        FermentingRecipes(output);
        ScentingRecipes(output);
        ColdTossRecipes(output);
        ColdStewRecipes(output);
        JuicerRecipes(output);
        StockpotRecipes(output);
        PotRecipes(output);
        ShakerRecipes(output);
        PressingTubRecipes(output);
        BarrelTubRecipes(output);
    }

    /** 模组配方 */
    private void modRecipes(RecipeOutput output) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KSItems.JUICER.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('B', Items.GLASS)
                .define('C', Items.BUCKET)
                .unlockedBy(getHasName(Items.HEAVY_WEIGHTED_PRESSURE_PLATE), has(Items.HEAVY_WEIGHTED_PRESSURE_PLATE))
                .unlockedBy(getHasName(Items.GLASS), has(Items.GLASS))
                .unlockedBy(getHasName(Items.BUCKET), has(Items.BUCKET))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KSItems.PETTER.get())
                .pattern("ABA")
                .pattern(" C ")
                .pattern(" D ")
                .define('A', Items.LIGHT_BLUE_DYE)
                .define('B', Items.CARROT)
                .define('C', Items.IRON_INGOT)
                .define('D', Items.STICK)
                .unlockedBy(getHasName(Items.LIGHT_BLUE_DYE), has(Items.LIGHT_BLUE_DYE))
                .unlockedBy(getHasName(Items.CARROT), has(Items.CARROT))
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, KSItems.ANCIENT_FRUIT_PLATTER.get())
                .requires(Items.BOWL)
                .requires(KSItems.ANCIENT_FRUIT,4 )
                .unlockedBy(getHasName(Items.BOWL), has(Items.BOWL))
                .unlockedBy(getHasName(KSItems.ANCIENT_FRUIT), has(KSItems.ANCIENT_FRUIT))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, KSItems.LIVESTOCK_FEED.get())
                .requires(ModItems.LETTUCE, 2)
                .requires(ModItems.RICE_PANICLE,2)
                .requires(ModItems.TOMATO,2)
                .unlockedBy(getHasName(ModItems.LETTUCE), has(ModItems.LETTUCE))
                .unlockedBy(getHasName(ModItems.RICE_PANICLE), has(ModItems.RICE_PANICLE))
                .unlockedBy(getHasName(ModItems.TOMATO), has(ModItems.TOMATO))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, KSItems.POULTRY_FEED.get())
                .requires(ModItems.CATERPILLAR)
                .requires(ModItems.CHILI_SEED)
                .requires(ModItems.WILD_RICE_SEED)
                .requires(ModItems.LETTUCE_SEED)
                .requires(ModItems.TOMATO_SEED)
                .unlockedBy(getHasName(ModItems.CATERPILLAR), has(ModItems.CATERPILLAR))
                .unlockedBy(getHasName(ModItems.CHILI_SEED), has(ModItems.CHILI_SEED))
                .unlockedBy(getHasName(ModItems.WILD_RICE_SEED), has(ModItems.WILD_RICE_SEED))
                .unlockedBy(getHasName(ModItems.LETTUCE_SEED), has(ModItems.LETTUCE_SEED))
                .unlockedBy(getHasName(ModItems.TOMATO_SEED), has(ModItems.TOMATO_SEED))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, KSItems.UNIVERSAL_FEED.get())
                .requires(Items.POTATO, 2)
                .requires(ModItems.LETTUCE, 2)
                .requires(Items.BONE_MEAL, 2)
                .requires(Tags.Items.SEEDS)
                .requires(Items.PAPER)
                .unlockedBy(getHasName(Items.POTATO), has(Items.POTATO))
                .unlockedBy(getHasName(ModItems.LETTUCE), has(ModItems.LETTUCE))
                .unlockedBy(getHasName(Items.BONE_MEAL), has(Items.BONE_MEAL))
                .unlockedBy(getHasName(Items.WHEAT_SEEDS), has(Tags.Items.SEEDS))
                .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                .save(output);

    }

    /** 榨汁机配方 */
    private void JuicerRecipes(RecipeOutput output) {

        // 上古水果 → 上古水果汁
        JuicerRecipeBuilder.builder()
                .addIngredient(Ingredient.of(KSItems.ANCIENT_FRUIT.get()))
                .setFluid(KSFluids.ANCIENT_FRUIT_JUICE.get())
                .save(output);

    }

    /** 冷萃配方 */
    private void ColdBrewTeapotRecipes(RecipeOutput output) {

        // 抹茶粉 + 水 → 抹茶
        ColdBrewTeapotRecipeBuilder.builder()
                .setTeaFluid(Fluids.WATER)
                .setIngredient(Ingredient.of(KSItems.MATCHA_POWDER.get()))
                .setIngredientCount(1)
                .setResult(new ItemStack(TeacupRegistry.getItem(KSTeacups.MATCHA_TEA)))
                .setContainer(Ingredient.of(KSItems.YUNOMI.get()))
                .setTime(240)
                .save(output);

    }

    /** 发酵配方 */
    private void FermentingRecipes(RecipeOutput output) {

        // 钻石 → 绿宝石
        FermentingRecipeBuilder.builder()
                .setIngredient(Ingredient.of(Items.DIAMOND))
                .setResult(new ItemStack(Items.EMERALD))
                .setDuration(1200)
                .setMinLight(10)
                .setMaxLight(12)
                .save(output, KaleidoscopeStartale.id("fermenting/diamond"));

    }

    /** 窨制配方 */
    private void ScentingRecipes(RecipeOutput output) {

        // 绿宝石 ×3 → 钻石
        ScentingRecipeBuilder.builder()
                .setIngredients(Items.EMERALD, Items.EMERALD, Items.EMERALD)
                .setResult(new ItemStack(Items.DIAMOND))
                .setDuration(1200)
                .save(output, KaleidoscopeStartale.id("scenting/emerald"));

    }

    /** 冷炒配方 */
    private void ColdTossRecipes(RecipeOutput output) {

        // 铁锭 → 铁块
        ColdTossRecipeBuilder.builder()
                .addIngredient(Ingredient.of(Items.IRON_INGOT))
                .setResult(new ItemStack(Items.IRON_BLOCK))
                .setStirFryCount(5)
                .setCarrier(Ingredient.of(Items.BOWL))
                .save(output);

        // 钻石 → 钻石块
        ColdTossRecipeBuilder.builder()
                .addIngredient(Ingredient.of(Items.DIAMOND))
                .setResult(new ItemStack(Items.DIAMOND_BLOCK))
                .setStirFryCount(5)
                .save(output);

    }

    /** 冷炖配方 */
    private void ColdStewRecipes(RecipeOutput output) {

        // 牛奶 + 牛肉 → 熟牛肉
        ColdStewRecipeBuilder.builder()
                .setSoupBase(Ingredient.of(Items.MILK_BUCKET))
                .addIngredient(Ingredient.of(Items.BEEF))
                .setResult(new ItemStack(Items.COOKED_BEEF))
                .setDuration(600)
                .save(output);

        // 细雪桶 + 钻石 + 抹茶粉 → 蘑菇煲
        ColdStewRecipeBuilder.builder()
                .setSoupBase(Ingredient.of(Items.POWDER_SNOW_BUCKET))
                .addIngredient(Ingredient.of(Items.DIAMOND))
                .addIngredient(Ingredient.of(KSItems.MATCHA_POWDER.get()))
                .setResult(new ItemStack(Items.MUSHROOM_STEW))
                .setDuration(800)
                .save(output);

    }

    /** 森罗物语：厨房 - 汤锅 */
    private void StockpotRecipes(RecipeOutput output) {

        StockpotRecipeBuilder.builder()
                .addInput(
                        KSItems.RHUBARB, KSItems.RHUBARB, KSItems.RHUBARB,
                        KSItems.RHUBARB, KSItems.RHUBARB, KSItems.RHUBARB
                )
                .setCarrier(Items.GLASS_BOTTLE)
                .setResult(KSItems.RHUBARB_JAM.get(), 1)
                .save(output, "rhubarb_jam");
        StockpotRecipeBuilder.builder()
                .addInput(
                        KSItems.AMARANTH, KSItems.AMARANTH, KSItems.AMARANTH
                )
                .setResult(KSItems.AMARANTH_SOUP.get(), 1)
                .save(output, "amaranth_soup");
        StockpotRecipeBuilder.builder()
                .addInput(
                        KSItems.GOLDFISH, KSItems.GOLDFISH,
                        Items.CARROT, Items.CARROT,
                        Tags.Items.MUSHROOMS, Tags.Items.MUSHROOMS
                )
                .setCarrier(Items.BOWL)
                .setSoupBase(ModSoupBases.MILK)
                .setResult(KSItems.FISH_MUSHROOM_SOUP.get(), 1)
                .save(output, "fish_mushroom_soup");
        StockpotRecipeBuilder.builder()
                .addInput(
                        KSItems.ENCHANTED_NIGHTCRAWLER, TagMod.CATERPILLARS, TagMod.CATERPILLARS,
                        KSItems.AMARANTH, KSItems.AMARANTH
                )
                .setCarrier(Items.BOWL)
                .setSoupBase(ModSoupBases.MILK)
                .setResult(KSItems.GLOWING_CATERPILLAR_SOUP.get(), 1)
                .save(output, "glowing_caterpillar_soup");

    }

    /** 森罗物语：厨房 - 炒锅 */
    private void PotRecipes(RecipeOutput output) {

        PotRecipeBuilder.builder()
                .addInput(KSItems.ANCIENT_FRUIT, KSItems.ANCIENT_FRUIT, KSItems.ANCIENT_FRUIT)
                .setCarrier(Items.STICK)
                .setStirFryCount(3)
                .setResult(KSItems.ANCIENT_FRUIT_SKEWER.get())
                .save(output);
        PotRecipeBuilder.builder()
                .addInput(Tags.Items.FOODS_DOUGH, TagCommon.COOKED_EGGS, TagCommon.RAW_MEATS)
                .setResult(KSItems.EGG_BAKED_CAKE.get())
                .setStirFryCount(3)
                .save(output);
        PotRecipeBuilder.builder()
                .addInput(KSItems.RHUBARB, KSItems.ANCIENT_FRUIT, KSItems.AMARANTH, Items.WHEAT, Items.WHEAT, Items.COCOA_BEANS)
                .setStirFryCount(6)
                .setResult(KSItems.IDEA_COOKIE.get())
                .save(output);
        PotRecipeBuilder.builder()
                .addInput(Tags.Items.MUSHROOMS, KSItems.RHUBARB, KSItems.RHUBARB, KSItems.AMARANTH, KSItems.AMARANTH)
                .setStirFryCount(6)
                .setResult(KSItems.INSPIRATION_MUSHROOM.get())
                .save(output);
        PotRecipeBuilder.builder()
                .addInput(TagCommon.COOKED_EGGS, TagCommon.COOKED_EGGS, TagCommon.COOKED_RICE, TagCommon.CROPS_TOMATO, ModItems.LETTUCE, Items.CARROT)
                .setCarrier(Items.BOWL)
                .setStirFryCount(5)
                .setResult(KSItems.OMELETTE_RICE.get())
                .save(output);
        PotRecipeBuilder.builder()
                .addInput(Items.SUGAR, Items.SUGAR, Items.SUGAR, Items.SUGAR, Items.PUMPKIN_PIE, Items.PUMPKIN_PIE, Items.PUMPKIN_PIE, Items.PUMPKIN_PIE, KSItems.RHUBARB_JAM)
                .setCarrier(Items.BOWL)
                .setStirFryCount(5)
                .setResult(KSItems.RHUBARB_FONDANT_PIE.get())
                .save(output);
        PotRecipeBuilder.builder()
                .addInput(Items.SLIME_BALL, Items.SLIME_BALL, KSItems.ANCIENT_FRUIT, KSItems.ANCIENT_FRUIT, Items.SWEET_BERRIES, Items.GOLD_INGOT)
                .setCarrier(Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
                .setStirFryCount(5)
                .setResult(KSItems.SLIME_KING_FLAVORED_JELLY.get())
                .save(output);

    }

    /** 森罗物语：酒馆 - 雪克杯 */
    private void ShakerRecipes(RecipeOutput output) {

        ShakerBuilder.builder()
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_BLUE)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_BLUE)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_WHITE)
                .setResult(KSItems.BUBBLE_ECHO_COCKTAIL.get())
                .save(output);
        ShakerBuilder.builder()
                .addIngredient(KSItems.ANCIENT_FRUIT_WINE)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_BLUE)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_RED)
                .setResult(KSItems.ICE_ELF_COCKTAIL.get())
                .save(output);
        ShakerBuilder.builder()
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_RED)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_RED)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE)
                .setResult(KSItems.SPRING_DRINK_COCKTAIL.get())
                .save(output);
        ShakerBuilder.builder()
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.SAKURA_WINE)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_RED)
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE)
                .setResult(KSItems.SPRING_DRINK_1_COCKTAIL.get())
                .save(output);


    }

    /** 森罗物语：酒馆 - 果盆 */
    private void PressingTubRecipes(RecipeOutput output) {

        PressingTubBuilder.builder()
                .setIngredient(KSItems.ANCIENT_FRUIT)
                .setFluid(KSFluids.ANCIENT_FRUIT_JUICE.get())
                .save(output);

    }

    /** 森罗物语：酒馆 - 酒桶 */
    private void BarrelTubRecipes(RecipeOutput output) {

        BarrelBuilder.builder()
                .setFluid(KSFluids.ANCIENT_FRUIT_JUICE.get())
                .addIngredient(Items.GLOW_BERRIES)
                .addIngredient(ItemTags.FLOWERS)
                .setResult(KSItems.ANCIENT_FRUIT_WINE.get())
                .save(output);
        BarrelBuilder.builder()
                .setFluid(ModFluids.GRAPE_JUICE.get())
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.WINE.get())
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.GOLD_GRAPE.get())
                .addIngredient(com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.ICE_GRAPE.get())
                .setResult(KSItems.GRAPE_FLAVORED_WINE.get())
                .save(output);
        BarrelBuilder.builder()
                .setFluid(Fluids.WATER)
                .addIngredient(Items.SUGAR)
                .addIngredient(Items.APPLE)
                .addIngredient(Items.IRON_INGOT)
                .setResult(KSItems.JOJA_COLA.get())
                .save(output);
        BarrelBuilder.builder()
                .setFluid(ModFluids.SWEET_BERRIES_JUICE.get())
                .addIngredient(Items.FERMENTED_SPIDER_EYE)
                .addIngredient(Items.NETHER_WART)
                .addIngredient(Items.REDSTONE)
                .addIngredient(TagCommon.RAW_MEATS)
                .setResult(KSItems.RED_EYE_WINE.get())
                .save(output);
        BarrelBuilder.builder()
                .setFluid(Fluids.WATER)
                .addIngredient(Items.WHEAT_SEEDS)
                .addIngredient(Items.ICE)
                .setResult(KSItems.REFRESHING_BEER.get())
                .save(output);
        BarrelBuilder.builder()
                .setFluid(Fluids.WATER)
                .addIngredient(Items.WHEAT_SEEDS)
                .addIngredient(Items.WHEAT)
                .addIngredient(Items.GOLD_NUGGET)
                .setResult(KSItems.YANJING_U7.get())
                .save(output);

    }
}
