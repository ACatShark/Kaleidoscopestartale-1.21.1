package com.tequilacat.kaleidoscope_startale.crafting.recipe;

import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 窨制配方。
 * <p>
 * 竹盘上放指定多个物品，一定时间后转换为一个产物。
 *
 * @param ingredients 所需原料列表
 * @param result      产出结果物品
 * @param duration    窨制时长（刻）
 */
public record ScentingRecipe(List<Ingredient> ingredients, ItemStack result, int duration)
        implements Recipe<ScentingRecipe.Input> {

    /**
     * 窨制配方的输入。
     * <p>
     * 包装竹盘上所有槽位的物品。
     *
     * @param items 竹盘上的物品列表
     */
    public record Input(NonNullList<ItemStack> items) implements RecipeInput {

        /**
         * 获取指定槽位的物品。
         *
         * @param slot 槽位索引
         * @return 该槽位的物品
         */
        @Override
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        /**
         * 获取输入槽位数量。
         *
         * @return 槽位数量
         */
        @Override
        public int size() {
            return items.size();
        }
    }

    /**
     * 检查输入是否匹配本配方。
     * <p>
     * 在服务端执行：按堆叠数量逐个匹配，每个原料需要匹配并消耗一个物品，
     * 全部原料均匹配成功时视为匹配。
     *
     * @param inv   窨制输入容器
     * @param level 世界实例
     * @return 匹配返回 true
     */
    @Override
    public boolean matches(Input inv, Level level) {
        if (level.isClientSide) {
            return false;
        }
        // 按堆叠数量逐个匹配：每个 ingredient 需要匹配并消耗一个物品
        List<ItemStack> remaining = new java.util.ArrayList<>();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }
        for (Ingredient ingredient : ingredients) {
            boolean matched = false;
            for (ItemStack stack : remaining) {
                if (stack.isEmpty()) {
                    continue;
                }
                if (ingredient.test(stack)) {
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
     * @param inv        窨制输入容器
     * @param registries 注册表提供者
     * @return 产出物品堆
     */
    @Override
    public ItemStack assemble(Input inv, HolderLookup.Provider registries) {
        return result.copy();
    }

    /**
     * 判断配方是否可在指定尺寸的合成网格中制作。
     * <p>
     * 该配方不依赖合成网格，始终返回 true。
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
     * @return 窨制配方序列化器
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return KSRecipes.SCENTING_SERIALIZER.get();
    }

    /**
     * 获取配方类型。
     *
     * @return 窨制配方类型
     */
    @Override
    public RecipeType<?> getType() {
        return KSRecipes.SCENTING_TYPE;
    }
}