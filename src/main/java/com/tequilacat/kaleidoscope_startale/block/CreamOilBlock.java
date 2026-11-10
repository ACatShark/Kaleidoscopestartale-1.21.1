package com.tequilacat.kaleidoscope_startale.block;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock;
import com.tequilacat.kaleidoscope_startale.blockentity.CreamOilBlockEntity;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 奶油壶方块。
 * <p>
 * 继承自油锅方块，额外记录锅内的奶油数量。
 * 奶油数量通过物品的自定义数据组件在放置、克隆与掉落之间传递，
 * 并支持手持奶油向锅内添加。
 */
public class CreamOilBlock extends OilPotBlock {

    /** 物品自定义数据中记录奶油数量的键名 */
    public static final String CREAM_COUNT_NBT = "cream_count";

    /**
     * 创建方块实体实例。
     *
     * @param pos   方块位置
     * @param state 方块状态
     * @return 奶油壶方块实体
     */
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreamOilBlockEntity(pos, state);
    }

    /**
     * 处理方块被放置时的逻辑。
     * <p>
     * 除父类逻辑外，若放置物品携带奶油数量数据，
     * 则将其恢复到方块实体中。
     *
     * @param level  当前世界
     * @param pos    方块位置
     * @param state  方块状态
     * @param placer 放置方块的实体
     * @param stack  被放置的物品堆
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof CreamOilBlockEntity creamOil) {
            int creamCount = readCreamCount(stack);
            if (creamCount > 0) {
                creamOil.setCreamCount(creamCount);
            }
        }
    }

    /**
     * 获取用于复制的物品堆（如创造模式中键取方块）。
     * <p>
     * 若方块实体中存在奶油数量，则将其写入返回的物品堆中。
     *
     * @param state  方块状态
     * @param target 点击结果
     * @param level  世界读取器
     * @param pos    方块位置
     * @param player 玩家
     * @return 携带奶油数量的物品堆
     */
    @Override
    public @NotNull ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        if (level.getBlockEntity(pos) instanceof CreamOilBlockEntity be) {
            int creamCount = be.getCreamCount();
            if (creamCount > 0) {
                writeCreamCount(stack, creamCount);
            }
        }
        return stack;
    }

    /**
     * 获取方块的掉落物。
     * <p>
     * 若方块实体中存在奶油数量，则将其写入对应的奶油壶掉落物中。
     *
     * @param state  方块状态
     * @param params 战利品参数构建器
     * @return 掉落物列表
     */
    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> stacks = super.getDrops(state, params);
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof CreamOilBlockEntity creamOil)) return stacks;
        int creamCount = creamOil.getCreamCount();
        if (creamCount <= 0) return stacks;
        stacks.forEach(s -> {
            if (s.is(KSItems.CREAM_OIL.get())) {
                writeCreamCount(s, creamCount);
            }
        });
        return stacks;
    }

    /**
     * 从物品堆中读取奶油数量。
     *
     * @param stack 物品堆
     * @return 奶油数量，未记录时返回 0
     */
    public static int readCreamCount(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) return 0;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        return tag.contains(CREAM_COUNT_NBT) ? tag.getInt(CREAM_COUNT_NBT) : 0;
    }

    /**
     * 将奶油数量写入物品堆的自定义数据中。
     *
     * @param stack      物品堆
     * @param creamCount 奶油数量
     */
    public static void writeCreamCount(ItemStack stack, int creamCount) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(CREAM_COUNT_NBT, creamCount);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * 处理在方块上使用物品的交互。
     * <p>
     * 仅响应主手且手持奶油时：按锅内剩余容量添加奶油，
     * 非创造模式消耗手持物品，并播放添加音效；
     * 其余情况交由默认方块交互处理。
     *
     * @param stack     使用的物品堆
     * @param state     方块状态
     * @param level     世界实例
     * @param pos       方块位置
     * @param player    交互的玩家
     * @param hand      交互手
     * @param hitResult 方块点击结果
     * @return 物品交互结果
     */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof CreamOilBlockEntity creamOil)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (mainHand.is(KSItems.CREAM.get())) {
            int need = CreamOilBlockEntity.MAX_CREAM_COUNT - creamOil.getCreamCount();
            if (need <= 0) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            int add = Math.min(need, mainHand.getCount());
            creamOil.addCream(add);
            if (!player.isCreative()) {
                mainHand.shrink(add);
            }
            player.playSound(SoundEvents.LANTERN_HIT, 1.0F,
                    player.getRandom().nextFloat() * 0.2F + 0.4F);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}