package com.tequilacat.kaleidoscope_startale.data.builder;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdTossRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * 冷炒配方的数据生成构建器。
 * <p>
 * 以链式调用方式设置原料列表、产出、翻炒次数与取成品容器等参数，
 * 支持以物品或标识符形式设置字段，并提供多种保存入口。
 * 取成品容器默认为空，表示可直接右键取走成品。
 */
public class ColdTossRecipeBuilder {

    /** 原料列表 */
    private final List<Ingredient> ingredients = new ArrayList<>();
    /** 产出结果物品 */
    private ItemStack result;
    /** 翻炒次数，默认 5 */
    private int stirFryCount = 5;
    /** 取成品所需的容器，默认为空 */
    private Ingredient carrier = Ingredient.EMPTY;

    /**
     * 创建构建器实例。
     *
     * @return 新的构建器实例
     */
    public static ColdTossRecipeBuilder builder() {
        return new ColdTossRecipeBuilder();
    }

    /**
     * 追加一项原料。
     *
     * @param ingredient 原料
     * @return 当前构建器实例
     */
    public ColdTossRecipeBuilder addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    /**
     * 以物品标识符追加一项原料。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdTossRecipeBuilder addIngredient(ResourceLocation itemId) {
        this.ingredients.add(Ingredient.of(BuiltInRegistries.ITEM.get(itemId)));
        return this;
    }

    /**
     * 设置产出结果物品。
     *
     * @param result 产出物品堆
     * @return 当前构建器实例
     */
    public ColdTossRecipeBuilder setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    /**
     * 以物品标识符设置产出结果物品。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdTossRecipeBuilder setResult(ResourceLocation itemId) {
        this.result = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 设置翻炒次数。
     *
     * @param stirFryCount 翻炒次数
     * @return 当前构建器实例
     */
    public ColdTossRecipeBuilder setStirFryCount(int stirFryCount) {
        this.stirFryCount = stirFryCount;
        return this;
    }

    /**
     * 设置烹饪完成后取成品所需的容器原料。
     * <p>
     * 未设置时默认 Ingredient.EMPTY，表示直接右键即可取走成品；
     * 例如配方需要碗来接 → setCarrier(Ingredient.of(Items.BOWL))。
     *
     * @param carrier 取成品所需的容器原料
     * @return 当前构建器实例
     */
    public ColdTossRecipeBuilder setCarrier(Ingredient carrier) {
        this.carrier = carrier;
        return this;
    }

    /**
     * 以物品标识符设置取成品所需的容器原料。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public ColdTossRecipeBuilder setCarrier(ResourceLocation itemId) {
        this.carrier = Ingredient.of(BuiltInRegistries.ITEM.get(itemId));
        return this;
    }

    /**
     * 保存配方，使用基于产出物品路径自动生成的标识符。
     *
     * @param output 配方输出
     */
    public void save(RecipeOutput output) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "cold_toss/" + itemId.getPath());
        save(output, id);
    }

    /**
     * 保存配方，使用指定标识符。
     *
     * @param output 配方输出
     * @param id     配方标识符
     */
    public void save(RecipeOutput output, ResourceLocation id) {
        ColdTossRecipe recipe = new ColdTossRecipe(List.copyOf(ingredients), result, stirFryCount, carrier);
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