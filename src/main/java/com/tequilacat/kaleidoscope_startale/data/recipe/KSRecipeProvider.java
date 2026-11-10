package com.tequilacat.kaleidoscope_startale.data.recipe;

import com.github.ysbbbbbb.kaleidoscopecookery.datagen.recipe.ModRecipeProvider;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.TeacupRegistry;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdBrewTeapotRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdStewRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdTossRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.FermentingRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.JuicerRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ScentingRecipe;
import com.tequilacat.kaleidoscope_startale.init.KSFluids;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import com.tequilacat.kaleidoscope_startale.init.KSTeacups;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 茶的配方（冷萃茶壶、发酵、窨制、冷炒、冷炖）的数据生成。
 * <p>
 * 合并自茶志项目：原先依赖的 {@code data.builder.XxxRecipeBuilder} 链式构建器已被本项目的
 * 配方序列化器占用同名类，因此这里直接构造配方记录对象，用 {@link RecipeOutput#accept} 输出。
 */
public class KSRecipeProvider extends ModRecipeProvider {
    public KSRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        buildColdBrewTeapotRecipes(output);
        buildFermentingRecipes(output);
        buildScentingRecipes(output);
        buildColdTossRecipes(output);
        buildColdStewRecipes(output);
        buildJuicerRecipes(output);
    }

    /** 榨汁机配方 */
    private void buildJuicerRecipes(RecipeOutput output) {

        // 上古水果 → 上古水果汁
        output.accept(KaleidoscopeStartale.id("juicer/ancient_fruit_juice"),
                new JuicerRecipe(
                        NonNullList.of(Ingredient.EMPTY, Ingredient.of(KSItems.ANCIENT_FRUIT.get())),
                        KSFluids.ANCIENT_FRUIT_JUICE.get(),
                        250,
                        59,
                        ItemStack.EMPTY),
                null);

    }

    /** 冷萃配方 */
    private void buildColdBrewTeapotRecipes(RecipeOutput output) {

        // 抹茶粉 + 水 → 抹茶
        output.accept(KaleidoscopeStartale.id("cold_brew_teapot/matcha_tea"),
                new ColdBrewTeapotRecipe(
                        ResourceLocation.withDefaultNamespace("water"),
                        Ingredient.of(KSItems.MATCHA_POWDER.get()),
                        1,
                        240,
                        new ItemStack(TeacupRegistry.getItem(KSTeacups.MATCHA_TEA)),
                        Ingredient.of(KSItems.YUNOMI.get())),
                null);

    }

    /** 发酵配方 */
    private void buildFermentingRecipes(RecipeOutput output) {

        // 钻石 → 绿宝石
        output.accept(KaleidoscopeStartale.id("fermenting/diamond"),
                new FermentingRecipe(
                        Ingredient.of(Items.DIAMOND),
                        new ItemStack(Items.EMERALD),
                        1200,
                        10,
                        12),
                null);

    }

    /** 窨制配方 */
    private void buildScentingRecipes(RecipeOutput output) {

        // 绿宝石 ×3 → 钻石
        output.accept(KaleidoscopeStartale.id("scenting/emerald"),
                new ScentingRecipe(
                        List.of(Ingredient.of(Items.EMERALD),
                                Ingredient.of(Items.EMERALD),
                                Ingredient.of(Items.EMERALD)),
                        new ItemStack(Items.DIAMOND),
                        1200),
                null);

    }

    /** 冷炒配方 */
    private void buildColdTossRecipes(RecipeOutput output) {

        // 铁锭 → 铁块
        output.accept(KaleidoscopeStartale.id("cold_toss/iron_block"),
                new ColdTossRecipe(
                        List.of(Ingredient.of(Items.IRON_INGOT)),
                        new ItemStack(Items.IRON_BLOCK),
                        5,
                        Ingredient.of(Items.BOWL)),
                null);

        // 钻石 → 钻石块
        output.accept(KaleidoscopeStartale.id("cold_toss/diamond_block"),
                new ColdTossRecipe(
                        List.of(Ingredient.of(Items.DIAMOND)),
                        new ItemStack(Items.DIAMOND_BLOCK),
                        5,
                        Ingredient.EMPTY),
                null);

    }

    /** 冷炖配方 */
    private void buildColdStewRecipes(RecipeOutput output) {

        // 牛奶 + 牛肉 → 熟牛肉
        output.accept(KaleidoscopeStartale.id("cold_stew/cooked_beef"),
                new ColdStewRecipe(
                        Ingredient.of(Items.MILK_BUCKET),
                        List.of(Ingredient.of(Items.BEEF)),
                        new ItemStack(Items.COOKED_BEEF),
                        600),
                null);

        // 细雪桶 + 钻石 + 抹茶粉 → 蘑菇煲
        output.accept(KaleidoscopeStartale.id("cold_stew/mushroom_stew"),
                new ColdStewRecipe(
                        Ingredient.of(Items.POWDER_SNOW_BUCKET),
                        List.of(Ingredient.of(Items.DIAMOND),
                                Ingredient.of(KSItems.MATCHA_POWDER.get())),
                        new ItemStack(Items.MUSHROOM_STEW),
                        800),
                null);

    }
}
