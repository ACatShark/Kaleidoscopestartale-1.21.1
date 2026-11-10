package com.tequilacat.kaleidoscope_startale.crafting.recipe;

import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * 发酵配方。
 * <p>
 * 竹盘上的物品在光照处于 [minLight, maxLight] 区间时发酵转化为结果。
 *
 * @param ingredient 所需原料
 * @param result     产出结果物品
 * @param duration   发酵时长（刻）
 * @param minLight   最小光照等级（含）
 * @param maxLight   最大光照等级（含）
 */
public record FermentingRecipe(Ingredient ingredient, ItemStack result, int duration, int minLight, int maxLight)
        implements Recipe<SingleRecipeInput> {

    /**
     * 检查输入是否匹配本配方。
     *
     * @param inv   单物品输入容器
     * @param level 世界实例
     * @return 原料匹配返回 true
     */
    @Override
    public boolean matches(SingleRecipeInput inv, Level level) {
        return ingredient.test(inv.getItem(0));
    }

    /**
     * 检查给定光照等级是否在本配方的光照区间内。
     *
     * @param light 光照等级
     * @return 光照在 [minLight, maxLight] 范围内返回 true
     */
    public boolean matchesLight(int light) {
        return light >= minLight && light <= maxLight;
    }

    /**
     * 组装配方输出。
     *
     * @param inv        单物品输入容器
     * @param registries 注册表提供者
     * @return 产出物品堆
     */
    @Override
    public ItemStack assemble(SingleRecipeInput inv, HolderLookup.Provider registries) {
        return result.copy();
    }

    /**
     * 判断配方是否可在指定尺寸的合成网格中制作。
     * <p>
     * 该配方为单输入单输出，不受网格尺寸限制，始终返回 true。
     *
     * @param width  网格宽度
     * @param height 网格高度
     * @return 始终返回 true
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    /**
     * 获取配方结果物品。
     *
     * @param registries 注册表提供者
     * @return 结果物品
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    /**
     * 获取配方序列化器。
     *
     * @return 发酵配方序列化器
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return KSRecipes.FERMENTING_SERIALIZER.get();
    }

    /**
     * 获取配方类型。
     *
     * @return 发酵配方类型
     */
    @Override
    public RecipeType<?> getType() {
        return KSRecipes.FERMENTING_TYPE;
    }
}