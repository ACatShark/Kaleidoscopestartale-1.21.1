package com.tequilacat.kaleidoscope_startale.crafting.recipe;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.BaseRecipe;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * 冷萃茶壶配方。
 * <p>
 * 仅当茶壶下方为灵魂火时生效。
 * 需匹配指定茶液类型与所需原料及数量，产出固定数量的结果物品。
 *
 * @param teaFluid       所需茶液标识符
 * @param ingredient     所需原料
 * @param ingredientCount 所需原料数量
 * @param time           处理时长（刻）
 * @param result         产出结果物品
 */
public record ColdBrewTeapotRecipe(ResourceLocation teaFluid, Ingredient ingredient, int ingredientCount,
                                   int time, ItemStack result, Ingredient container
) implements BaseRecipe<TeapotInput> {

    /** 单次产出的固定数量 */
    public static final int OUTPUT_COUNT = 12;

    /**
     * 检查输入是否匹配本配方。
     * <p>
     * 需同时满足：茶液标识符一致、原料通过匹配、原料数量不低于所需数量。
     *
     * @param container 茶壶输入容器
     * @param level     世界实例
     * @return 匹配返回 true
     */
    @Override
    public boolean matches(TeapotInput container, Level level) {
        ItemStack stack = container.getItemStack();
        ResourceLocation fluid = container.getTeaFluid();
        return teaFluid.equals(fluid) && ingredient.test(stack) && stack.getCount() >= ingredientCount;
    }

    /**
     * 获取配方结果物品。
     *
     * @param provider 注册表提供者
     * @return 结果物品
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    /**
     * 组装配方输出。
     * <p>
     * 基于结果物品复制并设置为固定产出数量。
     *
     * @param container      茶壶输入容器
     * @param registryAccess 注册表访问
     * @return 产出物品堆
     */
    @Override
    public ItemStack assemble(TeapotInput container, HolderLookup.Provider registryAccess) {
        return getResultItem(registryAccess).copyWithCount(OUTPUT_COUNT);
    }

    /**
     * 获取配方序列化器。
     *
     * @return 冷萃茶壶配方序列化器
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return KSRecipes.COLD_BREW_TEAPOT_SERIALIZER.get();
    }

    /**
     * 获取配方类型。
     *
     * @return 冷萃茶壶配方类型
     */
    @Override
    public RecipeType<?> getType() {
        return KSRecipes.COLD_BREW_TEAPOT_TYPE;
    }
}