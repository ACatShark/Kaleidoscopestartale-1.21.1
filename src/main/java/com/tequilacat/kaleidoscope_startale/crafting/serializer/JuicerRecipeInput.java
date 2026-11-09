package com.tequilacat.kaleidoscope_startale.crafting.serializer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 榨汁机配方输入。
 * <p>
 * 从物品处理器快照出当前所有槽位的物品，
 * 作为榨汁机配方的输入数据，并提供空判定、相等性与哈希计算。
 */
public final class JuicerRecipeInput implements RecipeInput {

    /**
     * 快照得到的物品列表。
     */
    private final List<ItemStack> items;

    /**
     * 构造榨汁机配方输入。
     * <p>
     * 遍历物品处理器的所有槽位，复制当前物品形成不可变列表。
     *
     * @param handler 物品处理器
     */
    public JuicerRecipeInput(IItemHandler handler) {
        List<ItemStack> snapshot = new ArrayList<>(handler.getSlots());
        for (int i = 0; i < handler.getSlots(); ++i) {
            snapshot.add(handler.getStackInSlot(i));
        }
        this.items = List.copyOf(snapshot);
    }

    /**
     * 获取指定索引的物品。
     *
     * @param index 槽位索引
     * @return 该槽位的物品
     */
    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    /**
     * 获取槽位数量。
     *
     * @return 槽位数量
     */
    @Override
    public int size() {
        return this.items.size();
    }

    /**
     * 判断输入是否为空。
     * <p>
     * 所有槽位物品均为空时视为空输入。
     *
     * @return 是否为空输入
     */
    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    /**
     * 判断与另一对象是否相等。
     * <p>
     * 要求槽位数量一致且逐槽物品匹配。
     *
     * @param obj 另一对象
     * @return 是否相等
     */
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

    /**
     * 计算哈希值。
     * <p>
     * 基于各槽位物品类型（空槽位贡献 0）计算。
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        int hash = 1;
        for (ItemStack stack : this.items) {
            hash = 31 * hash + (stack.isEmpty() ? 0 : stack.getItem().hashCode());
        }
        return hash;
    }
}