package tequila.cat.pigpig.item;

import java.util.List;

import com.github.ysbbbbbb.kaleidoscopecookery.item.BowlFoodBlockItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** 可放置的碗装食物 + 一句风味文本。 */
public class BowlFoodBlockItemWithTooltip extends BowlFoodBlockItem {
    private final String tooltipKey;

    public BowlFoodBlockItemWithTooltip(Block block, FoodProperties foodProperties, Item convertTo, String tooltipKey) {
        super(block, foodProperties, convertTo);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GRAY));
    }
}
