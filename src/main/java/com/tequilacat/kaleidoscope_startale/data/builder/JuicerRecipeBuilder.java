package com.tequilacat.kaleidoscope_startale.data.builder;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.JuicerRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.serializer.JuicerRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

/**
 * 榨汁机配方的数据生成构建器。
 * <p>
 * 以链式调用方式添加工原料、设置流体与产出，并保存配方。
 * 流体量默认为 250 mB，制作时间默认为 59 刻，产物默认为空。
 */
public class JuicerRecipeBuilder {

    /** 原料列表 */
    private final List<Ingredient> ingredients = new ArrayList<>();
    /** 所需流体 */
    private Fluid fluid;
    /** 流体量（mB），默认 250 */
    private int fluidAmount = JuicerRecipeSerializer.DEFAULT_FLUID_AMOUNT;
    /** 制作时长（刻），默认 59 */
    private int craftTime = JuicerRecipeSerializer.DEFAULT_CRAFT_TIME;
    /** 产出结果物品，默认为空 */
    private ItemStack result = ItemStack.EMPTY;

    /**
     * 创建构建器实例。
     *
     * @return 新的构建器实例
     */
    public static JuicerRecipeBuilder builder() {
        return new JuicerRecipeBuilder();
    }

    /**
     * 添加一项原料。
     *
     * @param ingredient 原料
     * @return 当前构建器实例
     */
    public JuicerRecipeBuilder addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    /**
     * 添加一项原料。
     *
     * @param itemId 物品标识符
     * @return 当前构建器实例
     */
    public JuicerRecipeBuilder addIngredient(ResourceLocation itemId) {
        this.ingredients.add(Ingredient.of(BuiltInRegistries.ITEM.get(itemId)));
        return this;
    }

    /**
     * 设置所需流体。
     *
     * @param fluid 流体
     * @return 当前构建器实例
     */
    public JuicerRecipeBuilder setFluid(Fluid fluid) {
        this.fluid = fluid;
        return this;
    }

    /**
     * 设置所需流体量。
     *
     * @param fluidAmount 流体量（mB）
     * @return 当前构建器实例
     */
    public JuicerRecipeBuilder setFluidAmount(int fluidAmount) {
        this.fluidAmount = fluidAmount;
        return this;
    }

    /**
     * 设置制作时长。
     *
     * @param craftTime 制作时长（刻）
     * @return 当前构建器实例
     */
    public JuicerRecipeBuilder setCraftTime(int craftTime) {
        this.craftTime = craftTime;
        return this;
    }

    /**
     * 设置产出的果汁物品。
     *
     * @param result 产出物品堆
     * @return 当前构建器实例
     */
    public JuicerRecipeBuilder setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    /**
     * 保存配方，使用基于产出物品路径自动生成的标识符。
     * <p>
     * 产物为空时使用流体路径作为标识符。
     *
     * @param output 配方输出
     */
    public void save(RecipeOutput output) {
        ResourceLocation key;
        if (this.result.isEmpty()) {
            key = BuiltInRegistries.FLUID.getKey(this.fluid);
        } else {
            key = BuiltInRegistries.ITEM.getKey(this.result.getItem());
        }
        save(output, ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "juicer/" + key.getPath()));
    }

    /**
     * 保存配方，使用指定标识符。
     *
     * @param output 配方输出
     * @param id     配方标识符
     */
    public void save(RecipeOutput output, ResourceLocation id) {
        NonNullList<Ingredient> list = NonNullList.of(Ingredient.EMPTY, this.ingredients.toArray(Ingredient[]::new));
        JuicerRecipe recipe = new JuicerRecipe(list, this.fluid, this.fluidAmount, this.craftTime, this.result);
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
