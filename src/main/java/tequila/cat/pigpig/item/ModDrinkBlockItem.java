package tequila.cat.pigpig.item;

import java.util.List;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.resources.DrinkEffectDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 可放置的酒（tavern 的 DrinkBlock 方块物品）+ 可直接饮用。
 * 饮用时按酿造等级从 drink_effect 数据表里抽取效果，喝完把空瓶还给玩家。
 */
public class ModDrinkBlockItem extends BottleBlockItem implements IHasContainer {
    public ModDrinkBlockItem(Block block) {
        super(block, new Item.Properties().stacksTo(16));
    }

    protected ModDrinkBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    private static Item emptyBottle() {
        return ModItems.EMPTY_BOTTLE.get();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

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

    /** 对着同一瓶酒右键：叠一层。 */
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

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof DrinkBlockEntity be && be.addItem(stack)) {
            be.refresh();
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

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

    @Override
    public Item getContainerItem() {
        return emptyBottle();
    }
}
