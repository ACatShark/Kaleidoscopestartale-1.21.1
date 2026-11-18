package com.tequilacat.kaleidoscope_startale.item;

import com.tequilacat.kaleidoscope_startale.init.KSFoods;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * 纸盒牛奶物品。
 * <p>
 * 继承方块物品，堆叠上限 64 并带有食物属性。既可放置为方块，也可直接饮用：
 * 潜行时放置方块，非潜行时开始饮用；饮用完成会触发消耗进度与统计、
 * 清除全部状态效果、播放饮用音效，并返还空纸盒。
 */
public class MilkCartonItem extends BlockItem {

    /** 饮用耗时（刻）。 */
    private static final int USE_DURATION = 32;

    /**
     * 创建纸盒牛奶物品。
     *
     * @param block 关联方块
     */
    public MilkCartonItem(Block block) {
        super(block, new Item.Properties().stacksTo(64).food(KSFoods.MILK_CARTON_FOOD));
    }

    /**
     * 获取饮用时长。
     *
     * @param stack  物品堆叠
     * @param entity 使用实体
     * @return 使用时长（刻）
     */
    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return USE_DURATION;
    }

    /**
     * 获取使用动画。
     *
     * @param stack 物品堆叠
     * @return 饮用动画
     */
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    /**
     * 使用物品。
     * <p>
     * 潜行时走方块放置逻辑，否则直接开始饮用。
     *
     * @param level  世界
     * @param player 玩家
     * @param hand   交互手
     * @return 交互结果
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    /**
     * 对方块使用物品。
     * <p>
     * 潜行时放置方块，否则直接饮用（与模组内其他饮品一致）。
     *
     * @param context 使用上下文
     * @return 交互结果
     */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        InteractionResult result = this.use(context.getLevel(), player, context.getHand()).getResult();
        return result == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : result;
    }

    /**
     * 完成饮用。
     * <p>
     * 先交由父类处理食物消耗，再在服务端触发消耗进度与统计；
     * 服务端清除全部状态效果并播放饮用音效；玩家饮用后返还空纸盒，非玩家实体则消耗一个。
     *
     * @param stack  物品堆叠
     * @param level  世界
     * @param entity 使用实体
     * @return 使用后的物品堆叠
     */
    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level,
                                              @NotNull LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }
        if (!level.isClientSide) {
            entity.removeAllEffects();
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (entity instanceof Player player) {
            return ItemUtils.createFilledResult(stack, player, new ItemStack(KSItems.CARTON.get()));
        }
        stack.shrink(1);
        return stack;
    }
}
