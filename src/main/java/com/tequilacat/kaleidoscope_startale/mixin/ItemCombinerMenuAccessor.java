package com.tequilacat.kaleidoscope_startale.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 物品合成菜单访问器。
 * <p>
 * 通过 Mixin 访问器暴露 ItemCombinerMenu 的私有玩家字段，
 * 供其他 Mixin 在菜单逻辑中获取持有玩家。
 */
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {

    /**
     * 获取菜单对应的玩家。
     *
     * @return 玩家
     */
    @Accessor("player")
    Player KS$getPlayer();
}