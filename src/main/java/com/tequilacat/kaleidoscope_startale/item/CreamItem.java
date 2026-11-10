package com.tequilacat.kaleidoscope_startale.item;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * 奶油物品。
 * <p>
 * 支持两种右键方块的用途：
 * 在炒锅上使用时将其油脂载体设为奶油；
 * 在空油壶上使用时将其替换为奶油油壶。
 */
public class CreamItem extends Item {

    /**
     * 构造奶油物品。
     *
     * @param properties 物品属性
     */
    public CreamItem(Properties properties) {
        super(properties);
    }

    /**
     * 处理在方块上使用奶油的交互。
     * <p>
     * 若点击的是炒锅，则将炒锅的油脂载体设为奶油并消耗物品；
     * 若点击的是空油壶（非奶油油壶），则将其替换为奶油油壶并消耗物品；
     * 其余情况交由父类处理。
     *
     * @param context 使用上下文
     * @return 交互结果
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();

        if (clickedState.is(ModBlocks.POT.get())) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            BlockEntity be = level.getBlockEntity(clickedPos);
            if (be instanceof PotBlockEntity potEntity && potEntity instanceof CarrierSetter setter) {
                setter.KS$setCarrier(Ingredient.of(KSItems.CREAM.get()));
                if (player != null && !player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }

        if (clickedState.is(ModBlocks.OIL_POT.get())
                && !clickedState.is(KSBlocks.CREAM_OIL.get())) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            level.setBlock(clickedPos, KSBlocks.CREAM_OIL.get().defaultBlockState(), 3);
            if (player != null && !player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        return super.useOn(context);
    }
}