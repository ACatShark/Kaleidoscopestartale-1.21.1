package com.tequilacat.kaleidoscope_startale.mixin.client;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

/**
 * 创造模式物品栏界面调用器。
 * <p>
 * 通过 Mixin 调用器暴露界面私有的列表刷新方法，
 * 供其他 Mixin 在重排物品后以指定物品集合刷新当前页签。
 */
@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenInvoker {

    /**
     * 用给定物品刷新当前页签的列表。
     *
     * @param items 物品集合
     */
    @Invoker("refreshCurrentTabContents")
    void KS$refreshCurrentTabContents(Collection<ItemStack> items);
}