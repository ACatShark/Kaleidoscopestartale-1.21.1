package com.tequilacat.kaleidoscope_startale.item;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.resources.DrinkEffectDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 模组饮品方块物品类。
 * <p>
 * 继承瓶子方块物品并实现容器接口，支持放置饮品方块、叠加饮品数量、
 * 饮用时施加对应酿造等级的药水效果，并返还空瓶。
 */
public class ModDrinkBlockItem extends BottleBlockItem implements IHasContainer {

    /**
     * 构造饮品方块物品。
     * <p>
     * 默认堆叠上限为 16。
     *
     * @param block 关联方块
     */
    public ModDrinkBlockItem(Block block) {
        super(block, new Item.Properties().stacksTo(16));
    }

    /**
     * 构造饮品方块物品。
     *
     * @param block      关联方块
     * @param properties 物品属性
     */
    protected ModDrinkBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /**
     * 获取空瓶物品。
     *
     * @return 空瓶物品
     */
    private static Item emptyBottle() {
        return ModItems.EMPTY_BOTTLE.get();
    }

    /**
     * 获取使用时长。
     *
     * @param stack  物品堆叠
     * @param entity 使用实体
     * @return 使用时长（刻）
     */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    /**
     * 获取使用动画。
     *
     * @param stack 物品堆叠
     * @return 饮用动画
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    /**
     * 对方块使用物品。
     * <p>
     * 潜行或玩家为空时尝试增加饮品数量，否则放置方块；
     * 非潜行时则直接进行饮用。
     *
     * @param context 使用上下文
     * @return 交互结果
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        Block self = this.getBlock();

        if (player == null || player.isShiftKeyDown()) {
            return player != null && this.tryIncreaseCount(self, state, level, pos, stack, player)
                    ? InteractionResult.SUCCESS
                    : this.place(new BlockPlaceContext(context));
        }

        InteractionResult result = this.use(level, player, context.getHand()).getResult();
        return result == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : result;
    }

    /**
     * 尝试增加饮品方块的数量。
     * <p>
     * 若目标方块为饮品方块且数量增加成功，则播放放置音效并消耗物品。
     *
     * @param self   当前方块
     * @param state  方块状态
     * @param level  世界
     * @param pos    位置
     * @param stack  物品堆叠
     * @param player 玩家
     * @return 是否成功增加数量
     */
    private boolean tryIncreaseCount(Block self, BlockState state, Level level, BlockPos pos, ItemStack stack, Player player) {
        if (!(self instanceof DrinkBlock drink) || !state.is(self) || !drink.tryIncreaseCount(level, pos, state, stack)) {
            return false;
        }
        SoundType soundType = state.getSoundType(level, pos, player);
        SoundEvent sound = this.getPlaceSound(state, level, pos, player);
        level.playSound(player, pos, sound, SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return true;
    }

    /**
     * 更新自定义方块实体数据。
     * <p>
     * 若目标为饮品方块实体，则将物品内容加入其中并刷新。
     *
     * @param pos    位置
     * @param level  世界
     * @param player 玩家
     * @param stack  物品堆叠
     * @param state  方块状态
     * @return 是否更新成功
     */
    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof DrinkBlockEntity be && be.addItem(stack)) {
            be.refresh();
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    /**
     * 使用物品。
     * <p>
     * 立即开始使用，用于触发饮用流程。
     *
     * @param level  世界
     * @param player 玩家
     * @param hand   交互手
     * @return 交互结果与物品堆叠
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    /**
     * 完成物品使用。
     * <p>
     * 触发消耗物品进度与统计，施加饮品效果，非创造模式消耗物品，
     * 最后将容器（空瓶）返还给实体。
     *
     * @param stack  物品堆叠
     * @param level  世界
     * @param entity 使用实体
     * @return 使用后的物品堆叠
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        this.addDrinkEffect(stack, level, entity);

        if (entity instanceof Player player && !player.isCreative()) {
            stack.shrink(1);
        }
        return this.returnContainerToEntity(stack, level, entity);
    }

    /**
     * 施加饮品效果。
     * <p>
     * 根据物品的酿造等级从效果数据中选取对应档位，
     * 按各条目的概率为实体添加状态效果。
     *
     * @param drink  饮品物品堆叠
     * @param level  世界
     * @param entity 使用实体
     */
    protected void addDrinkEffect(ItemStack drink, Level level, LivingEntity entity) {
        DrinkEffectData effectData = DrinkEffectDataReloadListener.INSTANCE.get(drink.getItem());
        if (effectData == null) {
            return;
        }
        List<List<DrinkEffectData.Entry>> effects = effectData.effects();
        if (effects.isEmpty()) {
            return;
        }

        int brewLevel = BottleBlockItem.getBrewLevel(drink);
        if (brewLevel < 1) {
            brewLevel = 1;
        }
        if (brewLevel > effects.size()) {
            brewLevel = effects.size();
        }

        for (DrinkEffectData.Entry entry : effects.get(brewLevel - 1)) {
            if (!level.isClientSide && level.random.nextFloat() < entry.probability()) {
                entity.addEffect(new MobEffectInstance(entry.effect(), entry.duration() * 20, entry.amplifier()));
            }
        }
    }

    /**
     * 获取容器物品。
     *
     * @return 空瓶物品
     */
    @Override
    public Item getContainerItem() {
        return emptyBottle();
    }
}