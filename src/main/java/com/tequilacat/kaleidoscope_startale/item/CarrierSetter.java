package com.tequilacat.kaleidoscope_startale.item;

import net.minecraft.world.item.crafting.Ingredient;

/**
 * 油脂载体设置接口。
 * <p>
 * 由支持设置油脂载体的方块实体实现，
 * 用于在物品交互或自动化流程中为炒锅等设备设置所需的油脂原料。
 */
public interface CarrierSetter {

    /**
     * 设置油脂载体。
     *
     * @param ingredient 要设置的油脂原料
     */
    void KS$setCarrier(Ingredient ingredient);
}