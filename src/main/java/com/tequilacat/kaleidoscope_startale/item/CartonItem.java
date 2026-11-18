package com.tequilacat.kaleidoscope_startale.item;

import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * 纸盒物品。
 * <p>
 * 继承方块物品，堆叠上限 64。对成年牛使用时可将其「挤奶」为牛奶盒：
 * 服务端消耗一个纸盒并把牛奶盒交给玩家，同时播放挤奶音效；
 * 幼年牛与非牛实体不响应。
 */
public class CartonItem extends BlockItem {

    /**
     * 构造纸盒物品。
     *
     * @param block 关联方块
     */
    public CartonItem(Block block) {
        super(block, new Item.Properties().stacksTo(64));
    }

    /**
     * 对活体实体使用物品。
     * <p>
     * 仅对成年牛生效：服务端消耗一个纸盒并把牛奶盒放入玩家物品栏（放不下则掉落），
     * 同时播放挤奶音效；返回按侧区分的成功结果。
     *
     * @param stack  物品堆叠
     * @param player 玩家
     * @param target 目标实体
     * @param hand   交互手
     * @return 交互结果
     */
    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
                                                           @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!(target instanceof Cow cow) || cow.isBaby()) {
            return InteractionResult.PASS;
        }
        Level level = player.level();
        if (!level.isClientSide) {
            ItemStack milkCarton = new ItemStack(KSItems.MILK_CARTON.get());
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            if (!player.getInventory().add(milkCarton)) {
                player.drop(milkCarton, false);
            }
            cow.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}