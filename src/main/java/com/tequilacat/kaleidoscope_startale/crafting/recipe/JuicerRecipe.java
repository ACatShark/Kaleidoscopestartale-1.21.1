package com.tequilacat.kaleidoscope_startale.crafting.recipe;

import com.tequilacat.kaleidoscope_startale.crafting.serializer.JuicerRecipeInput;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 榨汁机配方。
 * <p>
 * 由原料列表、产出流体及流体量、制作时间与可选产物构成。
 * 匹配时按单个物品份数展开输入，回溯分配原料，
 * 并返回各槽位应消耗的数量，从而支持同一槽位堆叠多份原料。
 *
 * @param ingredients 原料列表
 * @param fluid       产出流体
 * @param fluidAmount 流体量
 * @param craftTime   制作时间（刻）
 * @param result      可选产物
 */
public record JuicerRecipe(NonNullList<Ingredient> ingredients, Fluid fluid, int fluidAmount,
                           int craftTime, ItemStack result) implements Recipe<JuicerRecipeInput> {

    /**
     * 紧凑构造函数。
     * <p>
     * 复制原料列表以避免外部修改，产物为 null 时替换为空堆叠。
     */
    public JuicerRecipe {
        NonNullList<Ingredient> copy = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < ingredients.size(); ++i) {
            copy.set(i, ingredients.get(i));
        }
        ingredients = copy;
        result = result == null ? ItemStack.EMPTY : result;
    }

    /**
     * 判断配方是否匹配输入。
     *
     * @param input 配方输入
     * @param level 世界
     * @return 是否匹配
     */
    @Override
    public boolean matches(JuicerRecipeInput input, Level level) {
        return this.matchConsumption(input) != null;
    }

    /**
     * 匹配输入并计算各槽位的消耗数量。
     * <p>
     * 将非空槽位按物品份数展开为单元列表（每槽最多贡献材料上限份数），
     * 回溯为每个原料分配一个单元；匹配成功时返回各槽位应消耗的数量，
     * 失败时返回 null。
     *
     * @param input 配方输入
     * @return 各槽位消耗数量数组，匹配失败时返回 null
     */
    public int @Nullable [] matchConsumption(JuicerRecipeInput input) {
        List<ItemStack> units = new ArrayList<>();
        List<Integer> unitSlot = new ArrayList<>();

        for (int slot = 0; slot < input.size(); ++slot) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            // 一个槽位最多贡献 4 份（材料上限）
            int usable = Math.min(stack.getCount(), this.ingredients.size());
            for (int i = 0; i < usable; ++i) {
                units.add(stack);
                unitSlot.add(slot);
            }
        }

        if (units.size() < this.ingredients.size()) {
            return null;
        }

        int[] assigned = new int[this.ingredients.size()];
        Arrays.fill(assigned, -1);
        if (!this.assign(0, units, assigned, new boolean[units.size()])) {
            return null;
        }

        int[] consumePerSlot = new int[input.size()];
        for (int unitIndex : assigned) {
            ++consumePerSlot[unitSlot.get(unitIndex)];
        }
        return consumePerSlot;
    }

    /**
     * 回溯分配原料单元。
     * <p>
     * 依次为每个原料寻找一个未使用且匹配的单元，找不到时回溯。
     *
     * @param index    当前原料索引
     * @param units    单元列表
     * @param assigned 每个原料分配的单元索引
     * @param used     单元是否已被使用
     * @return 是否成功分配全部原料
     */
    private boolean assign(int index, List<ItemStack> units, int[] assigned, boolean[] used) {
        if (index >= this.ingredients.size()) {
            return true;
        }

        Ingredient ingredient = this.ingredients.get(index);
        if (ingredient.isEmpty()) {
            return false;
        }

        for (int i = 0; i < units.size(); ++i) {
            if (!used[i] && ingredient.test(units.get(i))) {
                used[i] = true;
                assigned[index] = i;
                if (this.assign(index + 1, units, assigned, used)) {
                    return true;
                }
                used[i] = false;
                assigned[index] = -1;
            }
        }
        return false;
    }

    /**
     * 组装配方产物。
     *
     * @param input      配方输入
     * @param registries 注册表提供者
     * @return 产物副本
     */
    @Override
    public ItemStack assemble(JuicerRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    /**
     * 获取原料列表。
     *
     * @return 原料列表
     */
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    /**
     * 获取配方产物。
     *
     * @param registries 注册表提供者
     * @return 产物堆叠
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    /**
     * 获取配方序列化器。
     *
     * @return 榨汁机配方序列化器
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return KSRecipes.JUICER_SERIALIZER.get();
    }

    /**
     * 获取配方类型。
     *
     * @return 榨汁机配方类型
     */
    @Override
    public RecipeType<?> getType() {
        return KSRecipes.JUICER_TYPE;
    }

    /**
     * 判断是否为特殊配方。
     *
     * @return 恒为 true
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    /**
     * 判断能否在合成网格中合成。
     *
     * @param width  宽度
     * @param height 高度
     * @return 恒为 false
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }
}