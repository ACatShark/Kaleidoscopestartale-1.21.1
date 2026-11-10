package com.tequilacat.kaleidoscope_startale.data.builder;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ScentingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.stream.Stream;

/**
 * 窨制配方的数据生成构建器。
 * <p>
 * 以链式调用方式设置原料列表、产出与窨制时长等参数，
 * 提供多种原料设置重载与保存入口。
 * 原料数量上限为 MAX_INGREDIENTS，超出时抛出异常。
 */
public class ScentingRecipeBuilder {

    /** 最多 3 个原料 */
    public static final int MAX_INGREDIENTS = 3;

    /** 原料列表 */
    private List<Ingredient> ingredients;
    /** 产出结果物品 */
    private ItemStack result;
    /** 窨制时长（刻），默认 1200 */
    private int duration = 1200;

    /**
     * 创建构建器实例。
     *
     * @return 新的构建器实例
     */
    public static ScentingRecipeBuilder builder() {
        return new ScentingRecipeBuilder();
    }

    /**
     * 设置原料列表。
     *
     * @param ingredients 原料列表
     * @return 当前构建器实例
     * @throws IllegalArgumentException 原料数量超过上限时抛出
     */
    public ScentingRecipeBuilder setIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() > MAX_INGREDIENTS) {
            throw new IllegalArgumentException("ScentingRecipe 最多支持 " + MAX_INGREDIENTS + " 个原料，实际: " + ingredients.size());
        }
        this.ingredients = ingredients;
        return this;
    }

    /**
     * 以物品标识符数组设置原料列表。
     *
     * @param itemIds 物品标识符数组
     * @return 当前构建器实例
     * @throws IllegalArgumentException 原料数量超过上限时抛出
     */
    public ScentingRecipeBuilder setIngredients(ResourceLocation... itemIds) {
        if (itemIds.length > MAX_INGREDIENTS) {
            throw new IllegalArgumentException("ScentingRecipe 最多支持 " + MAX_INGREDIENTS + " 个原料，实际: " + itemIds.length);
        }
        this.ingredients = Stream.of(itemIds)
                .map(id -> Ingredient.of(BuiltInRegistries.ITEM.get(id)))
                .toList();
        return this;
    }

    /**
     * 以物品数组设置原料列表。
     *
     * @param items 物品数组
     * @return 当前构建器实例
     * @throws IllegalArgumentException 原料数量超过上限时抛出
     */
    public ScentingRecipeBuilder setIngredients(ItemLike... items) {
        if (items.length > MAX_INGREDIENTS) {
            throw new IllegalArgumentException("ScentingRecipe 最多支持 " + MAX_INGREDIENTS + " 个原料，实际: " + items.length);
        }
        this.ingredients = Stream.of(items)
                .map(Ingredient::of)
                .toList();
        return this;
    }

    /**
     * 设置产出结果物品。
     *
     * @param result 产出物品堆
     * @return 当前构建器实例
     */
    public ScentingRecipeBuilder setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    /**
     * 设置窨制时长。
     *
     * @param duration 窨制时长（刻）
     * @return 当前构建器实例
     */
    public ScentingRecipeBuilder setDuration(int duration) {
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
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "scenting/" + itemId.getPath());
        save(output, id);
    }

    /**
     * 保存配方，使用指定标识符。
     *
     * @param output 配方输出
     * @param id     配方标识符
     */
    public void save(RecipeOutput output, ResourceLocation id) {
        ScentingRecipe recipe = new ScentingRecipe(ingredients, result, duration);
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