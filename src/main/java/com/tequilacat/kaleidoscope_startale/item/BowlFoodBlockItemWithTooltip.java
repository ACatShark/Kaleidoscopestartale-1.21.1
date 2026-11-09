package com.tequilacat.kaleidoscope_startale.item;

import com.github.ysbbbbbb.kaleidoscopecookery.item.BowlFoodBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * 带提示文本的碗装食物方块物品类。
 * <p>
 * 继承碗装食物方块物品，在物品提示末尾追加一条
 * 由构造时指定翻译键决定的灰色说明文本。
 */
public class BowlFoodBlockItemWithTooltip extends BowlFoodBlockItem {

    /**
     * 提示文本的翻译键。
     */
    private final String tooltipKey;

    /**
     * 构造带提示文本的碗装食物方块物品。
     *
     * @param block          关联方块
     * @param foodProperties 食物属性
     * @param convertTo      食用后转换成的物品（如空碗）
     * @param tooltipKey     提示文本的翻译键
     */
    public BowlFoodBlockItemWithTooltip(Block block, FoodProperties foodProperties, Item convertTo, String tooltipKey) {
        super(block, foodProperties, convertTo);
        this.tooltipKey = tooltipKey;
    }

    /**
     * 追加物品提示文本。
     * <p>
     * 在父类提示基础上追加一条灰色说明文本。
     *
     * @param stack             物品堆叠
     * @param context           提示上下文
     * @param tooltipComponents 提示组件列表
     * @param tooltipFlag       提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GRAY));
    }
}