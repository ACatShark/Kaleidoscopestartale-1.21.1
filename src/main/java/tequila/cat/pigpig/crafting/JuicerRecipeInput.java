package tequila.cat.pigpig.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import net.neoforged.neoforge.items.IItemHandler;

/** 榨汁机配方输入：对物品栏做一次快照，避免配方匹配过程中被并发修改。 */
public final class JuicerRecipeInput implements RecipeInput {
    private final List<ItemStack> items;

    public JuicerRecipeInput(IItemHandler handler) {
        List<ItemStack> snapshot = new ArrayList<>(handler.getSlots());
        for (int i = 0; i < handler.getSlots(); ++i) {
            snapshot.add(handler.getStackInSlot(i));
        }
        this.items = List.copyOf(snapshot);
    }

    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JuicerRecipeInput other) || this.items.size() != other.items.size()) {
            return false;
        }
        for (int i = 0; i < this.items.size(); ++i) {
            if (!ItemStack.matches(this.items.get(i), other.items.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (ItemStack stack : this.items) {
            hash = 31 * hash + (stack.isEmpty() ? 0 : stack.getItem().hashCode());
        }
        return hash;
    }
}
