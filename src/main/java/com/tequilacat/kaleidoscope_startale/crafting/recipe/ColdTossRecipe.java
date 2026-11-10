package com.tequilacat.kaleidoscope_startale.crafting.recipe;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 冷炒配方。
 * <p>
 * 由原料列表、产物、翻炒次数与可选载体构成，
 * 匹配时逐一从输入中消耗匹配的原料。
 *
 * @param ingredients  原料列表
 * @param result       产物
 * @param stirFryCount 翻炒次数
 * @param carrier      可选载体
 */
public record ColdTossRecipe(List<Ingredient> ingredients, ItemStack result, int stirFryCount, Ingredient carrier)
        implements Recipe<SimpleInput> {

    /**
     * 构造不带载体的冷炒配方。
     *
     * @param ingredients  原料列表
     * @param result       产物
     * @param stirFryCount 翻炒次数
     */
    public ColdTossRecipe(List<Ingredient> ingredients, ItemStack result, int stirFryCount) {
        this(ingredients, result, stirFryCount, Ingredient.EMPTY);
    }

    /**
     * 检查输入是否匹配本配方。
     * <p>
     * 逐 ingredient 从输入物品中消耗一个匹配项，全部消耗成功返回 true。
     *
     * @param inv   炒锅输入容器（包装 9 槽 inputs）
     * @param level 世界实例
     * @return 全部原料匹配返回 true
     */
    @Override
    public boolean matches(SimpleInput inv, Level level) {
        List<ItemStack> remaining = new ArrayList<>();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }
        for (Ingredient ingredient : ingredients) {
            boolean matched = false;
            for (ItemStack stack : remaining) {
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    stack.shrink(1);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    /**
     * 组装配方输出。
     *
     * @param inv        炒锅输入容器
     * @param registries 注册表提供者
     * @return 产出物品堆
     */
    @Override
    public ItemStack assemble(SimpleInput inv, HolderLookup.Provider registries) {
        return result.copy();
    }

    /**
     * 判断配方是否可在指定尺寸的合成网格中制作。
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
     * @return 冷炒配方序列化器
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return KSRecipes.COLD_TOSS_SERIALIZER.get();
    }

    /**
     * 获取配方类型。
     *
     * @return 冷炒配方类型
     */
    @Override
    public RecipeType<?> getType() {
        return KSRecipes.COLD_TOSS_TYPE;
    }
}