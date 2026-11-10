package com.tequilacat.kaleidoscope_startale.data.builder;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdStewRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.serializer.ColdStewRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * 冷炖配方的数据生成构建器。
 * <p>
 * 以链式调用方式设置汤底、原料列表、产出与处理时长等参数，
 * 支持以物品或标识符形式设置字段，并提供多种保存入口。
 * 汤底默认使用序列化器中定义的默认汤底。
 */
public class ColdStewRecipeBuilder {

    /** 汤底原料，默认使用序列化器中定义的默认汤底 */
    private Ingredient soupBase = ColdStewRecipeSerializer.DEFAULT_SOUP_BASE;
    /** 原料列表 */
    private final List<Ingredient> ingredients = new ArrayList<>();
    /** 产出结果物品 */
    private ItemStack result;
    /** 处理时长（刻），默认 1200 */
    private int duration = 1200;

    /**
     * 创建构建器实例。
     *
     * @return 新的构建器实例
     */
    public static ColdStewRecipeBuilder builder() {
        return new ColdStewRecipeBuilder();
    }

    /**
     * 追加一项原料。
     *
     * @param ingredient 原料
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    /**
     * 以物品标识符追加一项原料。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder addIngredient(ResourceLocation itemId) {
        this.ingredients.add(Ingredient.of(BuiltInRegistries.ITEM.get(itemId)));
        return this;
    }

    /**
     * 设置产出结果物品。
     *
     * @param result 产出物品堆
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    /**
     * 以物品标识符设置产出结果物品。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder setResult(ResourceLocation itemId) {
        this.result = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 设置汤底原料。
     *
     * @param soupBase 汤底原料
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder setSoupBase(Ingredient soupBase) {
        this.soupBase = soupBase;
        return this;
    }

    /**
     * 以物品标识符设置汤底原料。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder setSoupBase(ResourceLocation itemId) {
        this.soupBase = Ingredient.of(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 以若干物品设置汤底原料。
     *
     * @param items 汤底物品
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder setSoupBase(net.minecraft.world.level.ItemLike... items) {
        this.soupBase = Ingredient.of(items);
        return this;
    }

    /**
     * 设置处理时长。
     *
     * @param duration 处理时长（刻）
     * @return 当前构建器实例
     */
    public ColdStewRecipeBuilder setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * 保存配方，使用基于产出物品路径自动生成的标识符。
     *
     * @param output 配方输出
     */
    public void save(RecipeOutput output) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "cold_stew/" + itemId.getPath());
        save(output, id);
    }

    /**
     * 保存配方，使用指定标识符。
     *
     * @param output 配方输出
     * @param id     配方标识符
     */
    public void save(RecipeOutput output, ResourceLocation id) {
        ColdStewRecipe recipe = new ColdStewRecipe(soupBase, List.copyOf(ingredients), result, duration);
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