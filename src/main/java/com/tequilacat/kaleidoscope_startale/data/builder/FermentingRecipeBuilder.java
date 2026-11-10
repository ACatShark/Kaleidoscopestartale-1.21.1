package com.tequilacat.kaleidoscope_startale.data.builder;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.FermentingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 发酵配方的数据生成构建器。
 * <p>
 * 以链式调用方式设置原料、产出、发酵时长与光照区间等参数，
 * 支持以物品或标识符形式设置字段，并提供多种保存入口。
 * 光照区间默认为 0 到 3，即光照小于 4 时可发酵。
 */
public class FermentingRecipeBuilder {

    /** 所需原料 */
    private Ingredient ingredient;
    /** 产出结果物品 */
    private ItemStack result;
    /** 发酵时长（刻），默认 1200 */
    private int duration = 1200;
    /** 允许发酵的最低光照，默认 0 */
    private int minLight = 0;
    /** 允许发酵的最高光照，默认 3 */
    private int maxLight = 3;

    /**
     * 创建构建器实例。
     *
     * @return 新的构建器实例
     */
    public static FermentingRecipeBuilder builder() {
        return new FermentingRecipeBuilder();
    }

    /**
     * 设置所需原料。
     *
     * @param ingredient 原料
     * @return 当前构建器实例
     */
    public FermentingRecipeBuilder setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    /**
     * 以物品标识符设置所需原料。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public FermentingRecipeBuilder setIngredient(ResourceLocation itemId) {
        this.ingredient = Ingredient.of(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 设置产出结果物品。
     *
     * @param result 产出物品堆
     * @return 当前构建器实例
     */
    public FermentingRecipeBuilder setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    /**
     * 以物品标识符设置产出结果物品。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public FermentingRecipeBuilder setResult(ResourceLocation itemId) {
        this.result = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 设置发酵时长。
     *
     * @param duration 发酵时长（刻）
     * @return 当前构建器实例
     */
    public FermentingRecipeBuilder setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * 设置允许发酵的最低光照。
     *
     * @param minLight 最低光照
     * @return 当前构建器实例
     */
    public FermentingRecipeBuilder setMinLight(int minLight) {
        this.minLight = minLight;
        return this;
    }

    /**
     * 设置允许发酵的最高光照。
     *
     * @param maxLight 最高光照
     * @return 当前构建器实例
     */
    public FermentingRecipeBuilder setMaxLight(int maxLight) {
        this.maxLight = maxLight;
        return this;
    }

    /**
     * 保存配方，使用基于产出物品路径自动生成的标识符。
     *
     * @param output 配方输出
     */
    public void save(RecipeOutput output) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "fermenting/" + itemId.getPath());
        save(output, id);
    }

    /**
     * 保存配方，使用指定标识符。
     *
     * @param output 配方输出
     * @param id     配方标识符
     */
    public void save(RecipeOutput output, ResourceLocation id) {
        FermentingRecipe recipe = new FermentingRecipe(ingredient, result, duration, minLight, maxLight);
        output.accept(id, recipe, null);
    }

    /**
     * 保存配方，使用字符串解析为标识符。
     *
     * @param output 配方输出
     * @param name   配方标识符字符串
     */
    public void save(RecipeOutput output, String name) {
        save(output, ResourceLocation.parse(name));
    }
}