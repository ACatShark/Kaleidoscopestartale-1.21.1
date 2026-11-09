package tequila.cat.pigpig.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import tequila.cat.pigpig.init.ModRecipes;

/**
 * 榨汁机配方：任意数量的材料组合 → 定量液体（+ 可选副产物物品）。
 * 材料在 4 个槽位之间可以任意分配，只用「总量」参与匹配。
 */
public record JuicerRecipe(NonNullList<Ingredient> ingredients, Fluid fluid, int fluidAmount,
                           int craftTime, ItemStack result) implements Recipe<JuicerRecipeInput> {

    public JuicerRecipe {
        NonNullList<Ingredient> copy = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < ingredients.size(); ++i) {
            copy.set(i, ingredients.get(i));
        }
        ingredients = copy;
        result = result == null ? ItemStack.EMPTY : result;
    }

    @Override
    public boolean matches(JuicerRecipeInput input, Level level) {
        return this.matchConsumption(input) != null;
    }

    /**
     * 计算每个槽位要消耗多少个物品；不匹配返回 null。
     * 采用二分图式的回溯分配（材料 ≤ 4 个，数量爆炸可控）。
     */
    @Nullable
    public int[] matchConsumption(JuicerRecipeInput input) {
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

    @Override
    public ItemStack assemble(JuicerRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.JUICER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.JUICER_TYPE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }
}
