package com.tequilacat.kaleidoscope_startale.data.builder;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdBrewTeapotRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

/**
 * 冷萃茶壶配方的数据生成构建器。
 * <p>
 * 以链式调用方式设置茶液、原料、产出、容器与处理时长等参数，
 * 支持以物品或标识符形式设置字段，并提供多种保存入口。
 * 容器默认使用空杯物品。
 */
public class ColdBrewTeapotRecipeBuilder {

    /** 所需茶液标识符 */
    private ResourceLocation teaFluid;
    /** 所需原料 */
    private Ingredient ingredient;
    /** 产出结果物品 */
    private ItemStack result;
    /** 取成品所需的容器，默认使用空杯 */
    private Ingredient container = Ingredient.of(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.EMPTY_CUP.get());
    /** 所需原料数量，默认 1 */
    private int ingredientCount = 1;
    /** 处理时长（刻），默认 2400 */
    private int time = 2400;

    /**
     * 创建构建器实例。
     *
     * @return 新的构建器实例
     */
    public static ColdBrewTeapotRecipeBuilder builder() {
        return new ColdBrewTeapotRecipeBuilder();
    }

    /**
     * 设置所需茶液。
     *
     * @param fluid 茶液流体
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setTeaFluid(Fluid fluid) {
        this.teaFluid = BuiltInRegistries.FLUID.getKey(fluid);
        return this;
    }

    /**
     * 设置所需原料。
     *
     * @param ingredient 原料
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    /**
     * 以物品标识符设置所需原料。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setIngredient(ResourceLocation itemId) {
        this.ingredient = Ingredient.of(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 设置所需原料数量。
     *
     * @param count 原料数量
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setIngredientCount(int count) {
        this.ingredientCount = count;
        return this;
    }

    /**
     * 设置产出结果物品。
     *
     * @param result 产出物品堆
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    /**
     * 以物品标识符设置产出结果物品。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setResult(ResourceLocation itemId) {
        this.result = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 设置取成品所需的容器。
     *
     * @param container 容器原料
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setContainer(Ingredient container) {
        this.container = container;
        return this;
    }

    /**
     * 以物品标识符设置取成品所需的容器。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setContainer(ResourceLocation itemId) {
        this.container = Ingredient.of(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 设置处理时长。
     *
     * @param time 处理时长（刻）
     * @return 当前构建器实例
     */
    public ColdBrewTeapotRecipeBuilder setTime(int time) {
        this.time = time;
        return this;
    }

    /**
     * 保存配方，使用基于产出物品路径自动生成的标识符。
     *
     * @param output 配方输出
     */
    public void save(RecipeOutput output) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "cold_brew_teapot/" + itemId.getPath());
        save(output, id);
    }

    /**
     * 保存配方，使用指定标识符。
     *
     * @param output 配方输出
     * @param id     配方标识符
     */
    public void save(RecipeOutput output, ResourceLocation id) {
        ColdBrewTeapotRecipe recipe = new ColdBrewTeapotRecipe(teaFluid, ingredient, ingredientCount, time, result, container);
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