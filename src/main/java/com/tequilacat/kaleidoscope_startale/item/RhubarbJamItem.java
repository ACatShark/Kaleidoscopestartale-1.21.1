package com.tequilacat.kaleidoscope_startale.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * 大黄果酱物品类。
 * <p>
 * 继承模组饮品方块物品，完成使用时按食物属性进食，
 * 并指定饮用音效为蜂蜜饮用音效。
 */
public class RhubarbJamItem extends ModDrinkBlockItem {

    /**
     * 构造大黄果酱物品。
     *
     * @param block      关联方块
     * @param properties 物品属性
     */
    public RhubarbJamItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /**
     * 完成物品使用。
     * <p>
     * 若物品具有食物属性则让实体进食，否则回退到父类默认逻辑。
     *
     * @param stack  物品堆叠
     * @param level  世界
     * @param entity 使用实体
     * @return 使用后的物品堆叠
     */
    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        FoodProperties food = stack.getFoodProperties(entity);
        return food != null ? entity.eat(level, stack, food) : super.finishUsingItem(stack, level, entity);
    }

    /**
     * 获取饮用音效。
     *
     * @return 饮用音效
     */
    @Override
    public @NotNull SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }
}