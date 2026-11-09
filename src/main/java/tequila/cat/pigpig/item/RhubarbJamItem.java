package tequila.cat.pigpig.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** 大黄酱：像喝酒一样饮用（有饮用音效），但结算走原版进食逻辑。 */
public class RhubarbJamItem extends ModDrinkBlockItem {
    public RhubarbJamItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        FoodProperties food = stack.getFoodProperties(entity);
        return food != null ? entity.eat(level, stack, food) : super.finishUsingItem(stack, level, entity);
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }
}
