package com.tequilacat.kaleidoscope_startale.item;

import com.tequilacat.kaleidoscope_startale.block.CreamOilBlock;
import com.tequilacat.kaleidoscope_startale.blockentity.CreamOilBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 奶油油壶方块物品。
 * <p>
 * 继承自方块物品，在提示信息中显示当前奶油数量，
 * 空壶则显示为空提示，便于玩家在物品栏中辨识壶内储量。
 */
public class CreamOilBlockItem extends BlockItem {

    /**
     * 构造奶油油壶方块物品。
     *
     * @param block      对应方块
     * @param properties 物品属性
     */
    public CreamOilBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * 追加物品提示信息。
     * <p>
     * 先调用父类添加基础提示，再根据物品携带的奶油数量追加提示：
     * 数量大于 0 时以黄色显示数量与上限，否则以灰色显示空壶提示。
     *
     * @param stack            物品堆
     * @param context          提示上下文，可为 null
     * @param tooltipComponents 提示信息列表
     * @param tooltipFlag      提示标志
     */
    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable TooltipContext context,
            List<Component> tooltipComponents,
            net.minecraft.world.item.TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        int creamCount = CreamOilBlock.readCreamCount(stack);
        if (creamCount > 0) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.kaleidoscope_startale.cream_oil.count",
                    creamCount,
                    CreamOilBlockEntity.MAX_CREAM_COUNT
            ).withStyle(ChatFormatting.YELLOW));
        } else {
            tooltipComponents.add(Component.translatable(
                    "tooltip.kaleidoscope_startale.cream_oil.empty"
            ).withStyle(ChatFormatting.GRAY));
        }
    }
}